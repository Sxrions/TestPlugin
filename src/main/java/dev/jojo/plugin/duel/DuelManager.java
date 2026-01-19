package dev.jojo.plugin.duel;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import dev.jojo.plugin.arena.ArenaManager;
import dev.jojo.plugin.kit.KitManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class DuelManager {
    private static DuelManager instance;
    private JavaPlugin plugin;
    private final Map<PlayerRef, Duel> playerRefMap;
    private final Collection<Duel> duels;
    private final ArenaManager arenaManager;
    private final KitManager kitManager;

    private DuelManager(JavaPlugin plugin){
        this.playerRefMap = new HashMap<>();
        this.duels = new HashSet<>();
        this.plugin = plugin;
        this.kitManager = new KitManager(plugin);
        this.arenaManager = new ArenaManager(plugin);
    }

    public static DuelManager getInstance(JavaPlugin plugin) {
        if (instance==null){
            instance = new DuelManager(plugin);
        }
        return instance;
    }

    public KitManager getKitManager() {
        return kitManager;
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }
}
