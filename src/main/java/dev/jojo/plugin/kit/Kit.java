package dev.jojo.plugin.kit;

import com.hypixel.hytale.server.core.asset.type.item.config.Item;

import java.util.Map;

public class Kit {
    private String name;
    private Map<String, Integer> items;

    public Kit(String name, Map<String, Integer> items){
        this.name = name;
        this.items = items;
    }

    public Map<String, Integer> getItems(){
        return items;
    }

    public String getName() {
        return name;
    }

    public class KitPojo {
        String itemName;
        Integer quantity;
    }
}

