package dev.jojo.plugin.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.RootDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;

public class NoInteractionSystem extends EntityEventSystem<EntityStore, UseBlockEvent.Pre> {
    public NoInteractionSystem() {
        super(UseBlockEvent.Pre.class);
    }

    @Override
    public @Nullable Query<EntityStore> getQuery() {
        return Query.and(PlayerRef.getComponentType());
    }

    @Override
    public void handle(int paramInt, @NotNull ArchetypeChunk<EntityStore> paramArchetypeChunk, @NotNull Store<EntityStore> paramStore, @NotNull CommandBuffer<EntityStore> paramCommandBuffer, @NotNull UseBlockEvent.Pre paramEventType) {
        paramEventType.setCancelled(true);
    }

}
