package dev.jojo.plugin.systems;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.jojo.plugin.duel.DuelManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class DeathSystem extends DeathSystems.OnDeathSystem {

    /*@NotNull
    public Set<Dependency<EntityStore>> getDependencies() {
        return Set.of(new SystemDependency<>(Order.BEFORE, DeathSystems.PlayerDeathScreen.class));
    }*/

    @Override
    public void onComponentAdded(@NotNull Ref<EntityStore> paramRef, @NotNull DeathComponent paramT, @NotNull Store<EntityStore> paramStore, @NotNull CommandBuffer<EntityStore> paramCommandBuffer) {
        DuelManager.getInstance().handlePlayerDeath(paramCommandBuffer.getComponent(paramRef, PlayerRef.getComponentType()));
    }

    @Override
    public @Nullable Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }
}
