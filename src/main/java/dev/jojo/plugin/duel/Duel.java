package dev.jojo.plugin.duel;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import dev.jojo.plugin.arena.Arena;
import dev.jojo.plugin.kit.Kit;
import dev.jojo.plugin.kit.KitManager;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Duel {
    private Player player1;
    private Player player2;
    private Arena arena;
    private DuelState state;
    private String kitName;

    private int countdown = 5;
    private int timer = 20;

    private ScheduledFuture<?> taskCountdown;
    private ScheduledFuture<?> taskTimer;

    public Duel(Player p1, Player p2, Arena arena, String kitName){
        this.player1 = p1;
        this.player2 = p2;
        this.arena = arena;
        this.kitName = kitName;

        this.arena.setOccupied(true); //OCCUPATION DE L'ARENE DES LA CREATION DU DUEL
    }

    public void startCountdown(){
        teleportPlayer(player1);
        teleportPlayer(player2);
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
        kitManager.applyKit(player1,kitName);
        kitManager.applyKit(player2,kitName);
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

    public void end(Player looser){
        this.state = DuelState.ENDING;
        //TODO Affichage victoire défaite puis call end()
    }

    public void endDraw(){
        this.state = DuelState.ENDING;
        EventTitleUtil.showEventTitleToWorld(Message.raw("DRAW !"), Message.raw("You are all guez"), true, null, 1,1,1,player1.getReference().getStore());
        //TODO Affichage DRAW puis call end()
    }

    public void end(){
        taskTimer.cancel(false);
        taskCountdown.cancel(false);
      //TODO RESET LES JOUEURS, RESET l'arene
    } //TODO PAS OUBLIER DE VIRER DUEL DE DUELMANAGER

    enum DuelState{
        STARTING,
        PLAYING,
        ENDING
    }

    private void teleportPlayer(Player player){
        World world = arena.getWorld();
        World wp1 = player.getWorld();

        wp1.execute(() -> {
            Ref<EntityStore> ref = player.getReference();
            if (ref == null){
                HytaleServer.SCHEDULED_EXECUTOR.schedule(() -> {
                    teleportPlayer(player);
                },100,TimeUnit.MILLISECONDS);
                return;
            }

            Store<EntityStore> store = ref.getStore();
            Teleport teleport;
            if (player.equals(player1)){
                teleport = new Teleport(world, new Vector3d(arena.getSpawn1()[0],arena.getSpawn1()[1],arena.getSpawn1()[2]), new Vector3f(0,0,0));
            }else{
                teleport = new Teleport(world, new Vector3d(arena.getSpawn2()[0],arena.getSpawn2()[1],arena.getSpawn2()[2]), new Vector3f(0,0,0));
            }
            store.addComponent(ref, Teleport.getComponentType(), teleport);
        });
    }
}
