package net.lightning.core;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.lightning.api.database.model.ModelAccessor;
import net.lightning.common.CommonCacheManager;
import net.lightning.common.models.NetworkPlayer;
import net.lightning.core.event.GameEventHandler;
import net.lightning.core.event.GameEventManager;
import net.lightning.core.event.GameEventPriority;
import net.lightning.core.event.GameListener;
import net.lightning.core.event.player.PlayerPreJoinGameEvent;
import net.lightning.core.event.player.PlayerSpawnGameEvent;
import net.lightning.core.map.GameMapLoader;
import net.lightning.core.map.GameMapModel;
import net.lightning.core.modules.GameModule;
import net.lightning.core.server.PlayerNPC;
import net.lightning.core.sidebar.SidebarDisplay;
import net.lightning.core.sidebar.SidebarPane;
import net.lightning.core.team.DefaultTeamGiver;
import net.lightning.core.team.TeamGiver;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.redisson.api.RBatch;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Getter
public abstract class Game implements GameListener {

    protected final String coinsDisplay = "❍";

    protected final LightningGamePlugin<?> plugin;
    protected final Random random;

    protected final NPCManager npcManager;
    private final GameTeam npcTeam;

    protected final World world;
    protected final GameMapModel map;
    protected final GameEventManager eventManager;

    protected final SidebarDisplay sidebarDisplay;

    private final TeamGiver teamGiver;
    private GameTeam[] teams;

    @Setter(AccessLevel.PROTECTED)
    private GameState gameState;
    protected final Map<UUID, GamePlayer> players;
    private final Set<UUID> syncingPlayers;

    protected final GameSettings settings;
    protected final Map<Class<GameModule<? extends Game>>, GameModule<? extends Game>> modules;

    public Game(LightningGamePlugin<?> plugin) {
        this.plugin = plugin;
        this.random = new Random();

        NPCManager npcManager1;
        try {
            npcManager1 = new NPCManager(this);
        }
        catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            npcManager1 = null;
        }
        this.npcManager = npcManager1;
        this.npcTeam = new GameTeam(null, "Z-NPC", "[NPC] ", null, ChatColor.DARK_GRAY, 0, false);

