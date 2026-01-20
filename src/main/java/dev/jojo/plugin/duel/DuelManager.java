package dev.jojo.plugin.duel;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import dev.jojo.plugin.TestPlugin;
import dev.jojo.plugin.arena.Arena;
import dev.jojo.plugin.arena.ArenaManager;
import dev.jojo.plugin.kit.KitManager;

import java.util.*;

public class DuelManager {
    private static DuelManager instance;
    private JavaPlugin plugin = TestPlugin.getPluginInstance();
    private final Map<Player, Duel> playerDuelMap;
    private final Collection<Duel> duels;
    private final ArenaManager arenaManager;
    private final Map<String, Queue<Player>> queues;
    private final Map<Player, String> playerKitQueueMap;

    private DuelManager() {
        this.playerDuelMap = new HashMap<>();
        this.duels = new HashSet<>();
        KitManager kitManager = KitManager.getInstance();
        this.arenaManager = ArenaManager.getInstance();
        kitManager.loadKits();
        this.arenaManager.loadArenas();

        this.queues = new HashMap<>();
        for (String kit : kitManager.getKits().keySet()) {
            queues.put(kit, new ArrayDeque<>());
        }

        this.playerKitQueueMap = new HashMap<>();
    }

    public static DuelManager getInstance() {
        if (instance == null) {
            instance = new DuelManager();
        }
        return instance;
    }

    public void addToQueue(Player player, String kit) {
        queues.get(kit).add(player);
        playerKitQueueMap.put(player, kit);
        matchMake(kit);
    }

    public void removeFromQueue(Player player) {
        if (playerKitQueueMap.containsKey(player)) {
            String kit = playerKitQueueMap.remove(player);
            queues.get(kit).remove(player);
        }
    }

    public boolean matchMake(String kit) {
        Arena randomArena = arenaManager.getRandomFreeArena();
        if (queues.get(kit).size() >= 2 && randomArena != null) {
            Player p1 = queues.get(kit).remove();
            Player p2 = queues.get(kit).remove();
            Duel duel = new Duel(p1,p2,randomArena,kit);
            playerDuelMap.put(p1,duel);
            playerDuelMap.put(p2,duel);
            duels.add(duel);
            duel.startCountdown();
            return true;
        }

        return false;
    }

    //DEBUG
    public String getQueue(String kitName){
        String result = "";
        int cpt = 1;
        if (queues.containsKey(kitName)){
            for (Player player : queues.get(kitName)) {
                result += "1 : " + player.getDisplayName() + " ";
                cpt++;
            }
        }
        return result;
    }
}
