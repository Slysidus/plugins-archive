package net.lightning.core;

import lombok.Getter;
import net.lightning.core.event.npc.NPCRightClickEvent;
import net.lightning.core.server.CraftServerHandler;
import net.lightning.core.server.PlayerNPC;
import net.lightning.core.server.channel.PacketReader;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class NPCManager implements PacketReader {

    private final Game game;
    private final CraftServerHandler craftServerHandler;

    @Getter
    private final ConcurrentMap<Integer, PlayerNPC> npcMap;

    public NPCManager(Game game) throws ClassNotFoundException {
        this.game = game;
        this.npcMap = new ConcurrentHashMap<>();

        CraftServerHandler craftServerHandler = game.getPlugin().getCraftServerHandler();
        this.craftServerHandler = craftServerHandler;

        craftServerHandler.getPacketReaderInjector().registerReader(
                craftServerHandler.getNMSClass("PacketPlayInUseEntity"), this);
    }

    @Override
    public void read(Player bukkitPlayer, Object packet) {
        try {
            final String action = craftServerHandler.getFieldValue(packet, "action").toString();
            if (!action.equals("INTERACT")) {
                return;
            }

            final int entityId = (int) craftServerHandler.getFieldValue(packet, "a");
            if (npcMap.containsKey(entityId)) {
                Bukkit.getScheduler().runTask(game.getPlugin(), () -> {
                    PlayerNPC clickedNPC = npcMap.get(entityId);
                    NPCRightClickEvent clickEvent = new NPCRightClickEvent(game, clickedNPC);
                    game.getEventManager().fireEvent(clickEvent);
                    if (!clickEvent.isCancelled()) {
                        clickedNPC.getClickListeners().accept(game.getPlayer(bukkitPlayer));
                    }
                });
            }
        }
        catch (NoSuchFieldException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
    }

    public void registerNPC(PlayerNPC npc) {
        npcMap.put(npc.getId(), npc);
    }

    public Collection<PlayerNPC> getNPCs() {
        return npcMap.values();
    }

    public void spawnAllNPCs(Player bukkitPlayer) {
        for (PlayerNPC npc : getNPCs()) {
            npc.spawn(bukkitPlayer);
        }
    }

}
