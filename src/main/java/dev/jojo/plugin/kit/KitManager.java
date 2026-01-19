package dev.jojo.plugin.kit;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.EntityStatType;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jetbrains.annotations.Async;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class KitManager {

    private final ObjectMapper mapper = new ObjectMapper();
    private final File kitsFolder;
    private final Map<String, Kit> kits;
    private final JavaPlugin plugin;


    public KitManager(JavaPlugin plugin){
        this.plugin = plugin;
        this.kitsFolder = new File(plugin.getDataDirectory().toFile(),"kits");
        this.kits = new HashMap<>();
    }

    public void loadKits(){
        plugin.getLogger().atInfo().log("Loading kits...");
        File[] files = kitsFolder.listFiles();
        for (File file : files) {
            if (file.getName().endsWith(".json")){
                String kitName = file.getName().replace(".json","");
                try{
                    HashMap<String, Integer> items = mapper.readValue(file, new TypeReference<HashMap<String, Integer>>() {});
                    Kit kit = new Kit(kitName,items);
                    kits.put(kitName, kit);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        plugin.getLogger().atInfo().log("Kits loaded.");
    }

    public void listKits(){
        for (Map.Entry<String, Kit> entry : kits.entrySet()) {
            plugin.getLogger().at(Level.INFO).log(entry.getKey());
            Map<String, Integer> items = entry.getValue().getItems();
            for (Map.Entry<String, Integer> item : items.entrySet()) {
                plugin.getLogger().at(Level.INFO).log(item.getKey() +" : "+item.getValue());
            }
        }
    }

    private Kit getKit(String name){
        return kits.get(name);
    }

    public boolean exists(String name){
        return kits.containsKey(name);
    }

    public void applyKit(Player player, String kitName){
        player.getInventory().clear();
        Kit kit = getKit(kitName);
        Map<String, Integer> items = kit.getItems();
        for (String itemId : items.keySet()) {
            //ItemStack itemStack = new ItemStack(itemId);
            ItemStack itemStack = new ItemStack(itemId, items.get(itemId));
            if (itemId.contains("Armor")){
                player.getInventory().getArmor().addItemStack(itemStack);
            }else {
                player.getInventory().getCombinedHotbarFirst().addItemStack(itemStack);
            }
        }

        Ref<EntityStore> ref = player.getReference();
        Store<EntityStore> store = ref.getStore();
        EntityStatMap stats = store.getComponent(ref, EntityStatMap.getComponentType());
        int index = DefaultEntityStatTypes.getHealth();

        HytaleServer.SCHEDULED_EXECUTOR.schedule(() -> { //TODO TESTER LA THEORIE DES THREADS DU MALHEUR
            stats.setStatValue(index, stats.get(index).getMax());
        },500, TimeUnit.MILLISECONDS);
    }
}