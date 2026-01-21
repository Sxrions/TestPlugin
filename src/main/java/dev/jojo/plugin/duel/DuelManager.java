package dev.jojo.plugin.duel;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.ISystem;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.jojo.plugin.TestPlugin;
import dev.jojo.plugin.arena.Arena;
import dev.jojo.plugin.arena.ArenaManager;
import dev.jojo.plugin.deathsystem.DuelDeath;
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
        plugin.getEntityStoreRegistry().registerSystem((ISystem<EntityStore>)new DuelDeath());


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
            Duel duel = new Duel(p1,p2,randomArena,kit);
            playerDuelMap.put(p1,duel);
            playerDuelMap.put(p2,duel);
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
            for (PlayerRef playerRef : queues.get(kitName)) {
                result += "1 : " + playerRef.getUsername() + " ";
                cpt++;
            }
        }
        return result;
    }

    public void handlePlayerDisconnect(PlayerDisconnectEvent evt){
        Store<EntityStore> store = evt.getPlayerRef().getReference().getStore();
        World world = store.getExternalData().getWorld();
        world.execute(() -> {
            PlayerRef disconnectedPlayer = evt.getPlayerRef();
            if (playerDuelMap.containsKey(disconnectedPlayer)){
                Duel duel = playerDuelMap.get(disconnectedPlayer);
                PlayerRef player1 = duel.getPlayerRef1();
                System.out.println(player1.getUsername());
                PlayerRef player2 = duel.getPlayerRef2();
                System.out.println(player2.getUsername());
                duel.end(disconnectedPlayer);
                playerDuelMap.remove(player1);
                playerDuelMap.remove(player2);
            }
        });
    }

    public void handlePlayerDeath(PlayerRef deadPlayerRef){
        System.out.println("handlePlayerDeath appelé depuis thread: " + Thread.currentThread().getName());
        System.out.println("Dead player UUID: " + deadPlayerRef.getUuid());
        System.out.println("PlayerDuelMap size: " + playerDuelMap.size());

        for (PlayerRef key : playerDuelMap.keySet()) {
            System.out.println("Map contient UUID: " + key.getUuid() + " - Username: " + key.getUsername());
        }

        Store<EntityStore> store = deadPlayerRef.getReference().getStore();
        World world = store.getExternalData().getWorld();
        System.out.println("Monde du joueur mort: " + world.getName());

        if (playerDuelMap.containsKey(deadPlayerRef)){
            System.out.println("TROUVÉ DANS LA MAP !");
            Duel duel = playerDuelMap.get(deadPlayerRef);
            PlayerRef player1Ref = duel.getPlayerRef1();
            PlayerRef player2Ref = duel.getPlayerRef2();

            System.out.println(deadPlayerRef.getUsername());
            System.out.println("ALED ALED");

            // ✅ Ajoute un log ICI
            System.out.println("Avant schedule de end()");

            HytaleServer.SCHEDULED_EXECUTOR.schedule(() -> {
                System.out.println("=== DANS LE SCHEDULE ==="); // ← Ce log devrait s'afficher
                System.out.println("Appel de duel.end()");
                // Important: run the end logic on the world's thread to avoid modifying the Store from a system/global thread
                world.execute(() -> {
                    duel.end(deadPlayerRef);
                    System.out.println("Après duel.end()");
                    playerDuelMap.remove(player1Ref);
                    playerDuelMap.remove(player2Ref);
                    System.out.println("Joueurs retirés de la map");
                });
            }, 1000, TimeUnit.MILLISECONDS);

            System.out.println("Après schedule de end()");
        }
    }

}
