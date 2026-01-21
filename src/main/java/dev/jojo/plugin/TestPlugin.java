package dev.jojo.plugin;

import com.hypixel.hytale.component.system.ISystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.jojo.plugin.commands.QueueCommand;
import dev.jojo.plugin.commands.SeeQueueCommand;
import dev.jojo.plugin.deathsystem.DuelDeath;
import dev.jojo.plugin.duel.DuelManager;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class TestPlugin extends JavaPlugin {
    HytaleLogger logger = HytaleLogger.forEnclosingClass();
    static TestPlugin pluginInstance;

    public TestPlugin(@NotNull JavaPluginInit init) {
        super(init);
        pluginInstance = this;
    }

    public static TestPlugin getPluginInstance(){
        return pluginInstance;
    }

    @Override
    protected void setup() {
        super.setup();
        logger.at(Level.INFO).log("OMG LE MOD MARCHE");
        logger.at(Level.INFO).log(this.getDataDirectory().toFile().toString());

        this.getCommandRegistry().registerCommand(new QueueCommand("queue", "Fait la queue"));
        this.getCommandRegistry().registerCommand(new SeeQueueCommand("seequeue", "look at the queue bro"));

        DuelManager duelManager = DuelManager.getInstance();
    }
}
