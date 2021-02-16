package net.lightning.common.models;

import net.lightning.api.database.model.ModelAccessor;
import net.lightning.api.database.model.ModelManager;
import net.lightning.common.CommonModelDeclaration;
import net.lightning.common.Rank;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.redisson.Redisson;
import org.redisson.api.RBatch;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static net.lightning.common.models.MySQLTest.createPlayer;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RedisTest {

    private static final RedissonClient redissonClient;
    private static final ModelAccessor<NetworkPlayer>.RedisAccessorContext redisPlayerTable;

    private static final UUID testPlayerUUID = UUID.randomUUID();

    private static final List<UUID> bunchOfUUID;

    static {
        Config redisConfig = new Config();
        redisConfig.setUseLinuxNativeEpoll(true);
        redisConfig.setThreads(4);
        redisConfig.setNettyThreads(4);
        redisConfig.useSingleServer()
                .setAddress("127.0.0.1:6379")
                .setDatabase(7);

        redissonClient = Redisson.create(redisConfig);

        CommonModelDeclaration.registerCustomAccessors();
        final ModelAccessor<NetworkPlayer> playerAccessor = ModelManager.getAccessor(NetworkPlayer.class);
        assert playerAccessor != null;
        redisPlayerTable = playerAccessor.new RedisAccessorContext(redissonClient);

        bunchOfUUID = new ArrayList<>();
    }

    @Test
    @Order(1)
    void insert() {
        redisPlayerTable.put(createPlayer(testPlayerUUID, "TEST", Rank.PLAYER));
    }

    @Test
    @Order(2)
    void get() {
        assertEquals(testPlayerUUID, redisPlayerTable.get(testPlayerUUID).getUniqueId());
    }

    @Test
    @Order(3)
    void update() {
        Timestamp now = Timestamp.from(Instant.now());
        NetworkPlayer player = redisPlayerTable.get(testPlayerUUID);
        player.setLastLogin(now);
        redisPlayerTable.put(player);
        assertEquals(now, redisPlayerTable.get(testPlayerUUID).getLastLogin());
    }

    @Test
    @Order(4)
    void delete() {
        redisPlayerTable.delete(testPlayerUUID);
        assertNull(redisPlayerTable.get(testPlayerUUID));
    }

    @Test
    @Order(5)
    void performancePut50() {
        performanceTest(50);
    }

    @Test
    @Order(6)
    void performancePut200() {
        bunchOfUUID.addAll(performanceTest(200));
    }

    @Test
    @Order(7)
    void performancePut50Batch() {
        performanceBatchTest(50);
    }

    @Test
    @Order(8)
    void performancePut200Batch() {
        performanceBatchTest(200);
    }

    @Test
    @Order(9)
    void performanceGet50() {
        final Random random = new Random();
        Rank[] ranks = Rank.values();

        List<NetworkPlayer> playerList = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            playerList.add(redisPlayerTable.get(bunchOfUUID.get(i)));
        }

        assertFalse(playerList.stream()
                .anyMatch(player -> !bunchOfUUID.contains(player.getUniqueId())));
    }

    @Test
    @Order(10)
    void performanceGet50Batch() {
        final Random random = new Random();
        Rank[] ranks = Rank.values();

        RBatch batch = redissonClient.createBatch();
        List<Future<NetworkPlayer>> futures = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            futures.add(redisPlayerTable.get(batch, bunchOfUUID.get(i).toString()));
        }
        batch.execute();

        List<NetworkPlayer> playerList = futures.stream()
                .map(future -> {
                    try {
                        return future.get();
                    }
                    catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .collect(Collectors.toList());
        assertFalse(playerList.stream()
                .anyMatch(player -> !bunchOfUUID.contains(player.getUniqueId())));
    }

    private List<UUID> performanceTest(int size) {
        final Random random = new Random();
        Rank[] ranks = Rank.values();

        List<UUID> bunchOfUUID = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            UUID uuid = UUID.randomUUID();
            redisPlayerTable.put(createPlayer(uuid, "RANDOM-PLAYER", ranks[random.nextInt(ranks.length)]));
            bunchOfUUID.add(uuid);
        }
        return bunchOfUUID;
    }

    private void performanceBatchTest(int size) {
        final Random random = new Random();
        Rank[] ranks = Rank.values();

        RBatch batch = redissonClient.createBatch();
        for (int i = 0; i < size; i++) {
            UUID uuid = UUID.randomUUID();
            redisPlayerTable.put(createPlayer(UUID.randomUUID(), "RANDOM-PLAYER", ranks[random.nextInt(ranks.length)]));
        }
        batch.execute();
    }

}
