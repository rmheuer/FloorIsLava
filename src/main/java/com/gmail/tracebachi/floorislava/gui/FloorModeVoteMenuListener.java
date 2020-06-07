package com.gmail.tracebachi.floorislava.gui;

import com.gmail.tracebachi.floorislava.arena.Arena;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class FloorModeVoteMenuListener implements Listener {
    private final Arena arena;

    public FloorModeVoteMenuListener(Arena arena) {
        this.arena = arena;
    }

    @EventHandler
    public void onPlayerInteract(InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();
        Player player = (Player) event.getWhoClicked();

        if (!event.getView().getTitle().equals("Vote for the mode this round"))
            return;
        if (clickedItem == null)
            return;

        if (matchesItemStack(FloorModeVoteMenu.YES_PERKS_ITEM, clickedItem)) {
            player.closeInventory();
            arena.getVoteHandler().addVoteForPerks(player.getName());
        } else if (matchesItemStack(FloorModeVoteMenu.YES_PERKS_ITEM, clickedItem)) {
            player.closeInventory();
            arena.getVoteHandler().addVoteForPerks(player.getName());
        }
        arena.join(player);
    }

    private boolean matchesItemStack(ItemStack original, ItemStack input) {
        if (original == null || input == null)
            return false;

        if (input.getType() == original.getType()) {
            ItemMeta originalMeta = original.getItemMeta();
            ItemMeta inputMeta = input.getItemMeta();
            if (originalMeta != null && inputMeta != null) {
                if (originalMeta.hasDisplayName() && inputMeta.hasDisplayName())
                    return originalMeta.getDisplayName().equals(inputMeta.getDisplayName());
            } else {
                return (originalMeta != null) == (inputMeta != null);
            }
        }
        return false;
    }
}
