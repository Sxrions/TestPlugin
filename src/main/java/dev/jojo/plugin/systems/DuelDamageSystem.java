package dev.jojo.plugin.systems;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.ISystem;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.modules.entity.component.Invulnerable;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.jojo.plugin.duel.DuelManager;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class DuelDamageSystem extends DamageEventSystem {
    @Override
    public SystemGroup<EntityStore> getGroup() {
        return DamageModule.get().getFilterDamageGroup();
    }

    @Override
    public void handle(int paramInt, @NotNull ArchetypeChunk<EntityStore> paramArchetypeChunk, @NotNull Store<EntityStore> paramStore, @NotNull CommandBuffer<EntityStore> paramCommandBuffer, @NotNull Damage paramEventType) {
        if (!paramArchetypeChunk.getArchetype().contains(PlayerRef.getComponentType())) {
            return;
        }

        if (paramInt < 0 || paramInt >= paramArchetypeChunk.size()) {
            System.out.println("Index invalide: " + paramInt);
            return;
        }
        System.out.println("DEGAT AU JOUEUR");

        Ref<EntityStore> ref = paramArchetypeChunk.getReferenceTo(paramInt);
        PlayerRef playerRef = paramCommandBuffer.getComponent(ref, PlayerRef.getComponentType());
        EntityStatMap stats = paramCommandBuffer.getComponent(ref, EntityStatMap.getComponentType());

        int healthIndex = DefaultEntityStatTypes.getHealth();
        if (stats == null) {return;}
        float currentHealth = stats.get(healthIndex).get();
        float damageAmount = paramEventType.getAmount();

        System.out.println("VIE : " + currentHealth);
        System.out.println("DEGAT QUE JE VAIS ME BOUFFER DANS LA GUEULE : " + damageAmount);

        if (currentHealth - damageAmount <= 0) {
            paramEventType.setCancelled(true);
            UUID playerUuid = playerRef.getUuid();
            World world = paramCommandBuffer.getExternalData().getWorld();

            for (PlayerRef worldPlayerRef : world.getPlayerRefs()) {
                paramCommandBuffer.addComponent(worldPlayerRef.getReference(), Invulnerable.getComponentType());
            }

            DuelManager.getInstance().handlePlayerDeath(playerUuid);

            System.out.println("handled");
        }
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }
}
