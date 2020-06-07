package com.gmail.tracebachi.floorislava.gui;

import com.gmail.tracebachi.floorislava.arena.VoteHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

public class FloorModeVoteMenu {

    public static final ItemStack YES_PERKS_ITEM = new ItemStack(Material.LEATHER_CHESTPLATE);
    public static final ItemStack NO_PERKS_ITEM = new ItemStack(Material.BARRIER);

    private final Inventory inventory;

    public FloorModeVoteMenu() {
        this.inventory = Bukkit.createInventory(null, 27, "Vote for the mode this round");
    }

    public void showTo(Player player) {
        inventory.setItem(11, YES_PERKS_ITEM);
        inventory.setItem(15, NO_PERKS_ITEM);
        player.openInventory(inventory);
    }

    static {
        ItemMeta meta = YES_PERKS_ITEM.getItemMeta();
        if (meta == null)
            throw new NullPointerException("The perk mode vote item meta is null for some reasons.");
        meta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Perks");
        meta.setLore(Collections.singletonList(ChatColor.WHITE + "Click to vote for perks in this round."));
        YES_PERKS_ITEM.setItemMeta(meta);

        meta = NO_PERKS_ITEM.getItemMeta();
        if (meta == null)
            throw new NullPointerException("The no perk mode vote item meta is null for some reasons.");
        meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "No Perks");
        meta.setLore(Collections.singletonList(ChatColor.WHITE + "Click to vote for NO perks in this round."));
        NO_PERKS_ITEM.setItemMeta(meta);
    }
}
