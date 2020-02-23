package com.gmail.tracebachi.floorislava.arena.perks;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class Chikun extends Perk {
    @Override
    public boolean onPerkActivation(Player player, Action clickAction, Block clickedBlock, PlayerInteractEvent interactEvent, Player rightClicked) {
        interactEvent.setCancelled(false);
        return true;
    }

    @Override
    public Material getItem() {
        return Material.EGG;
    }
}
