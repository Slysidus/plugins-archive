package net.lightning.lobby;

import lombok.Getter;
import net.lightning.api.util.StringUtil;
import net.lightning.common.Rank;
import net.lightning.common.models.NetworkPlayer;
import net.lightning.core.Game;
import net.lightning.core.GamePlayer;
import net.lightning.core.GameTeam;
import net.lightning.core.LightningGamePlugin;
import net.lightning.core.event.GameEventHandler;
import net.lightning.core.event.GameEventPriority;
import net.lightning.core.event.player.PlayerJoinGameEvent;
import net.lightning.core.event.player.PlayerSpawnGameEvent;
import net.lightning.core.loader.types.ReadableLocation;
import net.lightning.core.map.GameMapLoader;
import net.lightning.core.map.GameMapModel;
import net.lightning.core.modules.*;
import net.lightning.core.sidebar.PerPlayerSidebarDisplay;
import net.lightning.core.sidebar.SidebarCreator;
import net.lightning.core.sidebar.SidebarDisplay;
import net.lightning.core.sidebar.SidebarPane;
import net.lightning.core.team.TeamGiver;
import net.lightning.core.util.ItemBuilder;
import net.lightning.lobby.graphics.PlayMainGUI;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Getter
public class LobbyGame extends Game implements SidebarCreator {

    private Map<Rank, GameTeam> teamsByRank;

    private PlayMainGUI playMainGUI;

    public LobbyGame(LightningGamePlugin<LobbyGame> plugin) {
        super(plugin);
        init();
    }

    @Override
    protected SidebarDisplay initSidebarDisplay() {
        return new PerPlayerSidebarDisplay(this);
    }

    @Override
    protected void initModules() {
        LobbyMapModel map = (LobbyMapModel) this.map;
        map.spawn.setWorld(world);

        registerModule(WeatherModule.builder()
                .build());
        registerModule(AntiBuildModule.builder()
                .placeBreakCheck((player, block) -> ((LobbyPlayer) player).isGriefer())
                .build());

        registerModule(InventoryProtectionModule.builder()
                .denyAll()
                .dropCheck((player, item) -> false)
                .build());
        registerModule(ItemInteractionModule.builder()
                .slotAction(1, (gamePlayer, item) -> {
                    LobbyPlayer player = (LobbyPlayer) gamePlayer;
                    player.setHidePlayers(!player.isHidePlayers());
                    updateHotbarPlayersToggler(player);

                    if (player.isHidePlayers()) {
                        player.hideAll();
                        player.sendUnlocalizedMessage(ChatColor.GREEN + "Players have been hidden.");
                    }
                    else {
                        player.showAll();
                        player.sendUnlocalizedMessage(ChatColor.GRAY + "Players are no longer hiddden.");
                    }
                })
                .slotAction(4, (player, item) -> {
                    if (!(player.getOpenGUI() instanceof PlayMainGUI)) {
                        player.openGUI(playMainGUI);
                    }
                })
                .build());

        registerModule(LifeModule.builder()
                .makeInvincible()
                .build());
        registerModule(FoodRegulatorModule.builder()
                .infiniteFood(true)
                .build());

        registerModule(PlayerKillerModule.builder()
                .killOnJoin(false)
                .voidLimit(map.voidLimit)
                .teleportInstead(map.spawn)
                .build());

        registerModule(ChatModule.builder()
                .formatter(player -> {
                    NetworkPlayer networkPlayer = player.getNetworkPlayer();
                    Rank rank = networkPlayer.getRank();

                    String format = rank.getDisplayPrefix() + "%s" + ChatColor.DARK_GRAY + " ⤜ " + rank.getColor() + "%s";
                    if (networkPlayer.getPrestige() > 0) {
                        format = ChatColor.DARK_GRAY + "⦏" + networkPlayer.getPrestigeDisplay() + ChatColor.DARK_GRAY + "⦐ " + format;
                    }
                    return format;
                })
                .allowColors(Rank.HELPER)
                .build());
    }

    @Override
    protected TeamGiver initTeamGiver() {
        Map<Rank, GameTeam> teamMap = new HashMap<>();

        final Rank[] ranks = Rank.values();
        char currentChar = 'a';
        for (int i = ranks.length - 1; i >= 0; i--) {
            Rank rank = ranks[i];
            GameTeam.GameTeamBuilder teamBuilder = GameTeam.builder()
                    .name(currentChar++ + "-" + rank.getDisplayName().trim())
                    .color(ChatColor.valueOf(rank.getColor().name()));
            if (rank.isPrefix()) {
                teamBuilder.prefix(rank.getDisplayName() + " ");
            }

            GameTeam team = teamBuilder.build();
            registerTeam(team);
            teamMap.put(rank, team);
        }
        this.teamsByRank = teamMap;

        return (game, player) -> teamsByRank.get(player.getNetworkPlayer().getRank());
    }

    @Override
    protected void initListeners() {

    }

