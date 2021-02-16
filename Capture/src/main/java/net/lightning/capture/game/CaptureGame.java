package net.lightning.capture.game;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.lightning.capture.Capture;
import net.lightning.capture.graphics.KitSelectorGUI;
import net.lightning.common.models.NetworkPlayer;
import net.lightning.core.Game;
import net.lightning.core.GamePlayer;
import net.lightning.core.GameState;
import net.lightning.core.GameTeam;
import net.lightning.core.event.GameEventHandler;
import net.lightning.core.event.player.PlayerJoinGameEvent;
import net.lightning.core.event.player.PlayerSpawnGameEvent;
import net.lightning.core.map.GameMapLoader;
import net.lightning.core.map.GameMapModel;
import net.lightning.core.modules.*;
import net.lightning.core.server.PlayerNPC;
import net.lightning.core.sidebar.PerPlayerSidebarDisplay;
import net.lightning.core.sidebar.SidebarCreator;
import net.lightning.core.sidebar.SidebarDisplay;
import net.lightning.core.sidebar.SidebarPane;
import net.lightning.core.stats.FightGameStats;
import net.lightning.core.util.ItemBuilder;
import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.util.*;

@Getter
public class CaptureGame extends Game implements SidebarCreator {

    private final Map<GameTeam, Accumulator> teamToAccumulator;
    private final Map<GameTeam, CaptureGameMapModel.TeamModel> teamToModel;

    private final Map<GameTeam, PlayerNPC> teamToKitNPC;

    public CaptureGame(Capture plugin) {
        super(plugin);
        this.teamToAccumulator = new HashMap<>();
        this.teamToModel = new HashMap<>();
        this.teamToKitNPC = new HashMap<>();
        super.init();
    }

    @Override
    protected SidebarDisplay initSidebarDisplay() {
        return new PerPlayerSidebarDisplay(this);
    }

    @Override
    protected void initTeams() {
        CaptureGameMapModel map = (CaptureGameMapModel) this.map;
        teamToModel.put(registerColorTeam(ChatColor.RED), map.teams.red);
        teamToModel.put(registerColorTeam(ChatColor.BLUE), map.teams.blue);
    }

    @Override
    protected void initModules() {
        CaptureGameMapModel map = (CaptureGameMapModel) this.map;

        registerModule(WeatherModule.builder()
                .build());
        registerModule(AntiBuildModule.builder()
                .checkGameState(true)
                .keepHistory(true)
                .cannotBreakUnplaced(true)
                .maxBuildHeight(map.heightLimit)
                .protectedZones(map.protectedCuboids)
                .placeBreakCheck((player, block) -> ((CaptureGamePlayer) player).getCurrentRoom() == CaptureRoom.OUTSIDE)
                .build());
        registerModule(PlayerKillerModule.builder()
                .voidLimit(map.killerLimit)
                .build());

        registerModule(LifeModule.builder()
                .fakeDeath(true)
                .noKeepNoDrop()
                .build());
        registerModule(FoodRegulatorModule.builder()
                .infiniteFood(true)
                .build());

        registerModule(InventoryProtectionModule.builder()
                .disableArmorClick(true)
                .dropCheck((player, item) -> item.getItemStack().getType().isBlock())
                .build());

        registerModule(TimeRespawnModule.builder()
                .teleportLocation(map.respawnLocation)
                .respawnAfter(5)
                .build());

        registerModule(ChatModule.builder()
                .build());
    }

    @Override
    protected void initListeners() {
        eventManager.registerNative(new CaptureGameListener(this));
    }

    @Override
    protected GameMapModel loadMapModel(GameMapLoader mapLoader) {
        return mapLoader.loadMapModel(new File(plugin.getDataFolder(), "maps/default.yml"), CaptureGameMapModel.class);
    }

