package com.gmail.tracebachi.floorislava.arena.perks;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.gmail.tracebachi.floorislava.utils.Prefixes.BAD;

public abstract class Perk {
    private long delay;
    private final Map<UUID, Long> playerDelayCache = new HashMap<>();

    public abstract boolean onPerkActivation(PlayerInteractEvent interactEvent, PlayerInteractEntityEvent entityEvent);

    public abstract Material getItem();

    public abstract String getCooldownMessage();

    public void activate(PlayerInteractEvent interactEvent, PlayerInteractEntityEvent entityEvent) {
        long now = System.currentTimeMillis();
        Player player = entityEvent == null ? interactEvent.getPlayer() : entityEvent.getPlayer();
        if (!playerDelayCache.containsKey(player.getUniqueId()))
            this.putInCache(player.getUniqueId(), now);

        if (now - playerDelayCache.get(player.getUniqueId()) < delay) { // At this point, the key ALWAYS exists.
            player.sendMessage(BAD + this.getCooldownMessage());
            return;
        }

        /*
           interactEvent is null if the perk is activated through a PlayerEntityInteractEvent
           entityEvent is null if the perk is activated through a PlayerEntityInteractEvent
        */
        if (!this.onPerkActivation(interactEvent, entityEvent)) return;
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
