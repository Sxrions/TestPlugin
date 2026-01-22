package dev.jojo.plugin.systems;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DeathSystem extends DeathSystems.OnDeathSystem {
    @Override
    public void onComponentAdded(@NotNull Ref<EntityStore> paramRef, @NotNull DeathComponent paramT, @NotNull Store<EntityStore> paramStore, @NotNull CommandBuffer<EntityStore> paramCommandBuffer) {
        paramStore.getExternalData().getWorld().execute(() -> {
            paramCommandBuffer.getStore().getExternalData().getWorld().getDeathConfig().getRespawnController().respawnPlayer(Universe.get().getWorld("lobby"), paramRef, paramCommandBuffer);
        });
    }

    @Override
    public @Nullable Query<EntityStore> getQuery() {
        return Archetype.empty();
    }
}
