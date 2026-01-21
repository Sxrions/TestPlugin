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
        cancelTasks();

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
        if (Universe.get().getPlayer(playerRef.getUuid()) == null) return;

        Ref<EntityStore> ref = playerRef.getReference();

        if (ref == null){
            if (Universe.get().getPlayer(playerRef.getUuid()) != null) {
                System.out.println("ref pour tp nulle, retrying...");
                HytaleServer.SCHEDULED_EXECUTOR.schedule(() -> teleportPlayer(playerRef, dest), 500, TimeUnit.MILLISECONDS);
            }
            return;
        }
        Store<EntityStore> store = ref.getStore();

        World world = store.getExternalData().getWorld();

        world.execute(() -> {
            Teleport teleport;
            if (playerRef.equals(playerRef1)){
                teleport = new Teleport(dest, new Vector3d(arena.getSpawn1()[0],arena.getSpawn1()[1],arena.getSpawn1()[2]), new Vector3f(0,0,0));
            }else{
                teleport = new Teleport(dest, new Vector3d(arena.getSpawn2()[0],arena.getSpawn2()[1],arena.getSpawn2()[2]), new Vector3f(0,0,0));
            }
            store.addComponent(ref, Teleport.getComponentType(), teleport);
        });
    }

    public void teleportLobby(UUID uuid){
        World world = arena.getWorld();
        world.execute(() -> {
            PlayerRef playerRef = Universe.get().getPlayer(uuid);
            if (playerRef == null) {
                System.out.println("playerRef nul a partir de uuid");
                return;
            }
            Ref<EntityStore> ref = playerRef.getReference();
            if (ref == null){
                if (Universe.get().getPlayer(uuid) != null) {
                    System.out.println("ref nulle, retrying tp to lobby");
                    HytaleServer.SCHEDULED_EXECUTOR.schedule(() -> {teleportLobby(uuid);},500,TimeUnit.MILLISECONDS);
                }
                return;
            }
            Store<EntityStore> store = ref.getStore();
            World lobby = Universe.get().getWorld("lobby");
            Vector3d position = lobby.getWorldConfig().getSpawnProvider().getSpawnPoint(lobby, uuid).getPosition();
            Teleport teleport = new Teleport(lobby, position, new Vector3f());
            store.addComponent(ref, Teleport.getComponentType(), teleport);
        });
    }

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
                if (stats!=null){
                    stats.maximizeStatValue(healthIndex);
                }
            });
        }
    }
}
