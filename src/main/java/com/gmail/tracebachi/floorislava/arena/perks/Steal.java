package com.gmail.tracebachi.floorislava.arena.perks;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Random;

import static com.gmail.tracebachi.floorislava.utils.Prefixes.BAD;
import static com.gmail.tracebachi.floorislava.utils.Prefixes.GOOD;

public class Steal extends Perk {
    @Override
    public boolean onPerkActivation(Player player, Action clickAction, Block clickedBlock, PlayerInteractEvent interactEvent, Player rightClicked) {
        Random random = new Random();
        int chance = random.nextInt(100);
        if (chance < 50) {
            player.sendMessage(BAD + "Badluck! Your attempt to steal an ability has backfired.");
            if (playerHasNoItems(player)) {
                player.sendMessage(BAD + "It appears you do not have any abilities left...");
                launchThief(player);
            } else {
                takeAbility(null, player);
                player.sendMessage(BAD + "A random ability has been taken away from you!");
            }
        } else {
            if (playerHasNoItems(rightClicked)) {
                player.sendMessage(BAD + "It appears your victim does not have any abilities left...");
                launchThief(player);
            } else {
                takeAbility(player, rightClicked);
                player.sendMessage(GOOD + "A random ability has been taken away from " + rightClicked.getName() + "!");
                rightClicked.sendMessage(BAD + "A random ability has been stolen by " + player.getName() + "!");
            }
        }
        return true;
    }

    @Override
    public Material getItem() {
        return Material.FLINT_AND_STEEL;
    }

    private boolean playerHasNoItems(Player player) {
        for (ItemStack itemStack : player.getInventory().getStorageContents()) {
            if (itemStack != null && !itemStack.getType().equals(Material.AIR))
                return true;
        }
        return false;
    }

    private void launchThief(Player player) {
        player.sendMessage(BAD + "Go away, thief!");
        Vector dir = player.getLocation().getDirection();
        Vector vec = new Vector(-dir.getX() * 10.0D, 0.6D, -dir.getZ() * 10.0D);
        player.setVelocity(vec);
    }

    private void takeAbility(Player to, Player from) {
        Random random = new Random();
        int randomAbilitySlot = random.nextInt(7);
        while (from.getInventory().getStorageContents()[randomAbilitySlot] == null ||
                from.getInventory().getStorageContents()[randomAbilitySlot].getType().equals(Material.AIR))
            randomAbilitySlot = random.nextInt(7);

        ItemStack takenAway = from.getInventory().getStorageContents()[randomAbilitySlot];
        if (takenAway.getAmount() == 1)
            from.getInventory().remove(takenAway);
        else
            takenAway.setAmount(takenAway.getAmount() - 1);
        ItemStack toGive = takenAway.clone();
        toGive.setAmount(1);
        if (to != null)
            to.getInventory().addItem(toGive);
    }
}
