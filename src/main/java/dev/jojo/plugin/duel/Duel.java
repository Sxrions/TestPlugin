package dev.jojo.plugin.duel;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import com.hypixel.hytale.server.core.util.UUIDUtil;
import dev.jojo.plugin.arena.Arena;
import dev.jojo.plugin.kit.KitManager;

import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Duel {
    private PlayerRef playerRef1;
    private PlayerRef playerRef2;
    private Arena arena;
    private DuelState state;
    private String kitName;

    private int countdown = 5;
    private int timer = 120;

    private ScheduledFuture<?> taskCountdown;
    private ScheduledFuture<?> taskTimer;

    public Duel(PlayerRef p1, PlayerRef p2, Arena arena, String kitName){
        this.playerRef1 = p1;
        this.playerRef2 = p2;
        this.arena = arena;
        this.kitName = kitName;

        this.arena.setOccupied(true); //OCCUPATION DE L'ARENE DES LA CREATION DU DUEL
    }

    public void startCountdown(){ //TODO bloquer les déplacements des joueurs (ou set speed 0 jsp comment)
        teleportPlayer(playerRef1, arena.getWorld());
        teleportPlayer(playerRef2, arena.getWorld());
        this.state = DuelState.STARTING;
        taskCountdown = HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(() -> {
            if (countdown<=0){
                taskCountdown.cancel(false);
                start();
                return;
            }
            arena.getWorld().sendMessage(Message.raw("" + countdown));
            countdown--;
        },0,1,TimeUnit.SECONDS);
    }

    private void start(){
        KitManager kitManager = KitManager.getInstance();
        kitManager.applyKit(playerRef1,kitName);
        kitManager.applyKit(playerRef2,kitName);
        //TODO MODIFIER ETATS JOUEURS jsp encore ce que je veux dire par la
        taskTimer = HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(() -> {
            if (timer<=0){
                taskTimer.cancel(false);
                endDraw();
                return;
            }
            timer--;
        },0,1,TimeUnit.SECONDS);

        this.state = DuelState.PLAYING;
    }

    public void end(PlayerRef looser){
        this.state = DuelState.ENDING;

        // Defensive: if arena world missing, avoid NPE
        World arenaWorld = arena == null ? null : arena.getWorld();
        if (arenaWorld == null) {
            // fallback: perform minimal cleanup
            end();
            return;
        }

        arenaWorld.execute(() -> {
            try {
                // Defensive retrieval of component instances: check references before using
                Ref<EntityStore> ref1 = (playerRef1 == null) ? null : playerRef1.getReference();
                Ref<EntityStore> ref2 = (playerRef2 == null) ? null : playerRef2.getReference();

                PlayerRef pref1 = null;
                PlayerRef pref2 = null;
                try {
                    if (ref1 != null) pref1 = arenaWorld.getEntityStore().getStore().getComponent(ref1, PlayerRef.getComponentType());
                } catch (Throwable ignored) {
                }
                try {
                    if (ref2 != null) pref2 = arenaWorld.getEntityStore().getStore().getComponent(ref2, PlayerRef.getComponentType());
                } catch (Throwable ignored) {
                }

                if (looser != null && pref1 != null && pref2 != null) {
                    if (looser.getUuid().equals(playerRef1.getUuid())){
                        EventTitleUtil.showEventTitleToPlayer(pref2,Message.raw("VICTOIRE"),Message.raw(playerRef1.getUsername() + " est nul"), true);
                        EventTitleUtil.showEventTitleToPlayer(pref1,Message.raw("DÉFAITE"),Message.raw(playerRef2.getUsername() + " est supérieur"), true);
                    } else {
                        EventTitleUtil.showEventTitleToPlayer(pref1,Message.raw("VICTOIRE"),Message.raw(playerRef2.getUsername() + " est nul"), true);
                        EventTitleUtil.showEventTitleToPlayer(pref2,Message.raw("DÉFAITE"),Message.raw(playerRef1.getUsername() + " est supérieur"), true);
                    }
                }
            } catch (Throwable t){
                System.out.println("Duel.end(PlayerRef) : erreur lors du cleanup: " + t.getMessage());
            } finally {
                // Always attempt higher-level cleanup
                try { end(); } catch (Throwable ignored) {}
            }
        });
    }

    public void endDraw(){
        this.state = DuelState.ENDING;
        // Defensive: try to get a valid store; if not available, show title to world without store
        try {
            Ref<EntityStore> ref = (playerRef1 == null) ? null : playerRef1.getReference();
            if (ref != null) {
                try {
                    Store<EntityStore> store = ref.getStore();
                    EventTitleUtil.showEventTitleToWorld(Message.raw("DRAW !"), Message.raw("You are all guez"), true, null, 1,1,1, store);
                } catch (Throwable ignored) {
                    // fallback if store unavailable
                    EventTitleUtil.showEventTitleToWorld(Message.raw("DRAW !"), Message.raw("You are all guez"), true, null, 1,1,1, null);
                }
            } else {
                EventTitleUtil.showEventTitleToWorld(Message.raw("DRAW !"), Message.raw("You are all guez"), true, null, 1,1,1, null);
            }
        } catch (Throwable t){
            System.out.println("Duel.endDraw: erreur lors de l'envoi des titres: " + t.getMessage());
        } finally {
            end();
        }
    }

    public void end(){
        if (taskTimer != null) {
            try { taskTimer.cancel(false); } catch (Throwable ignored) {}
            taskTimer = null;
        }
        if (taskCountdown != null) {
            try { taskCountdown.cancel(false); } catch (Throwable ignored) {}
            taskCountdown = null;
        }

        Player p1 = arena.getWorld().getEntityStore().getStore().getComponent(playerRef1.getReference(), Player.getComponentType());
        Player p2 = arena.getWorld().getEntityStore().getStore().getComponent(playerRef2.getReference(), Player.getComponentType());
        p1.getInventory().clear();
        p2.getInventory().clear();

        heal(playerRef1.getReference());
        heal(playerRef2.getReference());


        //TODO ICI QUE VIENT LE KICK
        teleportLobby(playerRef1.getUuid()); //ptetre stocker les uuid avant on verra
        teleportLobby(playerRef2.getUuid());

        //TODO RESET LES JOUEURS, RESET l'arene
        //TODO PAS OUBLIER DE VIRER DUEL DE DUELMANAGER
        try {
            if (arena != null) arena.setOccupied(false);
        } catch (Throwable ignored) {}
    }

    enum DuelState{
        STARTING,
        PLAYING,
        ENDING
    }

    private void teleportPlayer(PlayerRef playerRef, World dest){
        if (playerRef == null) return;

        // Stop retrying if player no longer exists in Universe
        if (Universe.get().getPlayer(playerRef.getUuid()) == null) return;

        Ref<EntityStore> initialRef = null;
        try {
            initialRef = playerRef.getReference();
        } catch (Throwable ignored) {}

        if (initialRef == null){
            // retry only while player still exists
            if (Universe.get().getPlayer(playerRef.getUuid()) != null) {
                System.out.println("ref pour tp nulle, retrying...");
                HytaleServer.SCHEDULED_EXECUTOR.schedule(() -> teleportPlayer(playerRef, dest), 100, TimeUnit.MILLISECONDS);
            }
            return;
        }

        Store<EntityStore> store;
        try { store = initialRef.getStore(); } catch (Throwable t) {
            if (Universe.get().getPlayer(playerRef.getUuid()) != null) {
                HytaleServer.SCHEDULED_EXECUTOR.schedule(() -> teleportPlayer(playerRef, dest), 150, TimeUnit.MILLISECONDS);
            }
            return;
        }
        World world = store == null ? null : store.getExternalData().getWorld();
        if (world == null) return;

        world.execute(() -> {
            Ref<EntityStore> ref = null;
            try { ref = playerRef.getReference(); } catch (Throwable ignored) {}
            if (ref == null){
                if (Universe.get().getPlayer(playerRef.getUuid()) != null) {
                    HytaleServer.SCHEDULED_EXECUTOR.schedule(() -> {
                        System.out.println("Ref is null for tp, retrying...");
                        teleportPlayer(playerRef, dest);
                    },100,TimeUnit.MILLISECONDS);
                }
                return;
            }

            try {
                Store<EntityStore> s = ref.getStore();
                DeathComponent death = null;
                try {
                    death = s.getComponent(ref, DeathComponent.getComponentType());
                } catch (Throwable ignored) {}
                if (death != null) {
                    if (Universe.get().getPlayer(playerRef.getUuid()) != null) {
                        System.out.println("Player is dead, will retry teleport shortly");
                        HytaleServer.SCHEDULED_EXECUTOR.schedule(() -> teleportPlayer(playerRef, dest), 300, TimeUnit.MILLISECONDS);
                    }
                    return;
                }

                Teleport teleport;
                if (playerRef.equals(playerRef1)){
                    teleport = new Teleport(dest, new Vector3d(arena.getSpawn1()[0],arena.getSpawn1()[1],arena.getSpawn1()[2]), new Vector3f(0,0,0));
                }else{
                    teleport = new Teleport(dest, new Vector3d(arena.getSpawn2()[0],arena.getSpawn2()[1],arena.getSpawn2()[2]), new Vector3f(0,0,0));
                }
                try {
                    s.addComponent(ref, Teleport.getComponentType(), teleport);
                } catch (Throwable t) {
                    System.out.println("teleportPlayer: addComponent failed: " + t.getMessage());
                }
            } catch (Throwable t){
                // defensive: avoid crashing the world thread
                System.out.println("teleportPlayer: unexpected error: " + t.getMessage());
            }
        });
    }

    public void teleportLobby(UUID uuid){
        // Use the arena world thread to perform world-safe operations but still check for nulls
        World world = (arena == null) ? null : arena.getWorld();
        if (world == null) return;

        world.execute(() -> {
            try {
                PlayerRef playerRef = Universe.get().getPlayer(uuid);
                if (playerRef == null) {
                    // do not retry if player disappeared
                    System.out.println("teleportLobby: playerRef is null, aborting");
                    return;
                }
                Ref<EntityStore> ref = null;
                try { ref = playerRef.getReference(); } catch (Throwable ignored) {}
                if (ref == null){
                    if (Universe.get().getPlayer(uuid) != null) {
                        System.out.println("MAIS PTN REF = null, on retry");
                        HytaleServer.SCHEDULED_EXECUTOR.schedule(() -> {
                            System.out.println("Ref is null for tp, retrying...");
                            teleportLobby(uuid);
                        },200,TimeUnit.MILLISECONDS);
                    } else {
                        System.out.println("teleportLobby: ref null and player gone, aborting");
                    }
                    return;
                }
                Store<EntityStore> store = null;
                try { store = ref.getStore(); } catch (Throwable t) {
                    System.out.println("teleportLobby: getStore failed: " + t.getMessage());
                    if (Universe.get().getPlayer(uuid) != null) {
                        HytaleServer.SCHEDULED_EXECUTOR.schedule(() -> teleportLobby(uuid),200,TimeUnit.MILLISECONDS);
                    }
                    return;
                }
                if (store.getComponent(ref,DeathComponent.getComponentType())!=null){
                    HytaleServer.SCHEDULED_EXECUTOR.schedule(() -> teleportLobby(uuid),200,TimeUnit.MILLISECONDS);
                    return;
                }

                Teleport teleport = new Teleport(Universe.get().getWorld("lobby"), Universe.get().getWorld("lobby").getWorldConfig().getSpawnProvider().getSpawnPoint(Universe.get().getWorld("lobby"), uuid ).getPosition(), new Vector3f());
                try { store.addComponent(ref, Teleport.getComponentType(), teleport); } catch (Throwable t) { System.out.println("teleportLobby addComponent failed: " + t.getMessage()); }
            } catch (Throwable t) {
                System.out.println("teleportLobby: erreur inattendue: " + t.getMessage());
            }
        });
    }

    // Expose cancellation to be called from DuelManager on disconnect
    public void cancelTasks(){
        if (taskTimer != null) {
            try { taskTimer.cancel(false); } catch (Throwable ignored) {}
            taskTimer = null;
        }
        if (taskCountdown != null) {
            try { taskCountdown.cancel(false); } catch (Throwable ignored) {}
            taskCountdown = null;
        }
    }

    public PlayerRef getPlayerRef1() {
        return playerRef1;
    }

    public PlayerRef getPlayerRef2() {
        return playerRef2;
    }

    public String getKitName() {
        return kitName;
    }

    public void heal(Ref<EntityStore> ref){
        if (ref!=null){
            ref.getStore().getExternalData().getWorld().execute(() -> {
                Store<EntityStore> store = ref.getStore();
                EntityStatMap stats = store.getComponent(ref,EntityStatMap.getComponentType());
                int healthIndex = DefaultEntityStatTypes.getHealth();
                stats.maximizeStatValue(healthIndex);
            });
        }
    }
}
