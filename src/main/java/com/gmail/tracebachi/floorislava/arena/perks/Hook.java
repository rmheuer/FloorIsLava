package com.gmail.tracebachi.floorislava.arena.perks;

import com.gmail.tracebachi.floorislava.utils.ArenaUtils;
import com.gmail.tracebachi.floorislava.utils.CuboidArea;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import static com.gmail.tracebachi.floorislava.utils.Prefixes.BAD;

public class Hook extends Perk {
    private CuboidArea arenaArea;

    public Hook(CuboidArea arenaArea) {
        this.arenaArea = arenaArea;
    }

    @Override
    public boolean onPerkActivation(Player player, Action clickAction, Block clickedBlock, PlayerInteractEvent interactEvent, Player rightClicked) {
        if (ArenaUtils.isPlayerNearWebs(rightClicked, 2, arenaArea)) {
            player.sendMessage(BAD + "You can not launch a player near webs!");
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
            return false;
        }
        Location playerLoc = player.getLocation();
        playerLoc.setPitch(-30f);
        Vector playerDir = playerLoc.getDirection();
        playerDir.add(new Vector(0.0, 0.15, 0.0));
        playerDir.multiply(2);
        rightClicked.getLocation().setDirection(playerDir);
        rightClicked.setVelocity(playerDir);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 1f, 1f);
        return true;
    }

    @Override
    public Material getItem() {
        return Material.TRIPWIRE_HOOK;
    }
}
