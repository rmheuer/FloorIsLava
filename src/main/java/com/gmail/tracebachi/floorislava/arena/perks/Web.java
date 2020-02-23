package com.gmail.tracebachi.floorislava.arena.perks;

import com.gmail.tracebachi.floorislava.utils.CuboidArea;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class Web extends Perk {
    private CuboidArea arenaArea;

    public Web(CuboidArea arenaArea) {
        this.arenaArea = arenaArea;
    }

    @Override
    public boolean onPerkActivation(PlayerInteractEvent interactEvent, PlayerInteractEntityEvent e) {
        createWebsAroundPlayer((Player) e.getRightClicked(), 2);
        return true;
    }

    @Override
    public Material getItem() {
        return Material.COBWEB;
    }

    @Override
    public String getCooldownMessage() {
        return "You cannot web players yet.";
    }

    private void createWebsAroundPlayer(Player player, int radius) {
        int px = player.getLocation().getBlockX();
        int py = player.getLocation().getBlockY();
        int pz = player.getLocation().getBlockZ();
        World world = player.getWorld();
        //TODO maybe it can be improved
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x * x + y * y + z * z <= radius * radius) {
                        int xpos = px + x;
                        int ypos = py + y;
                        int zpos = pz + z;
                        if (world.getBlockAt(xpos, ypos, zpos).getType().equals(Material.AIR) &&
                                arenaArea.isInside(xpos, ypos, zpos))
                            world.getBlockAt(xpos, ypos, zpos).setType(Material.COBWEB);
                    }
                }
            }
        }
    }
}
