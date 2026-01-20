package dev.jojo.plugin.duel;

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
    private int timer = 120;

    private ScheduledFuture<?> taskCountdown;
    private ScheduledFuture<?> taskTimer;

    public Duel(Player p1, Player p2, Arena arena, String kitName){
        this.player1 = p1;
        this.player2 = p2;
        this.arena = arena;
        this.kitName = kitName;

        this.arena.setOccupied(true); //OCCUPATION DE L'ARENE DES LA CREATION DU DUEL
        teleportPlayers();
    }

    public void startCountdown(){
        this.state = DuelState.STARTING;
        taskCountdown = HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(() -> {
            if (countdown<=0){
                taskCountdown.cancel(false);
                start();
                return;
            }
            countdown--;
            player1.sendMessage(Message.raw("" + countdown));
            player2.sendMessage(Message.raw("" + countdown));
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

    private void teleportPlayers(){
        World world = arena.getWorld();
        World wp1 = player1.getWorld();
        World wp2 = player2.getWorld();

        wp1.execute(() -> {
            Store<EntityStore> store = player1.getReference().getStore();
            Teleport teleport = Teleport.createForPlayer(world, new Vector3d(arena.getSpawn1()[0],arena.getSpawn1()[1],arena.getSpawn1()[2]), new Vector3f(0,0,0));
            store.addComponent(player1.getReference(), Teleport.getComponentType(), teleport);
        });
        wp2.execute(() -> {
            Store<EntityStore> store = player2.getReference().getStore();
            Teleport teleport = Teleport.createForPlayer(world, new Vector3d(arena.getSpawn2()[0],arena.getSpawn2()[1],arena.getSpawn2()[2]), new Vector3f(0,0,0));
            store.addComponent(player2.getReference(), Teleport.getComponentType(), teleport);
        });
    }
}
