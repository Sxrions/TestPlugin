package dev.jojo.plugin.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.jojo.plugin.duel.DuelManager;
import org.jetbrains.annotations.NotNull;

public class DequeueCommand extends AbstractPlayerCommand {
    public DequeueCommand(@NotNull String name, @NotNull String description) {
        super(name, description);
    }

    @Override
    protected void execute(@NotNull CommandContext paramCommandContext, @NotNull Store<EntityStore> paramStore, @NotNull Ref<EntityStore> paramRef, @NotNull PlayerRef paramPlayerRef, @NotNull World paramWorld) {
        paramWorld.execute(() -> {
            DuelManager duelManager = DuelManager.getInstance();
            duelManager.removeFromQueue(paramPlayerRef);
        });
    }
}
