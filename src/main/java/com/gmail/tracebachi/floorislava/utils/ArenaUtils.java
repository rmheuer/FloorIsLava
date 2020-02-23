package com.gmail.tracebachi.floorislava.utils;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class ArenaUtils {
    public static boolean isPlayerNearWebs(Player player, int radius, CuboidArea arenaArea) {
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

                        if (world.getBlockAt(xpos, ypos, zpos).getType().equals(Material.COBWEB) &&
                                arenaArea.isInside(xpos, ypos, zpos))
                            return true;
                    }
                }
            }
        }
        return false;
    }
}
