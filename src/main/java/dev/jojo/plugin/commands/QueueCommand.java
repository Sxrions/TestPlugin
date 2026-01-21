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
import dev.jojo.plugin.duel.DuelManager;
import dev.jojo.plugin.kit.KitManager;
import org.jetbrains.annotations.NotNull;

public class QueueCommand extends AbstractPlayerCommand {
    private final RequiredArg<String> kit;

    public QueueCommand(@NotNull String name, @NotNull String description) {
        super(name, description);
        kit = withRequiredArg("kit", "Kit", ArgTypes.STRING);
    }

    @Override
    protected void execute(@NotNull CommandContext paramCommandContext, @NotNull Store<EntityStore> paramStore, @NotNull Ref<EntityStore> paramRef, @NotNull PlayerRef paramPlayerRef, @NotNull World paramWorld) {
        paramWorld.execute(() -> {
            String kitName = kit.get(paramCommandContext);
            KitManager kitManager = KitManager.getInstance();
            DuelManager duelManager = DuelManager.getInstance();
            if (kitManager.exists(kitName)){
                assert paramPlayerRef != null;
                duelManager.addToQueue(paramPlayerRef,kitName);
            } else {
                paramCommandContext.sendMessage(Message.raw("Kit non existant"));
            }
        });
    }
}
