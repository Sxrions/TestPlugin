package dev.jojo.plugin.duel;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.jojo.plugin.TestPlugin;
import dev.jojo.plugin.arena.Arena;
import dev.jojo.plugin.arena.ArenaManager;
import dev.jojo.plugin.kit.KitManager;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class DuelManager {
    private static DuelManager instance;
    private JavaPlugin plugin = TestPlugin.getPluginInstance();
    private final Map<PlayerRef, Duel> playerDuelMap;
    private final ArenaManager arenaManager;
    private final Map<String, Queue<PlayerRef>> queues;
    private final Map<PlayerRef, String> playerKitQueueMap;

    private DuelManager() {
        this.playerDuelMap = new HashMap<>();
        KitManager kitManager = KitManager.getInstance();
        this.arenaManager = ArenaManager.getInstance();
        kitManager.loadKits();
        this.arenaManager.loadArenas();

        plugin.getEventRegistry().register(PlayerDisconnectEvent.class, this::handlePlayerDisconnect);

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

    public void addToQueue(PlayerRef playerRef, String kit) {
        queues.get(kit).add(playerRef);
        playerKitQueueMap.put(playerRef, kit);
        matchMake(kit);
    }

    public void removeFromQueue(PlayerRef playerRef) {
        if (playerKitQueueMap.containsKey(playerRef)) {
            String kit = playerKitQueueMap.remove(playerRef);
            queues.get(kit).remove(playerRef);
        }
    }

    public boolean matchMake(String kit) {
        Arena randomArena = arenaManager.getRandomFreeArena();
        if (queues.get(kit).size() >= 2 && randomArena != null) {
            PlayerRef p1 = queues.get(kit).remove();
            PlayerRef p2 = queues.get(kit).remove();
            Duel duel = new Duel(p1, p2, randomArena, kit);
            playerDuelMap.put(p1, duel);
            playerDuelMap.put(p2, duel);
            duel.startCountdown();
            return true;
        }

        return false;
    }

    //DEBUG
    public String getQueue(String kitName) {
        String result = "";
        int cpt = 1;
        if (queues.containsKey(kitName)) {
            for (PlayerRef playerRef : queues.get(kitName)) {
                result += "1 : " + playerRef.getUsername() + " ";
                cpt++;
            }
        }
        return result;
    }

    public void handlePlayerDisconnect(PlayerDisconnectEvent evt) {
        if (evt == null) return;
        PlayerRef disconnected = evt.getPlayerRef();
        removeFromQueue(disconnected);
        Duel duel;
        synchronized (playerDuelMap) {
            duel = playerDuelMap.remove(disconnected);
            if (duel != null) {
                PlayerRef p1 = duel.getPlayerRef1();
                PlayerRef p2 = duel.getPlayerRef2();
                if (p1 != null) playerDuelMap.remove(p1);
                if (p2 != null) playerDuelMap.remove(p2);
            }
        }

        if (duel == null) {
            return;
        }
        duel.cancelTasks();
        duel.end(disconnected);

        matchMake(duel.getKitName());
    }

    public void handlePlayerDeath(UUID uuid) {

        PlayerRef playerRef = Universe.get().getPlayer(uuid);
        if (playerRef==null){return;}

        if (playerDuelMap.containsKey(playerRef)) {
            Ref<EntityStore> playerRefRef = playerRef.getReference();
            Store<EntityStore> store = playerRefRef.getStore();
            World world = store.getExternalData().getWorld();
            Duel duel = playerDuelMap.get(playerRef);
            PlayerRef player1Ref = duel.getPlayerRef1();
            PlayerRef player2Ref = duel.getPlayerRef2();

            world.execute(() -> {
                duel.end(playerRef);
                playerDuelMap.remove(player1Ref);
                playerDuelMap.remove(player2Ref);
            });

            /*HytaleServer.SCHEDULED_EXECUTOR.schedule(() -> {

            }, 2000, TimeUnit.MILLISECONDS);*/
            matchMake(duel.getKitName());
        }
    }

}
