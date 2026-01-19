package dev.jojo.plugin.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.jojo.plugin.kit.KitManager;
import org.jetbrains.annotations.NotNull;

public class KitCommand extends AbstractPlayerCommand {
    private KitManager kitManager;
    private final RequiredArg<String> arg;

    public KitCommand(@NotNull String name, @NotNull String description, @NotNull KitManager kitManager) {
        super(name, description);
        this.kitManager = kitManager;
        arg = withRequiredArg("kit", "The kit that will be applied", ArgTypes.STRING);
    }

    @Override
    protected void execute(@NotNull CommandContext paramCommandContext, @NotNull Store<EntityStore> paramStore, @NotNull Ref<EntityStore> paramRef, @NotNull PlayerRef paramPlayerRef, @NotNull World paramWorld) {
        String kitName = arg.get(paramCommandContext);
        if (kitManager.exists(kitName)){
            Player player = paramStore.getComponent(paramRef, Player.getComponentType());
            paramCommandContext.sendMessage(Message.raw("Application du kit " + kitName));
            assert player != null;
            kitManager.applyKit(player, kitName);
        } else {
            paramCommandContext.sendMessage(Message.raw("Kit non existant"));
        }
    }
}