    @Override
    protected GameMapModel loadMapModel(GameMapLoader mapLoader) {
        return LobbyMapModel.builder()
                .maxPlayers((short) 50)
                .name("Lobby")
                .spawn(new ReadableLocation(null, 134.5, 122.01, 102.5))
                .voidLimit(70)
                .build();
    }

    @Override
    protected void postInit() {
        playMainGUI = new PlayMainGUI();
    }

    @Override
    public GamePlayer prepareJoin(Player nativePlayer, NetworkPlayer networkPlayer) {
        return new LobbyPlayer(this, nativePlayer, networkPlayer);
    }

    @Override
    public SidebarPane createPane(GamePlayer player) {
        SidebarPane pane = sidebarDisplay.createDefault(getTeams(), 9);
        pane.setDisplayName(ChatColor.LIGHT_PURPLE + "Lumaze " + ChatColor.GRAY + ChatColor.BOLD + "BETA");
        updateSidebar(pane, player);
        return pane;
    }

    public void updateSidebar(SidebarPane pane, GamePlayer player) {
        NetworkPlayer networkPlayer = player.getNetworkPlayer();
        Rank rank = networkPlayer.getRank();

        long requiredExp = networkPlayer.getRequiredPrestigeExp();
        renderLines(pane, Arrays.asList(
                "",
                ChatColor.WHITE + "Name: " + ChatColor.LIGHT_PURPLE + player.getName(),
                ChatColor.WHITE + "Rank: " + rank.getColor() + rank.getDisplayName(),
                "",
                ChatColor.WHITE + "Prestige: " + networkPlayer.getPrestigeDisplay(),
                ChatColor.DARK_GRAY + "» " + ChatColor.LIGHT_PURPLE + StringUtil.round(networkPlayer.getPrestigeExp() * 100. / requiredExp) + "% " +
                        ChatColor.DARK_GRAY + "[" + ChatColor.WHITE + StringUtil.humanReadableNumber(networkPlayer.getPrestigeExp())
                        + ChatColor.GRAY + "/" + StringUtil.humanReadableNumber(networkPlayer.getRequiredPrestigeExp()) + ChatColor.DARK_GRAY + "]",
                ChatColor.WHITE + "Coins: " + ChatColor.LIGHT_PURPLE + networkPlayer.getCoins() + " ❍",
                "",
                ChatColor.LIGHT_PURPLE + "play.lumaze.net"
        ));
    }

    @GameEventHandler
    public void onPlayerJoin(PlayerJoinGameEvent event) {
        LobbyPlayer player = (LobbyPlayer) event.getPlayer();
        for (GamePlayer otherPlayer : players.values()) {
            if (player == otherPlayer) {
                continue;
            }

            LobbyPlayer other = (LobbyPlayer) otherPlayer;
            if (other.isHidePlayers() && !other.canSee(player)) {
                other.hide(player);
            }
        }

        if (player.isHidePlayers()) {
            player.hideAll();
        }
    }

    @Override
    @GameEventHandler(priority = GameEventPriority.HIGH)
    public void onPlayerSpawnInternalHigh(PlayerSpawnGameEvent event) {
        LobbyMapModel map = (LobbyMapModel) this.map;
        LobbyPlayer player = (LobbyPlayer) event.getPlayer();
        Player nativePlayer = player.getNativePlayer();

        PlayerInventory playerInventory = nativePlayer.getInventory();
        playerInventory.clear();
        playerInventory.setItem(0, new ItemBuilder(SkullType.PLAYER)
                .setSkullOwner(nativePlayer.getName())
                .setDisplayName(ChatColor.LIGHT_PURPLE + "Your profile")
                .setLore(
                        ChatColor.GRAY + "This feature will be added soon."
                )
                .build());
        updateHotbarPlayersToggler(player);

        playerInventory.setItem(4, new ItemBuilder(Material.NETHER_STAR)
                .setDisplayName(ChatColor.LIGHT_PURPLE + "Play")
                .build());
        playerInventory.setHeldItemSlot(4);

        player.naiveTeleport(map.spawn);
        nativePlayer.setGameMode(GameMode.SURVIVAL);
        if (player.getNetworkPlayer().hasRankOrAbove(Rank.VIP)) {
            nativePlayer.setAllowFlight(true);
        }
    }

    private void updateHotbarPlayersToggler(LobbyPlayer lobbyPlayer) {
        lobbyPlayer.getNativePlayer().getInventory().setItem(1,
                new ItemBuilder(Material.INK_SACK)
                        .setColor(lobbyPlayer.isHidePlayers() ? DyeColor.GRAY : DyeColor.LIME)
                        .setDisplayName(lobbyPlayer.isHidePlayers() ? ChatColor.GREEN + "Show players" : ChatColor.GRAY + "Hide players")
                        .setLore(
                                ChatColor.GRAY + "Use this item to show/hide players",
                                ChatColor.GRAY + "in the lobby."
                        )
                        .build());
    }

}
