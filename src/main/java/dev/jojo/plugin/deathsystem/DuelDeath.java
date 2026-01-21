package dev.jojo.plugin.deathsystem;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.jojo.plugin.duel.DuelManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DuelDeath extends DeathSystems.OnDeathSystem {
    @Override
    public void onComponentAdded(@NotNull Ref<EntityStore> paramRef, @NotNull DeathComponent paramT, @NotNull Store<EntityStore> paramStore, @NotNull CommandBuffer<EntityStore> paramCommandBuffer) {
        PlayerRef deadRef = paramStore.getComponent(paramRef, PlayerRef.getComponentType());
        if (deadRef == null) return;
        DuelManager.getInstance().handlePlayerDeath(deadRef);
    }

    @Override
    public @Nullable Query<EntityStore> getQuery() {
        return Archetype.empty();
    }
}
