package dev.jojo.plugin.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.jojo.plugin.duel.DuelManager;
import dev.jojo.plugin.kit.KitManager;
import org.jetbrains.annotations.NotNull;

public class SeeQueueCommand extends AbstractPlayerCommand {
    private RequiredArg<String> kit;
    public SeeQueueCommand(@NotNull String name, @NotNull String description) {
        super(name, description);
        kit = withRequiredArg("kit", "kit", ArgTypes.STRING);
    }

    @Override
    protected void execute(@NotNull CommandContext paramCommandContext, @NotNull Store<EntityStore> paramStore, @NotNull Ref<EntityStore> paramRef, @NotNull PlayerRef paramPlayerRef, @NotNull World paramWorld) {
        paramCommandContext.sendMessage(Message.raw(DuelManager.getInstance().getQueue(kit.get(paramCommandContext))));
    }
}
