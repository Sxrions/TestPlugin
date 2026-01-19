package dev.jojo.plugin.arena;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.JavaType;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;

import java.io.File;
import java.util.*;

public class ArenaManager {
    private final ObjectMapper mapper = new ObjectMapper();
    private final File arenasFolder;
    private final Map<String, Arena> arenas;
    private final JavaPlugin plugin;

    public ArenaManager(JavaPlugin plugin){
        this.plugin = plugin;
        this.arenasFolder = new File(plugin.getDataDirectory().toFile(), "arenas");
        arenas = new HashMap<>();
    }

    public Arena getRandomFreeArena(){
        Collection<Arena> arenasList = new ArrayList<>();
        for (Arena arena : arenas.values()) {
            if (!arena.isOccupied()){
                arenasList.add(arena);
            }
        }
        int index = (int) Math.round(Math.random() * (arenasList.size()-1));
        return null;
    }

    public void loadArenas(){
        plugin.getLogger().atInfo().log("Arenas loading...");
        File[] files = arenasFolder.listFiles();
        for (File file : files){
            if (file.getName().endsWith(".json")){
                try {
                    Arena arena = mapper.readValue(file, Arena.class);
                    arenas.put(arena.getWorldName(), arena);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        plugin.getLogger().atInfo().log("Arenas loaded.");
    }

    public void listArenas(){
        plugin.getLogger().atInfo().log("ARENAS : ");
        for (Arena arena : arenas.values()) {
            plugin.getLogger().atInfo().log("name : "+ arena.getWorldName());
            plugin.getLogger().atInfo().log("coords 1 : "+ arena.getSpawn1()[0] + " " +arena.getSpawn1()[0] + " " +arena.getSpawn1()[0]);
            plugin.getLogger().atInfo().log("coords 2 : "+ arena.getSpawn2()[0] + " " +arena.getSpawn2()[0] + " " +arena.getSpawn2()[0]);
            plugin.getLogger().atInfo().log("Occupied ? : "+ arena.isOccupied());
        }
    }
}
