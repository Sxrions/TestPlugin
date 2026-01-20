package dev.jojo.plugin.duel;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import dev.jojo.plugin.arena.ArenaManager;
import dev.jojo.plugin.kit.KitManager;

import java.util.*;

public class DuelManager {
    private static DuelManager instance;
    private JavaPlugin plugin;
    private final Map<PlayerRef, Duel> playerRefDuelMap;
    private final Collection<Duel> duels;
    private final ArenaManager arenaManager;
    private final KitManager kitManager;
    private final Map<String, Queue<Player>> queues;
    private final Map<Player, String> playerKitQueueMap;

    private DuelManager(JavaPlugin plugin) {
        this.playerRefDuelMap = new HashMap<>();
        this.duels = new HashSet<>();
        this.plugin = plugin;
        this.kitManager = KitManager.getInstance(plugin);
        this.arenaManager = ArenaManager.getInstance(plugin);
        this.kitManager.loadKits();
        this.arenaManager.loadArenas();

        this.queues = new HashMap<>();
        for (String kit : kitManager.getKits().keySet()) {
            queues.put(kit, new ArrayDeque<>());
        }

        this.playerKitQueueMap = new HashMap<>();
    }

    public static DuelManager getInstance(JavaPlugin plugin) {
        if (instance == null) {
            instance = new DuelManager(plugin);
        }
        return instance;
    }

    public KitManager getKitManager() {
        return kitManager;
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public void addToQueue(Player player, String kit) {
        queues.get(kit).add(player);
        playerKitQueueMap.put(player, kit);
    }

    public void removeFromQueue(Player player) {
        if (playerKitQueueMap.containsKey(player)) {
            String kit = playerKitQueueMap.remove(player);
            queues.get(kit).remove(player);
        }
    }

    public boolean matchMake(String kit) {
        if (queues.get(kit).size() >= 2) { //TODO && arena available
            //TODO duel start, remove from queue

            return true;
        }

        return false;
    }
}
