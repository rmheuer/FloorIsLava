package com.gmail.tracebachi.floorislava.arena.perks;

import com.gmail.tracebachi.floorislava.utils.ArenaUtils;
import com.gmail.tracebachi.floorislava.utils.CuboidArea;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import static com.gmail.tracebachi.floorislava.utils.Prefixes.BAD;
import static com.gmail.tracebachi.floorislava.utils.Prefixes.GOOD;

public class Boost extends Perk {

    private CuboidArea arenaArea;

    public Boost(CuboidArea arenaArea) {
        this.arenaArea = arenaArea;
    }

    @Override
    public boolean onPerkActivation(PlayerInteractEvent e, PlayerInteractEntityEvent entityEvent) {
        if (e == null)
            return false;
        if (ArenaUtils.isPlayerNearWebs(e.getPlayer(), 1, arenaArea)) {
            e.getPlayer().sendMessage(BAD + "You can not use a boost while near webs!");
            e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
            return false;
        }
        Location loc = e.getPlayer().getLocation().clone();
        loc.setPitch(-30f);
        Vector vector = loc.getDirection();
        vector.add(new Vector(0.0, 0.15, 0.0));
        vector.multiply(2);
        e.getPlayer().sendMessage(GOOD + "Woooooosh...");
        e.getPlayer().setVelocity(vector);
        e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_GHAST_SHOOT, 1f, 1f);
        return true;
    }

    @Override
    public Material getItem() {
        return Material.FEATHER;
    }

    @Override
    public String getCooldownMessage() {
        return "You cannot boost yet.";
    }
}
