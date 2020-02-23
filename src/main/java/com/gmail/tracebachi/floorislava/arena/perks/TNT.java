package com.gmail.tracebachi.floorislava.arena.perks;

import com.gmail.tracebachi.floorislava.FloorIsLavaPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

public class TNT extends Perk {
    private FloorIsLavaPlugin plugin;

    public TNT(FloorIsLavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onPerkActivation(Player player, Action action, Block block, PlayerInteractEvent e, Player rightClicked) {
        if (action == Action.RIGHT_CLICK_BLOCK) {
            Location location = block.getLocation();
            TNTPrimed tnt = player.getWorld().spawn(location.add(0, 1, 0), TNTPrimed.class);
            tnt.setMetadata("FIL", new FixedMetadataValue(plugin, "FIL"));
        } else if (action == Action.RIGHT_CLICK_AIR) {
            Location location = player.getLocation();
            TNTPrimed tnt = player.getWorld().spawn(location.add(0, 1, 0), TNTPrimed.class);
            tnt.setMetadata("FIL", new FixedMetadataValue(plugin, "FIL"));
            Vector vector = player.getLocation().getDirection();
            vector.add(new Vector(0.0, 0.15, 0.0));
            tnt.setVelocity(vector);
        }
        return true;
    }

    @Override
    public Material getItem() {
        return Material.TNT;
    }
}
