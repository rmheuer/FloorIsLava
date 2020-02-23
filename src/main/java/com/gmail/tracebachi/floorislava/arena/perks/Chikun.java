package com.gmail.tracebachi.floorislava.arena.perks;

import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class Chikun extends Perk {
    @Override
    public boolean onPerkActivation(PlayerInteractEvent interactEvent, PlayerInteractEntityEvent entityEvent) {
        interactEvent.setCancelled(false);
        return true;
    }

    @Override
    public Material getItem() {
        return Material.EGG;
    }

    @Override
    public String getCooldownMessage() {
        return "You cannot use chikun eggs yet.";
    }
}
