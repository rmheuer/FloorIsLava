package com.gmail.tracebachi.floorislava.arena.perks;

import com.gmail.tracebachi.floorislava.FloorIsLavaPlugin;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import static com.gmail.tracebachi.floorislava.utils.Prefixes.BAD;
import static com.gmail.tracebachi.floorislava.utils.Prefixes.GOOD;

public class Invis extends Perk {
    private final FloorIsLavaPlugin plugin;

    public Invis(FloorIsLavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onPerkActivation(PlayerInteractEvent e, PlayerInteractEntityEvent entityEvent) {
        for (Player other : Bukkit.getOnlinePlayers())
            other.hidePlayer(plugin, e.getPlayer());
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Player playerToMakeVisible = Bukkit.getPlayerExact(e.getPlayer().getName()); // So we have no logout issues.
            if (playerToMakeVisible == null)
                return;
            playerToMakeVisible.sendMessage(BAD + "You are now visible!");
            e.getPlayer().playNote(playerToMakeVisible.getLocation(), Instrument.PIANO, Note.natural(1, Note.Tone.A));
            for (Player other : Bukkit.getOnlinePlayers())
                other.showPlayer(plugin, playerToMakeVisible);
        }, 60);
        e.getPlayer().sendMessage(GOOD + "You are now invisible!");
        e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.1f);
        return true;
    }

    @Override
    public Material getItem() {
        return Material.BLAZE_ROD;
    }

    @Override
    public String getCooldownMessage() {
        return "You cannot go invisible yet.";
    }
}
