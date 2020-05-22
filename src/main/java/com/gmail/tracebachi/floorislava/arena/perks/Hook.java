package com.gmail.tracebachi.floorislava.arena.perks;

import com.gmail.tracebachi.floorislava.utils.ArenaUtils;
import com.gmail.tracebachi.floorislava.utils.CuboidArea;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import static com.gmail.tracebachi.floorislava.utils.Prefixes.BAD;

public class Hook extends Perk {

    private CuboidArea arenaArea;

    public Hook(CuboidArea arenaArea) {
        this.arenaArea = arenaArea;
    }

    @Override
    public boolean onPerkActivation(PlayerInteractEvent interactEvent, PlayerInteractEntityEvent e) {
        if (e == null)
            return false;
        if (!(e.getRightClicked() instanceof Player))
            return false;
        Player rightClicked = (Player) e.getRightClicked();
        if (ArenaUtils.isPlayerNearWebs(rightClicked, 2, arenaArea)) {
            e.getPlayer().sendMessage(BAD + "You can not launch a player near webs!");
            e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
            return false;
        }
        Location playerLoc = e.getPlayer().getLocation();
        playerLoc.setPitch(-30f);
        Vector playerDir = playerLoc.getDirection();
        playerDir.add(new Vector(0.0, 0.15, 0.0));
        playerDir.multiply(2);
        e.getRightClicked().getLocation().setDirection(playerDir);
        e.getRightClicked().setVelocity(playerDir);
        e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_PLAYER_HURT, 1f, 1f);
        return true;
    }

    @Override
    public Material getItem() {
        return Material.TRIPWIRE_HOOK;
    }

    @Override
    public String getCooldownMessage() {
        return "You cannot throw players yet.";
    }
}
