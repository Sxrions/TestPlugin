package dev.jojo.plugin.systems;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.jojo.plugin.duel.DuelManager;
import org.jetbrains.annotations.NotNull;

public class DuelDamageSystem extends DamageEventSystem {
    @Override
    public SystemGroup<EntityStore> getGroup() {
        return DamageModule.get().getFilterDamageGroup();
    }

    @Override
    public void handle(int paramInt, @NotNull ArchetypeChunk<EntityStore> paramArchetypeChunk, @NotNull Store<EntityStore> paramStore, @NotNull CommandBuffer<EntityStore> paramCommandBuffer, @NotNull Damage paramEventType) {
        System.out.println("DEGAT");

        // Check if the archetype contains PlayerRef BEFORE trying to get the reference
        if (!paramArchetypeChunk.getArchetype().contains(PlayerRef.getComponentType())) {
            return;
        }

        System.out.println("DEGAT AU JOUEUR");

        // Get the reference immediately, while we're still in the correct context
        Ref<EntityStore> ref;
        try {
            ref = paramArchetypeChunk.getReferenceTo(paramInt);
        } catch (IndexOutOfBoundsException e) {
            // Entity no longer exists in chunk
            return;
        }

        // Get the PlayerRef now, before the async execution
        PlayerRef playerRef = paramStore.getComponent(ref, PlayerRef.getComponentType());
        if (playerRef == null) {
            return;
        }

        EntityStatMap stats = paramStore.getComponent(ref, EntityStatMap.getComponentType());
        if (stats == null) {
            return;
        }

        int healthIndex = DefaultEntityStatTypes.getHealth();
        float currentHealth = stats.get(healthIndex).get();
        float damageAmount = paramEventType.getAmount();

        System.out.println("VIE : " + currentHealth);
        System.out.println("DEGAT QUE JE VAIS ME BOUFFER DANS LA GUEULE : " + damageAmount);

        if (currentHealth - damageAmount <= 0) {
            paramEventType.setCancelled(true);

            // Only schedule the death handling async, not the entire damage processing
            paramCommandBuffer.getExternalData().getWorld().execute(() -> {
                DuelManager.getInstance().handlePlayerDeath(playerRef);
                System.out.println("handled");

                stats.maximizeStatValue(healthIndex);
            });
        }
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }
}
