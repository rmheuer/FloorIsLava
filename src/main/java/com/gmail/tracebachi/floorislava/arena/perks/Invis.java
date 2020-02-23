package com.gmail.tracebachi.floorislava.arena.perks;

import com.gmail.tracebachi.floorislava.FloorIsLavaPlugin;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import static com.gmail.tracebachi.floorislava.utils.Prefixes.BAD;
import static com.gmail.tracebachi.floorislava.utils.Prefixes.GOOD;

public class Invis extends Perk {
    private final FloorIsLavaPlugin plugin;

    public Invis(FloorIsLavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onPerkActivation(Player player, Action clickAction, Block clickedBlock, PlayerInteractEvent e, Player rightClicked) {
        for (Player other : Bukkit.getOnlinePlayers())
            other.hidePlayer(plugin, player);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Player playerToMakeVisible = Bukkit.getPlayerExact(player.getName()); // So we have no logout issues.
            if (playerToMakeVisible == null)
                return;
            playerToMakeVisible.sendMessage(BAD + "You are now visible!");
            player.playNote(playerToMakeVisible.getLocation(), Instrument.PIANO, Note.natural(1, Note.Tone.A));
            for (Player other : Bukkit.getOnlinePlayers())
                other.showPlayer(plugin, playerToMakeVisible);
        }, 60);
        player.sendMessage(GOOD + "You are now invisible!");
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.1f);
        return true;
    }

    @Override
    public Material getItem() {
        return Material.END_ROD;
    }
}