    @Override
    protected void postInit() {
        for (GameTeam team : getTeams()) {
            CaptureGameMapModel.TeamModel model = teamToModel.get(team);
            teamToAccumulator.put(team, new Accumulator(world, model.accumulator,
                    new HashSet<>(model.energyStorage), new ArrayList<>(model.strikesLocations),
                    team, (short) 50));

            PlayerNPC npc = plugin.getCraftServerHandler().createNPC(world, UUID.randomUUID(), RandomStringUtils.random(8, true, true).toLowerCase());
            npc.setSkin("ewogICJ0aW1lc3RhbXAiIDogMTU5MzUxMDg5ODk4NiwKICAicHJvZmlsZUlkIiA6ICJiMDk3ZDJmM2U0NTE0OTUyYmQ1ZTFlMjIzMWEyZDU0MSIsCiAgInByb2ZpbGVOYW1lIiA6ICI0eGoiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWVlOTM0YjM1MTdhNmJkZWQwNzFhM2EzNTU0OGI2Y2EwMjJhN2FhNWI1NDExZDViNWFlMmY3NGUxNjFmYzc1YyIKICAgIH0KICB9Cn0=", "H/E5jj3VG8NG+WVKVacWJfRdjKgtq2qeTkKWYjkijvrnPRAhpMIbg6MIyDDEUpWNzBVlilv3vGZZ4t/46oid96cbhCptfKe+6zFUj6dCR2cROygS0tsu98KSuwfVR2ADbavaUIaj0OksnjJcclc+aHsU+OKQSIwLpEGZ08nfqeHEw12dJtPbkaK3V3jhdZO9sZzEP/QFTPbyWEprWyv7KROg+isR4dPvL84V+MWIjXjYbdIbh8qGmPEgOk9qrT4XsfQvmiyksc64tdvbH48/b0//1XVb79/PyAMAQaTwf5gbjQOpguUdlxBoB38zTX/RmSv7PGNrd340+zGd0sO90IP7SJWMFmTkbn8bZd+eAfh1DZCv3wFUyAfDB0sjmNsStm9I7jfIYLdbfcOaH00dnyMwZJtQNL7rOvhrfbI+y0dRhWMquAivyqD1hELxiWpZ1S+JaFjtBqV1CMUYmohtC3lWgL4e54Hiy7D22pV5VpJy6wk++3aswq45pTpLrYd8rGHlxsvjpVqJRyZ7SmYFqNolPEq5j3XxCltIXAvqWELlTJCGr0QnGjZ1ONwXOot8YFW+9Dh3i9+ayvGvFE2MCIiz9QBLU8xWrkrW/byiYkDp7rG8XPiJpwbjQ+ByK1SMEspdVsxXhxlAy/wK0IGIc0YpG3PaZXY+45w1AmDM9WE=");
            npc.setLocation(model.kitsNpc);
            npc.addClickListener(player -> {
                if (player.getTeam() != team) {
                    return;
                }

                KitSelectorGUI kitSelectorGUI = new KitSelectorGUI();
                kitSelectorGUI.update(((CaptureGamePlayer) player).getSelectedKit());
                player.openGUI(kitSelectorGUI);
            });
            npcManager.registerNPC(npc);
            teamToKitNPC.put(team, npc);
        }

        for (Accumulator accumulator : teamToAccumulator.values()) {
            for (Block powerUnit : accumulator.getEnergyStorage()) {
                accumulator.updateUnit(powerUnit, false);
            }
            for (int i = 0; i < 4; i++) {
                accumulator.randomlyUpdatePowerUnits(true);
            }
        }

        setGameState(GameState.PLAYING);
    }

    /*
    Game
     */

    @Override
    public GamePlayer prepareJoin(Player nativePlayer, NetworkPlayer networkPlayer) {
        return new CaptureGamePlayer(this, nativePlayer, networkPlayer);
    }

