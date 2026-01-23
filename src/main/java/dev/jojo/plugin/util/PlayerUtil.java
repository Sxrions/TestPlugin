package dev.jojo.plugin.util;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.modules.entity.component.Invulnerable;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class PlayerUtil {
    public static void teleportWhenJoining(PlayerReadyEvent evt) {
        System.out.println("Player connected : " + evt.getPlayer().getDisplayName());
        Universe.get().getWorld("lobby").execute(() -> {
            Teleport teleport = new Teleport(Universe.get().getWorld("lobby"), new Vector3d(116.5, 25, 114.5), new Vector3f());
            evt.getPlayer().getInventory().clear();
            Store<EntityStore> store = evt.getPlayerRef().getStore();
            store.addComponent(evt.getPlayer().getReference(), Teleport.getComponentType(), teleport);
            if (evt.getPlayer().getGameMode().equals(GameMode.Adventure)) {
                store.addComponent(evt.getPlayer().getReference(), Invulnerable.getComponentType());
            }
        });
    }
}
