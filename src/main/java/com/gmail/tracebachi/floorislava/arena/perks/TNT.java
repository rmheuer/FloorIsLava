package com.gmail.tracebachi.floorislava.arena.perks;

import com.gmail.tracebachi.floorislava.FloorIsLavaPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.Objects;

public class TNT extends Perk {
    private final FloorIsLavaPlugin plugin;

    public TNT(FloorIsLavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onPerkActivation(PlayerInteractEvent e, PlayerInteractEntityEvent entityEvent) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Location location = Objects.requireNonNull(e.getClickedBlock(),
                    "If a block was right clicked, why'd this be null?").getLocation();
            TNTPrimed tnt = e.getPlayer().getWorld().spawn(location.add(0, 1, 0), TNTPrimed.class);
            tnt.setMetadata("FIL", new FixedMetadataValue(plugin, "FIL"));
        } else if (e.getAction() == Action.RIGHT_CLICK_AIR) {
            Location location = e.getPlayer().getLocation();
            TNTPrimed tnt = e.getPlayer().getWorld().spawn(location.add(0, 1, 0), TNTPrimed.class);
            tnt.setMetadata("FIL", new FixedMetadataValue(plugin, "FIL"));
            Vector vector = e.getPlayer().getLocation().getDirection();
            vector.add(new Vector(0.0, 0.15, 0.0));
            tnt.setVelocity(vector);
        }
        return true;
    }

    @Override
    public Material getItem() {
        return Material.TNT;
    }

    @Override
    public String getCooldownMessage() {
        return "You cannot use TNT yet.";
    }
}
