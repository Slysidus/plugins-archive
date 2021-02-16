package net.lightning.common.models;

import net.lightning.api.database.MySQLHikaryDatabase;
import net.lightning.api.database.model.ModelAccessor;
import net.lightning.api.database.model.ModelManager;
import net.lightning.api.database.providers.SQLDatabaseProvider;
import net.lightning.common.CommonModelDeclaration;
import net.lightning.common.Rank;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MySQLTest {

    private static final SQLDatabaseProvider database;
    private static final ModelAccessor<NetworkPlayer>.AccessorContext playerTable;

    private static final UUID developerUUID = UUID.randomUUID();

    static {
        database = new MySQLHikaryDatabase(
                "localhost",
                3306,
                "lightning_test",
                "root",
                "root",
                "serverTimezone=UTC"
        );

        CommonModelDeclaration.registerCustomAccessors();

        final ModelAccessor<NetworkPlayer> playerAccessor = ModelManager.getAccessor(NetworkPlayer.class);
        assert playerAccessor != null;
        playerTable = playerAccessor.new AccessorContext(database);
    }

    @Test
    @Order(1)
    void login() throws Exception {
        database.login();
    }

    @Test
    @Order(2)
    void create() throws Exception {
        assertFalse(playerTable.createTable(true));
    }

    @Test
    @Order(3)
    void insert() throws Exception {
        assertTrue(playerTable.insert(createPlayer(developerUUID, "DEV", Rank.DEVELOPER)));
    }

    @Test
    @Order(4)
    void get() throws Exception {
        assertEquals("DEV", playerTable.get("unique_id", developerUUID).getCachedName());
    }

    @Test
    @Order(5)
    void insertOrUpdate() throws Exception {
        assertTrue(playerTable.insertOrUpdate(createPlayer(developerUUID, "DEV-UPDATE", Rank.DEVELOPER)));
        assertEquals("DEV-UPDATE", playerTable.get("unique_id", developerUUID).getCachedName());
    }

    @Test
    @Order(6)
    void update() throws Exception {
        NetworkPlayer player = playerTable.get("unique_id", developerUUID);
        player.setRank(Rank.ADMIN);
        playerTable.update(player, "unique_id", developerUUID);
        assertEquals(Rank.ADMIN, playerTable.get("unique_id", developerUUID).getRank());
    }

    protected static NetworkPlayer createPlayer(UUID uuid, String name, Rank rank) {
        Timestamp now = Timestamp.from(Instant.now());
        return new NetworkPlayer(uuid, name, rank, false, now, now, 0, 0, 0);
    }

}