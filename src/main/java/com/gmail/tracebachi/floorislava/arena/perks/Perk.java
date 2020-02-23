package com.gmail.tracebachi.floorislava.arena.perks;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;

import static com.gmail.tracebachi.floorislava.utils.Prefixes.BAD;

public abstract class Perk {
    private long delay;
    private Map<UUID, Long> playerDelayCache;

    public abstract boolean onPerkActivation(Player player, Action clickAction, Block clickedBlock, PlayerInteractEvent interactEvent, Player rightClicked);

    public abstract Material getItem();

    // TODO make a PerkActivateEvent as the argument number is growing.
    // TODO cooldown message
    public void activate(Player player, Action clickAction, Block clickedBlock, PlayerInteractEvent interactEvent, Player rightClicked) {
        long now = System.currentTimeMillis();
        if (!playerDelayCache.containsKey(player.getUniqueId()))
            this.putInCache(player.getUniqueId(), now);

        if (now - playerDelayCache.get(player.getUniqueId()) < delay) { // At this point, the key ALWAYS exists.
            player.sendMessage(BAD + "You cannot place TNT yet.");
            return;
        }

        /* clickedBlock is null if clickAction == Action.RIGHT_CLICK_AIR
           interactEvent is null if the perk is activated through a PlayerEntityInteractEvent
        */
        if (!this.onPerkActivation(player, clickAction, clickedBlock, interactEvent, rightClicked)) return;
        decrementAmountOfItemStack(player.getInventory(), player.getInventory().getItemInMainHand());
        this.putInCache(player.getUniqueId(), now);
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public void putInCache(UUID playerUuid, long currentTime) {
        long oldDelay = 0;
        if (playerDelayCache.containsKey(playerUuid)) oldDelay = playerDelayCache.get(playerUuid);
        this.playerDelayCache.put(playerUuid, currentTime + oldDelay);
    }

    private void decrementAmountOfItemStack(Inventory inventory, ItemStack itemStack) {
        if (itemStack.getAmount() == 1)
            inventory.remove(itemStack);
        else
            itemStack.setAmount(itemStack.getAmount() - 1);
    }
}
