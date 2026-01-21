package dev.jojo.plugin.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.event.events.ecs.DamageBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockDamageSystem extends EntityEventSystem<EntityStore, DamageBlockEvent> {
    public BlockDamageSystem() {
        super(DamageBlockEvent.class);
    }

    @Override
    public void handle(int paramInt, @NotNull ArchetypeChunk<EntityStore> paramArchetypeChunk, @NotNull Store<EntityStore> paramStore, @NotNull CommandBuffer<EntityStore> paramCommandBuffer, @NotNull DamageBlockEvent paramEventType) {
        paramEventType.setCancelled(true);
    }

    @Override
    public @Nullable Query<EntityStore> getQuery() {
        return Query.and(PlayerRef.getComponentType());
    }
}
