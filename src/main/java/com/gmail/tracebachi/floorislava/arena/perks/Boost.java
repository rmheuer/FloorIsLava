package com.gmail.tracebachi.floorislava.arena.perks;

import com.gmail.tracebachi.floorislava.utils.ArenaUtils;
import com.gmail.tracebachi.floorislava.utils.CuboidArea;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
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
    public boolean onPerkActivation(Player player, Action clickAction, Block clickedBlock, PlayerInteractEvent e, Player rightClicked) {
        if (ArenaUtils.isPlayerNearWebs(player, 1, arenaArea)) {
            player.sendMessage(BAD + "You can not use a boost while near webs!");
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
            return false;
        }
        Location loc = player.getLocation().clone();
        loc.setPitch(-30f);
        Vector vector = loc.getDirection();
        vector.add(new Vector(0.0, 0.15, 0.0));
        vector.multiply(2);
        player.sendMessage(GOOD + "Woooooosh...");
        player.setVelocity(vector);
        player.playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1f, 1f);
        return true;
    }

    @Override
    public Material getItem() {
        return Material.FEATHER;
    }
}