        this.world = Bukkit.getWorlds().get(0);
        this.map = loadMapModel(new GameMapLoader());
        this.eventManager = new GameEventManager(plugin);
        this.settings = new GameSettings();
        this.sidebarDisplay = initSidebarDisplay();
        this.teams = new GameTeam[0];
        this.teamGiver = initTeamGiver();
        this.modules = new HashMap<>();
        this.players = new HashMap<>();
        this.syncingPlayers = new HashSet<>();
    }

    protected final void init() {
        if (map == null) {
            Bukkit.shutdown();
            return;
        }

        initModules();
        initTeams();

        initListeners();
        eventManager.register(this);
        if (this instanceof Listener) {
            throw new IllegalStateException("Please use a module or register another class to use native listeners.");
        }

        postInit();
    }

    /*
    Settings
     */

    protected abstract SidebarDisplay initSidebarDisplay();

    protected TeamGiver initTeamGiver() {
        return new DefaultTeamGiver();
    }

    protected abstract void initModules();

    protected void initTeams() {
    }

    protected void initListeners() {
    }

    protected abstract GameMapModel loadMapModel(GameMapLoader mapLoader);

    protected void postInit() {
    }

    protected void registerTeam(GameTeam team) {
        this.teams = (GameTeam[]) ArrayUtils.add(teams, team);
    }

    protected GameTeam registerColorTeam(@NotNull ChatColor color) {
        GameTeam team = new GameTeam(null, color.name(), null, null, color, teams.length, true);
        registerTeam(team);
        return team;
    }

    @SuppressWarnings("unchecked")
    protected void registerModule(GameModule<? extends Game> module) {
        for (Class<? extends GameModule<?>> dependency : module.getDependencies()) {
            if (!isModuleLoaded(dependency)) {
                plugin.getLogger().severe("Module '" + module.getName() + "' rely on module from class '" + dependency.getName() + "' which is not loaded.");
                return;
            }
        }

        module.setGame(this);
        if (module instanceof Listener) {
            eventManager.registerNative((Listener) module);
        }
        if (module instanceof GameListener) {
            eventManager.register((GameListener) module);
        }
        modules.put((Class<GameModule<? extends Game>>) module.getClass(), module);
    }

    @SuppressWarnings("unchecked")
    public <T extends GameModule<? extends Game>> T getModule(Class<T> moduleClass) {
        return (T) modules.get(moduleClass);
    }

    public <T extends GameModule<? extends Game>> boolean isModuleLoaded(Class<T> moduleClass) {
        return modules.containsKey(moduleClass);
    }

    /*
    Players
     */

    public final short getPlayerCount() {
        return (short) players.size();
    }

    public boolean canPlayerJoin(Player nativePlayer) {
        return nativePlayer.isOp() || getPlayerCount() < map.maxPlayers;
    }

    public abstract GamePlayer prepareJoin(Player nativePlayer, NetworkPlayer networkPlayer);

    protected GameTeam[] getScoreboardTeams() {
        return (GameTeam[]) ArrayUtils.add(teams, npcTeam);
    }

    /**
     * Try to join the game
     *
     * @param player The player
     * @return null if successful, deny message otherwise (can be empty)
     */
    public final String join(GamePlayer player) {
        Preconditions.checkNotNull(player);
        Preconditions.checkArgument(!players.containsKey(player.getUniqueId()));

        PlayerPreJoinGameEvent preJoinEvent = new PlayerPreJoinGameEvent(this, player);
        if (preJoinEvent.isCancelled()) {
            return preJoinEvent.getMessage();
        }

        if (teamGiver != null) {
            GameTeam team = teamGiver.attribute(this, player);
            player.setTeam(team);
        }

        if (sidebarDisplay != null && player.getTeam() != null) {
            SidebarPane pane = sidebarDisplay.addNewPlayer(player);
            if (pane != null) {
                // for each team
                for (GameTeam gameTeam : teams) {
                    // get the bukkit team to operate on
                    Team nativeTeam = pane.getNativeTeam(gameTeam);
                    // for each player in this team
                    for (GamePlayer otherPlayer : getPlayersInTeam(gameTeam)) {
                        // make sure the player is in it
                        if (!nativeTeam.hasEntry(otherPlayer.getName())) {
                            nativeTeam.addEntry(otherPlayer.getName());
                        }
                    }
                }
                Team npcNativeTeam = pane.getNativeTeam(npcTeam);
                for (PlayerNPC npc : npcManager.getNPCs()) {
                    final String name = npc.getProfile().getName();
                    if (!npcNativeTeam.hasEntry(name)) {
                        npcNativeTeam.addEntry(name);
                    }
                }

                player.getNativePlayer().setScoreboard(pane.getNativeScoreboard());
            }

            sidebarDisplay.forTeam(player.getTeam(), nativeTeam -> nativeTeam.addEntry(player.getName()));
        }

        players.put(player.getUniqueId(), player);
        GameNPECatcher.validateGameState(this);
        return null;
    }

    public void quit(GamePlayer player) {
        Preconditions.checkNotNull(player);
        Preconditions.checkArgument(players.containsKey(player.getUniqueId()));

        players.remove(player.getUniqueId());
        if (sidebarDisplay != null) {
            sidebarDisplay.removePlayer(player);
            sidebarDisplay.forTeam(player.getTeam(), nativeTeam -> nativeTeam.removeEntry(player.getName()));
        }
    }

    public @NotNull GamePlayer getPlayer(Player nativePlayer) {
        Preconditions.checkNotNull(nativePlayer);
        Preconditions.checkArgument(players.containsKey(nativePlayer.getUniqueId()));

        return players.get(nativePlayer.getUniqueId());
    }

    public Collection<GamePlayer> getPlayersInTeam(GameTeam team) {
        return players.values().stream()
                .filter(player -> player.getTeam() == team)
                .collect(Collectors.toList());
    }

    public short getPlayerCountInTeam(GameTeam team) {
        return (short) players.values().stream()
                .filter(player -> player.getTeam() == team)
                .count();
    }

    public void setTeam(GamePlayer player, GameTeam team) {
        if (ArrayUtils.indexOf(teams, team) == -1) {
            throw new IllegalStateException("Setting player's team to unregistred team!");
        }

        GameTeam previousTeam = player.getTeam();
        if (previousTeam.equals(team)) {
            return;
        }
        player.setTeam(team);

        if (sidebarDisplay != null) {
            sidebarDisplay.forTeam(previousTeam, nativeTeam -> nativeTeam.removeEntry(player.getName()));
            sidebarDisplay.forTeam(player.getTeam(), nativeTeam -> nativeTeam.addEntry(player.getName()));
        }
    }

    public void broadcast(String message) {
        Bukkit.broadcastMessage(message);
    }

    /*
    Utils
     */
    protected void renderLines(SidebarPane pane, List<String> lines) {
        for (int i = 0; i < lines.size(); i++) {
            pane.setLine(i, lines.get(i));
        }
    }

    /*
    Default listeners
     */
    @GameEventHandler(priority = GameEventPriority.HIGH)
    public void onPlayerSpawnInternalHigh(PlayerSpawnGameEvent event) {
        Player nativePlayer = event.getPlayer().getNativePlayer();
        nativePlayer.setGameMode(GameMode.SURVIVAL);
    }

    /*
    Database operations
     */
    protected void batchCacheExecute(Collection<GamePlayer> players, Consumer<GamePlayer> action) {
        CommonCacheManager commonCacheManager = plugin.getCommonCacheManager();
        ModelAccessor<NetworkPlayer>.RedisAccessorContext playerCache = commonCacheManager.getPlayerCache();
        RBatch batch = commonCacheManager.getRedissonClient().createBatch();

        for (GamePlayer player : players) {
            action.accept(player);
            NetworkPlayer networkPlayer = player.getNetworkPlayer();
            playerCache.set(batch, networkPlayer.getUniqueId().toString(), networkPlayer);
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, batch::executeSkipResult);
    }

}
