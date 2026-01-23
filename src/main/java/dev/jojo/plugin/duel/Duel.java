package dev.jojo.plugin.duel;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import dev.jojo.plugin.TestPlugin;
import dev.jojo.plugin.arena.Arena;
import dev.jojo.plugin.kit.KitManager;

import java.awt.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Duel {
    private final PlayerRef playerRef1;
    private final PlayerRef playerRef2;
    private final Arena arena;
    private DuelState state;
    private final String kitName;

    private int countdown = 5;
    private int timer = 120;

    private ScheduledFuture<?> taskCountdown;
    private ScheduledFuture<?> taskTimer;

    public Duel(PlayerRef p1, PlayerRef p2, Arena arena, String kitName) {
        this.playerRef1 = p1;
        this.playerRef2 = p2;
        this.arena = arena;
        this.kitName = kitName;

        this.arena.setOccupied(true); //OCCUPATION DE L'ARENE DES LA CREATION DU DUEL
    }

    public void startCountdown() { //TODO bloquer les déplacements des joueurs (ou set speed 0 jsp comment)
        teleportPlayer(playerRef1);
        teleportPlayer(playerRef2);
        this.state = DuelState.STARTING;
        taskCountdown = HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(() -> {
            if (countdown <= 0) {
                taskCountdown.cancel(false);
                start();
                return;
            }
            EventTitleUtil.showEventTitleToPlayer(playerRef1, Message.raw(countdown + " secondes..."), Message.raw("Duel dans :"), true);
            EventTitleUtil.showEventTitleToPlayer(playerRef2, Message.raw(countdown + " secondes..."), Message.raw("Duel dans :"), true);
            countdown--;
        }, 0, 1, TimeUnit.SECONDS);
    }

    private void start() {
        KitManager kitManager = KitManager.getInstance();
        kitManager.applyKit(playerRef1, kitName);
        kitManager.applyKit(playerRef2, kitName);
        //TODO MODIFIER ETATS JOUEURS jsp encore ce que je veux dire par la
        taskTimer = HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(() -> {
            if (timer <= 0) {
                taskTimer.cancel(false);
                endDraw();
                return;
            }
            timer--;
        }, 0, 1, TimeUnit.SECONDS);

        this.state = DuelState.PLAYING;
    }

    public void end(PlayerRef looser) {
        this.state = DuelState.ENDING;


        TestPlugin.getPluginInstance().lobby.execute(() -> {

            if (looser.getUuid().equals(playerRef1.getUuid())) {
                EventTitleUtil.showEventTitleToPlayer(playerRef2, Message.raw("VICTOIRE"), Message.raw(playerRef1.getUsername() + " est nul"), true);
                EventTitleUtil.showEventTitleToPlayer(playerRef1, Message.raw("DÉFAITE"), Message.raw(playerRef2.getUsername() + " est supérieur"), true);
            } else {
                EventTitleUtil.showEventTitleToPlayer(playerRef1, Message.raw("VICTOIRE"), Message.raw(playerRef2.getUsername() + " est nul"), true);
                EventTitleUtil.showEventTitleToPlayer(playerRef2, Message.raw("DÉFAITE"), Message.raw(playerRef1.getUsername() + " est supérieur"), true);
            }
            end();
        });
    }

    public void endDraw() {
        this.state = DuelState.ENDING;
        EventTitleUtil.showEventTitleToPlayer(playerRef1, Message.raw("DRAW !").color(Color.blue), Message.raw("You're all guez"), true);
        EventTitleUtil.showEventTitleToPlayer(playerRef2, Message.raw("DRAW !").color(Color.blue), Message.raw("You're all guez"), true);
        end();
    }

    public void end() {
        cancelTasks();

        Player p1 = TestPlugin.getPluginInstance().lobby.getEntityStore().getStore().getComponent(playerRef1.getReference(), Player.getComponentType());
        Player p2 = TestPlugin.getPluginInstance().lobby.getEntityStore().getStore().getComponent(playerRef2.getReference(), Player.getComponentType());
        p1.getInventory().clear();
        p2.getInventory().clear();

        heal(playerRef1.getReference());
        heal(playerRef2.getReference());

        //TODO ICI QUE VIENT LE KICK
        teleportLobby(playerRef1);
        teleportLobby(playerRef2);

        //TODO RESET LES JOUEURS, RESET l'arene
        //TODO PAS OUBLIER DE VIRER DUEL DE DUELMANAGER

        arena.setOccupied(false);
    }

    enum DuelState {
        STARTING,
        PLAYING,
        ENDING
    }

    private void teleportPlayer(PlayerRef playerRef) {
        if (playerRef == null) return;
        Ref<EntityStore> ref = playerRef.getReference();
        Store<EntityStore> store = ref.getStore();
        World lobby = TestPlugin.getPluginInstance().lobby;
        lobby.execute(() -> {
            Teleport teleport;
            if (playerRef.equals(playerRef1)) {
                teleport = new Teleport(lobby, new Vector3d(arena.getSpawn1()[0], arena.getSpawn1()[1], arena.getSpawn1()[2]), new Vector3f(0, 0, 0));
            } else {
                teleport = new Teleport(lobby, new Vector3d(arena.getSpawn2()[0], arena.getSpawn2()[1], arena.getSpawn2()[2]), new Vector3f(0, 0, 0));
            }
            store.addComponent(ref, Teleport.getComponentType(), teleport);
        });
    }

    private void teleportLobby(PlayerRef playerRef) {
        if (playerRef == null) return;
        Ref<EntityStore> ref = playerRef.getReference();
        Store<EntityStore> store = ref.getStore();
        World lobby = TestPlugin.getPluginInstance().lobby;
        lobby.execute(() -> {
            Teleport teleport = new Teleport(lobby, lobby.getWorldConfig().getSpawnProvider().getSpawnPoint(lobby, playerRef.getUuid()).getPosition(), new Vector3f());
            store.addComponent(ref, Teleport.getComponentType(), teleport);
        });
    }

    public void cancelTasks() {
        if (taskTimer != null) {
            try {
                taskTimer.cancel(false);
            } catch (Throwable ignored) {
            }
            taskTimer = null;
        }
        if (taskCountdown != null) {
            try {
                taskCountdown.cancel(false);
            } catch (Throwable ignored) {
            }
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

    public void heal(Ref<EntityStore> ref) {
        if (ref != null) {
            ref.getStore().getExternalData().getWorld().execute(() -> {
                Store<EntityStore> store = ref.getStore();
                EntityStatMap stats = store.getComponent(ref, EntityStatMap.getComponentType());
                int healthIndex = DefaultEntityStatTypes.getHealth();
                if (stats != null) {
                    stats.maximizeStatValue(healthIndex);
                }
            });
        }
    }
}
