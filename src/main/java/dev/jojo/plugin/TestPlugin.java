package dev.jojo.plugin;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import dev.jojo.plugin.arena.ArenaManager;
import dev.jojo.plugin.commands.KitCommand;
import dev.jojo.plugin.duel.DuelManager;
import dev.jojo.plugin.kit.KitManager;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class TestPlugin extends JavaPlugin {
    HytaleLogger logger = HytaleLogger.forEnclosingClass();
    static TestPlugin pluginInstance;

    private TestPlugin(@NotNull JavaPluginInit init) {
        super(init);
    }

    public static TestPlugin getPluginInstance(){
        return pluginInstance;
    }

    @Override
    protected void setup() {
        super.setup();
        logger.at(Level.INFO).log("OMG LE MOD MARCHE");
        logger.at(Level.INFO).log(this.getDataDirectory().toFile().toString());

        this.getCommandRegistry().registerCommand(new KitCommand("kit", "Applique le kit la", KitManager.getInstance()));
    }
}
