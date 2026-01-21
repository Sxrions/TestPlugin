package dev.jojo.plugin.duel;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import dev.jojo.plugin.arena.Arena;
import dev.jojo.plugin.kit.KitManager;

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

        World arenaWorld = arena.getWorld();
        arenaWorld.execute(() -> {
            PlayerRef pref1 = arena.getWorld().getEntityStore().getStore().getComponent(playerRef1.getReference(),PlayerRef.getComponentType());
            PlayerRef pref2 = arena.getWorld().getEntityStore().getStore().getComponent(playerRef2.getReference(),PlayerRef.getComponentType());
            if (looser.getUuid() == playerRef1.getUuid()){
                EventTitleUtil.showEventTitleToPlayer(pref2,Message.raw("VICTOIRE"),Message.raw(playerRef1.getUsername() + " est nul"), true);
                EventTitleUtil.showEventTitleToPlayer(pref1,Message.raw("DÉFAITE"),Message.raw(playerRef2.getUsername() + " est supérieur"), true);
            } else {
                EventTitleUtil.showEventTitleToPlayer(pref1,Message.raw("VICTOIRE"),Message.raw(playerRef2.getUsername() + " est nul"), true);
                EventTitleUtil.showEventTitleToPlayer(pref2,Message.raw("DÉFAITE"),Message.raw(playerRef1.getUsername() + " est supérieur"), true);
            }
            end();
        });
    }

    public void endDraw(){
        this.state = DuelState.ENDING;
        EventTitleUtil.showEventTitleToWorld(Message.raw("DRAW !"), Message.raw("You are all guez"), true, null, 1,1,1, playerRef1.getReference().getStore());
        end();
    }

    public void end(){
        if (taskTimer != null) taskTimer.cancel(false);
        if (taskCountdown != null) taskCountdown.cancel(false);

        World lobby = Universe.get().getWorld("lobby"); //TODO ICI QUE VIENT LE KICK
        teleportPlayer(playerRef1, lobby);
        teleportPlayer(playerRef2, lobby);

        //TODO RESET LES JOUEURS, RESET l'arene
        //TODO PAS OUBLIER DE VIRER DUEL DE DUELMANAGER
        arena.setOccupied(false);
    }

    enum DuelState{
        STARTING,
        PLAYING,
        ENDING
    }

    private void teleportPlayer(PlayerRef playerRef, World dest){
        Ref<EntityStore> initialRef = playerRef.getReference();
        if (initialRef == null){
            HytaleServer.SCHEDULED_EXECUTOR.schedule(() -> teleportPlayer(playerRef, dest), 100, TimeUnit.MILLISECONDS);
            return;
        }
        Store<EntityStore> store = initialRef.getStore();
        World wp1 = store.getExternalData().getWorld();

        wp1.execute(() -> {
            Ref<EntityStore> ref = playerRef.getReference();
            if (ref == null){
                HytaleServer.SCHEDULED_EXECUTOR.schedule(() -> {
                    teleportPlayer(playerRef, dest);
                },100,TimeUnit.MILLISECONDS);
                return;
            }

            // If the player is dead (on respawn screen), don't teleport now — retry later
            try {
                DeathComponent death = store.getComponent(ref, DeathComponent.getComponentType());
                if (death != null) {
                    HytaleServer.SCHEDULED_EXECUTOR.schedule(() -> teleportPlayer(playerRef, dest), 300, TimeUnit.MILLISECONDS);
                    return;
                }
            } catch (Throwable ignored) {
                // If reading the component failed, retry shortly instead of failing
                HytaleServer.SCHEDULED_EXECUTOR.schedule(() -> teleportPlayer(playerRef, dest), 300, TimeUnit.MILLISECONDS);
                return;
            }

            Teleport teleport;
            if (playerRef.equals(playerRef1)){
                teleport = new Teleport(dest, new Vector3d(arena.getSpawn1()[0],arena.getSpawn1()[1],arena.getSpawn1()[2]), new Vector3f(0,0,0));
            }else{
                teleport = new Teleport(dest, new Vector3d(arena.getSpawn2()[0],arena.getSpawn2()[1],arena.getSpawn2()[2]), new Vector3f(0,0,0));
            }
            store.addComponent(ref, Teleport.getComponentType(), teleport);
        });
    }

    public PlayerRef getPlayerRef1() {
        return playerRef1;
    }

    public PlayerRef getPlayerRef2() {
        return playerRef2;
    }
}