    @GameEventHandler
    public void onPlayerSpawn(PlayerSpawnGameEvent event) {
        CaptureGamePlayer player = (CaptureGamePlayer) event.getPlayer();
        CaptureGameMapModel.TeamModel teamModel = teamToModel.get(player.getTeam());
        player.naiveTeleport(teamModel.spawnLocation);
        player.setCurrentRoom(CaptureRoom.OUTSIDE);

        Player nativePlayer = player.getNativePlayer();
        PlayerInventory inventory = nativePlayer.getInventory();
        DyeColor teamDyeColor = player.getTeam().getColor() == ChatColor.RED ? DyeColor.RED : DyeColor.BLUE;

        ItemStack blocks = new ItemBuilder(Material.STAINED_CLAY, 64)
                .setColor(teamDyeColor)
                .build();
        switch (player.getSelectedKit()) {
            case SOLDIER:
                inventory.setItem(0, new ItemBuilder(Material.IRON_SWORD)
                        .makeUnbreakable()
                        .build());
                inventory.setItem(1, blocks);
                inventory.setItem(2, new ItemBuilder(Material.GOLD_PICKAXE)
                        .makeUnbreakable()
                        .build());
                inventory.setItem(3, new ItemStack(Material.GOLDEN_APPLE, 2));
                inventory.setItem(4, blocks);
                inventory.setItem(5, blocks);

                inventory.setChestplate(new ItemBuilder(Material.LEATHER_CHESTPLATE)
                        .setColor(teamDyeColor)
                        .makeUnbreakable()
                        .build());
                inventory.setLeggings(new ItemBuilder(Material.LEATHER_LEGGINGS)
                        .setColor(teamDyeColor)
                        .makeUnbreakable()
                        .build());
                inventory.setBoots(new ItemBuilder(Material.LEATHER_BOOTS)
                        .setColor(teamDyeColor)
                        .makeUnbreakable()
                        .build());
                break;
            case ARCHER:
                inventory.setItem(0, new ItemBuilder(Material.WOOD_SWORD)
                        .makeUnbreakable()
                        .build());
                inventory.setItem(1, blocks);
                inventory.setItem(2, new ItemBuilder(Material.GOLD_PICKAXE)
                        .makeUnbreakable()
                        .build());
                inventory.setItem(3, new ItemStack(Material.GOLDEN_APPLE, 2));
                inventory.setItem(6, blocks);
                inventory.setItem(5, blocks);

                inventory.setItem(4, new ItemBuilder(Material.BOW)
                        .makeUnbreakable()
                        .build());
                inventory.setItem(7, new ItemStack(Material.ARROW, 32));

                inventory.setChestplate(new ItemBuilder(Material.LEATHER_CHESTPLATE)
                        .setColor(teamDyeColor)
                        .makeUnbreakable()
                        .build());
                inventory.setLeggings(new ItemBuilder(Material.LEATHER_LEGGINGS)
                        .setColor(teamDyeColor)
                        .makeUnbreakable()
                        .build());
                inventory.setBoots(new ItemBuilder(Material.LEATHER_BOOTS)
                        .setColor(teamDyeColor)
                        .makeUnbreakable()
                        .build());
                break;
            case HEDGEHOG:
                inventory.setItem(1, blocks);
                inventory.setItem(2, new ItemBuilder(Material.WOOD_PICKAXE)
                        .makeUnbreakable()
                        .build());
                inventory.setItem(3, new ItemStack(Material.GOLDEN_APPLE, 1));
                inventory.setItem(4, blocks);
                inventory.setItem(5, blocks);

                inventory.setChestplate(new ItemBuilder(Material.CHAINMAIL_CHESTPLATE)
                        .addEnchantment(Enchantment.THORNS, 1)
                        .makeUnbreakable()
                        .build());
                inventory.setBoots(new ItemBuilder(Material.LEATHER_BOOTS)
                        .setColor(teamDyeColor)
                        .makeUnbreakable()
                        .build());

                nativePlayer.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60 * 60 * 20, 0, false, false));
                break;
        }

        for (ItemStack itemStack : inventory) {
            if (itemStack == null) {
                continue;
            }

            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.spigot().setUnbreakable(true);
            itemStack.setItemMeta(itemMeta);
        }
    }

    @GameEventHandler
    public void onPlayerJoin(PlayerJoinGameEvent event) {
        CaptureGamePlayer player = (CaptureGamePlayer) event.getPlayer();
        player.sendUnlocalizedMessage(ChatColor.WHITE + "You are now using the kit " + ChatColor.YELLOW + player.getSelectedKit().getName() + ChatColor.WHITE + ".");
    }

    @GameEventHandler
    public void onPlayerMoveRoom(PlayerMoveRoomGameEvent event) {
        CaptureGamePlayer player = event.getPlayer();
        Player nativePlayer = player.getNativePlayer();

        if (event.getTo() == CaptureRoom.ROOF) {
            PlayerNPC kitNpc = teamToKitNPC.get(player.getTeam());
            kitNpc.spawn(nativePlayer);
            Bukkit.getScheduler().runTaskLater(plugin, () -> kitNpc.hideFromTabList(nativePlayer), 40);
        }
    }

    public boolean stealPower(GameTeam thief, GameTeam fromTeam, short power) {
        Accumulator thiefAccumulator = teamToAccumulator.get(thief);
        Accumulator victimAccumulator = teamToAccumulator.get(fromTeam);

        if (victimAccumulator.getPower() - power >= 0 && thiefAccumulator.getPower() + power <= 100) {
            victimAccumulator.setPower((short) (victimAccumulator.getPower() - power));
            thiefAccumulator.setPower((short) (thiefAccumulator.getPower() + power));
            updatePowerInformation();
            return true;
        }
        return false;
    }

    public void win(Accumulator accumulator) {
        TimeRespawnModule timeRespawnModule = getModule(TimeRespawnModule.class);
        timeRespawnModule.getRespawning().forEach((player, task) -> task.cancel());

        CaptureGameMapModel map = (CaptureGameMapModel) this.map;
        CaptureGameMapModel.TeamModel model = teamToModel.get(accumulator.getTeam());

        GameTeam winningTeam = accumulator.getTeam();
        Map<GamePlayer, EndRewards> endRewardsMap = new HashMap<>();
        for (GamePlayer player : players.values()) {
            Player nativePlayer = player.getNativePlayer();
            nativePlayer.setGameMode(GameMode.SPECTATOR);
            nativePlayer.setFlySpeed(-1);
            player.hideAll();
            player.naiveTeleport(model.endSceneStart);

            FightGameStats stats = (FightGameStats) player.getGameStats();

            int coins = 20;
            int prestigeExp = (player.getTeam() == winningTeam ? 30 : 10) + stats.getKills() * 7 + (stats.getDeaths() / 2);

            endRewardsMap.put(player, new EndRewards(coins, prestigeExp));
        }

        Collection<GamePlayer> gamePlayers = players.values();
        batchCacheExecute(gamePlayers, player -> {
            NetworkPlayer networkPlayer = player.getNetworkPlayer();
            EndRewards rewards = endRewardsMap.get(player);
            if (rewards == null) {
                return;
            }

            networkPlayer.setCoins(networkPlayer.getCoins() + rewards.coins);
            networkPlayer.setPrestigeExp(networkPlayer.getPrestigeExp() + rewards.prestigeExp);
        });

        EndingScene endingScene = new EndingScene(accumulator, map.sceneRadius, map.sceneHeight);
        endingScene.play(gamePlayers, 8, players -> {
            for (GamePlayer player : players) {
                player.sendTitle(winningTeam.getColor() + winningTeam.getName() + " team won!", null, 10, 5 * 20, 10);

                Player nativePlayer = player.getNativePlayer();
                nativePlayer.setFlySpeed(0.1f);
                player.showAll();

                EndRewards rewards = endRewardsMap.get(player);
                if (rewards == null) {
                    continue;
                }

                player.sendUnlocalizedMessage(" ");
                player.sendUnlocalizedMessage(ChatColor.WHITE + "You earned " + ChatColor.LIGHT_PURPLE + rewards.coins + " " + coinsDisplay
                        + ChatColor.WHITE + " and " + ChatColor.AQUA + rewards.prestigeExp + " PXP" + ChatColor.WHITE + ".");
                player.sendUnlocalizedMessage(" ");
                player.playSound(Sound.ORB_PICKUP);
            }
        });
    }

    @RequiredArgsConstructor
    private static class EndRewards {

        private final int coins, prestigeExp;

    }

    /*
    Sidebar
     */

    @Override
    public SidebarPane createPane(GamePlayer player) {
        SidebarPane pane = sidebarDisplay.createDefault(getScoreboardTeams(), 7)
                .setDisplayName(ChatColor.YELLOW + "Lightning Capture");
        pane.setLine(0, "");

        int lineOffset = 0;
        for (GameTeam team : getTeams()) {
            String teamName = team.getName().substring(0, 1).toUpperCase() + team.getName().substring(1).toLowerCase();
            pane.setLine(lineOffset + 1, team.getColor() + teamName + ChatColor.GRAY + (player.getTeam() == team ? " (YOU)" : ""));
            pane.setLine(lineOffset + 2, "Power: " + ChatColor.YELLOW + teamToAccumulator.get(team).getPower() + "%");
            pane.setLine(lineOffset + 3, "");
            lineOffset += 3;
        }
        return pane;
    }

    public void updatePowerInformation() {
        int lineOffset = 0;
        for (GameTeam team : getTeams()) {
            sidebarDisplay.setLine(lineOffset + 2, "Power: " + ChatColor.YELLOW + teamToAccumulator.get(team).getPower() + "%");
            lineOffset += 3;
        }
    }

}
