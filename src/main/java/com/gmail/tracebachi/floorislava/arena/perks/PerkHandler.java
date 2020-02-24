package com.gmail.tracebachi.floorislava.arena.perks;

import com.gmail.tracebachi.floorislava.FloorIsLavaPlugin;
import com.gmail.tracebachi.floorislava.utils.CuboidArea;
import com.gmail.tracebachi.floorislava.utils.ItemUseDelay;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public class PerkHandler {
    private ItemUseDelay delay;
    private CuboidArea arenaArea;
    private FloorIsLavaPlugin plugin;

    private Map<String, Perk> perks;

    public PerkHandler(CuboidArea arenaArea, FloorIsLavaPlugin plugin) {
        this.arenaArea = arenaArea;
        this.plugin = plugin;
        this.delay = new ItemUseDelay(this.plugin.getConfig());
        this.init();
    }

    public Perk getPerkFromMaterial(Material material) {
        for (Perk perk : perks.values())
            if (perk.getItem() == material) return perk;
        return null;
    }

    private void init() {
        this.perks = new HashMap<>();
        this.add("tnt", new TNT(plugin));
        this.add("hook", new Hook(arenaArea));
        this.add("web", new Web(arenaArea));
        this.add("invis", new Invis(plugin));
        this.add("boost", new Boost(arenaArea));
        this.add("chikun", new Chikun());
        this.add("steal", new Steal());
    }

    private void add(String name, Perk perk) {
        perk.setDelay(delay.valueOf(name));
        this.perks.put(name, perk);
    }
}
