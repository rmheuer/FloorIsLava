/*
 * This file is part of FloorIsLava.
 *
 * FloorIsLava is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FloorIsLava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FloorIsLava.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.gmail.tracebachi.floorislava.arena;

import com.gmail.tracebachi.floorislava.arena.perks.Perk;
import com.gmail.tracebachi.floorislava.arena.perks.PerkHandler;
import com.gmail.tracebachi.floorislava.booster.Booster;
import com.gmail.tracebachi.floorislava.FloorIsLavaPlugin;
import com.gmail.tracebachi.floorislava.leaderboard.FloorLeaderboard;
import com.gmail.tracebachi.floorislava.utils.*;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.*;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.*;

import static com.gmail.tracebachi.floorislava.utils.Prefixes.BAD;
import static com.gmail.tracebachi.floorislava.utils.Prefixes.GOOD;

/**
 * Created by Trace Bachi (BigBossZee) on 8/20/2015.
 */
public class Arena implements Listener {

    private final FloorIsLavaPlugin plugin;
    private final Booster booster;
    private final FloorLeaderboard floorLeaderboard;

    private final ItemStack winPrize;
    private final ItemStack losePrize;

    private final Map<String, PlayerState> playing = new HashMap<>();
    private final Map<String, Loadout> loadoutMap = new HashMap<>();
    private final Set<String> watching = new HashSet<>();

    private boolean started = false;
    private boolean enabled = true;
    private boolean noPerks;
    private int initialPlayerCount;
    private int wager = 0;
    private int countdown = 0;
    private int elapsedTicks = 0;
    private int degradeLevel = 0;
    private BukkitTask arenaTask;
    private BukkitTask countdownTask;
    private ArenaBlocks arenaBlocks;

    private int minimumPlayers;
    private int minimumRewardPlayers;
    private int baseReward;
    private int winnerReward;
    private int maxCountdown;
    private String worldName;
    private List<String> prestartCommands;
    private List<String> whitelistCommands;
    private int ticksPerCheck;
    private int startDegradeOn;
    private int degradeOn;
    private int disablePerksDegradationLevel;
    private int boosterBroadcastRange;
    private CuboidArea arenaCuboidArea;
    private CuboidArea watchCuboidArea;
    private PerkHandler perkHandler;
    private VoteHandler voteHandler;

    public Arena(FloorIsLavaPlugin plugin) {
        this.plugin = plugin;
        this.booster = new Booster(plugin, this);
        this.floorLeaderboard = new FloorLeaderboard(new File(plugin.getDataFolder(), "leaderboards.yml"));
        this.floorLeaderboard.load();
        this.winPrize = new ItemStack(Material.POTATO);
        this.losePrize = new ItemStack(Material.POISONOUS_POTATO);

        ItemMeta wintatoMeta = winPrize.getItemMeta();
        Objects.requireNonNull(wintatoMeta, "Wintato item meta is null for some weird reasons.")
                .setDisplayName(ChatColor.GOLD + "WinTato");
        List<String> wintatoLore = new ArrayList<>();
        wintatoLore.add("You won a round of FloorIsLava!");
        wintatoLore.add("--");
        wintatoLore.add("May the WinTato be with you - Zee");
        wintatoMeta.setLore(wintatoLore);
        winPrize.setItemMeta(wintatoMeta);

        ItemMeta losetatoMeta = losePrize.getItemMeta();
        Objects.requireNonNull(losetatoMeta, "Losetato item meta is null for some weird reasons.")
                .setDisplayName(ChatColor.RED + "LoseTato");
        List<String> losetatoLore = new ArrayList<>();
        losetatoLore.add("You lost a round of FloorIsLava!");
        losetatoLore.add("--");
        losetatoLore.add("Better luck next time - Zee");
        losetatoMeta.setLore(losetatoLore);
        losePrize.setItemMeta(losetatoMeta);
    }

    public void wager(int amount, Player player) {
        String name = player.getName();
        EconomyResponse response = plugin.getEconomy().withdrawPlayer(player, amount);

        if (!response.transactionSuccess()) {
            player.sendMessage(BAD + "You do not have enough funds to wager that amount.");
            return;
        }

        wager += amount;
        player.sendMessage(GOOD + "You added $" + amount + " to FloorIsLava ( = $" + wager + " )");
        broadcast(GOOD + "+$" + amount + " by " + name + " ( = $" + wager + " )", name);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean containsPlayer(String playerName) {
        return playing.containsKey(playerName);
    }

    public void join(Player player) {
        if (started) {
            player.sendMessage(BAD + "You voted too late. FloorIsLava has already begun.");
            return;
        }

        String playerName = player.getName();

        boolean rewardAllowed = playing.size() + 1 >= minimumRewardPlayers;
        if (rewardAllowed) {
            broadcast(GOOD + playerName + " has joined. There are enough players for rewards.", playerName);
        } else {
            broadcast(GOOD + playerName + " has joined. There are not enough players for rewards.", playerName);
        }
        playing.put(playerName, null);
        resetCountdown();

        if (rewardAllowed)
            player.sendMessage(GOOD + "You have joined FloorIsLava. There are enough players for rewards.");
        else
            player.sendMessage(GOOD + "You have joined FloorIsLava. There are not enough players for rewards.");

        World world = Bukkit.getWorld(worldName);
        Location location = watchCuboidArea.getRandomLocationInside(world);
        location.setYaw(player.getLocation().getYaw());
        location.setPitch(player.getLocation().getPitch());
        player.teleport(location);
    }

    public void watch(Player player) {
        if (!enabled) {
            player.sendMessage(BAD + "Unable to join. FloorIsLava is currently disabled.");
            return;
        }

        watching.add(player.getName());

        player.sendMessage(GOOD + "Teleporting to FloorIsLava viewing area ...");

        World world = Bukkit.getWorld(worldName);
        Location location = watchCuboidArea.getRandomLocationInside(world);
        location.setYaw(player.getLocation().getYaw());
        location.setPitch(player.getLocation().getPitch());
        player.teleport(location);
    }

    public void leave(Player player) {
        String name = player.getName();

        if (watching.contains(name)) {
            watching.remove(name);
            player.sendMessage(GOOD + "You are no longer watching FloorIsLava.");
            return;
        }

        if (!playing.containsKey(name)) {
            return;
        }

        PlayerState state = playing.remove(name);
        Location playerLocation = player.getLocation();

        if (state != null) {
            state.restoreInventory(player);
            state.restoreLocation(player);
            state.restoreGameMode(player);
        }

        if (arenaBlocks.getCuboidArea().isInside(playerLocation)) {
            World world = Bukkit.getWorld(worldName);
            player.teleport(watchCuboidArea.getRandomLocationInside(world));
        }

        if (!started && playing.size() < minimumPlayers) {
            resetCountdown();
        }

        player.sendMessage(GOOD + "You have left FloorIsLava.");
        player.setFireTicks(0);
        player.setHealth(Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH),
                "Turns out players don't have GENERIC_MAX_HEALTH anymore.").getValue());
        if (!started) {
            voteHandler.removeVoteFor(name);
            if (playing.size() >= minimumRewardPlayers)
                broadcast(BAD + name + " has left. There are still enough players for rewards.", null);
            else
                broadcast(BAD + name + " has left. There are no longer enough players for rewards.", null);
        }
    }

    /**************************************************************************
     * Getter Methods
     *************************************************************************/

    public boolean hasStarted() {
        return started;
    }

    public int getWager() {
        return wager;
    }

    public int getPlayingSize() {
        return playing.size();
    }

    public Map<String, Loadout> getLoadoutMap() {
        return loadoutMap;
    }

    public String getWorldName() {
        return worldName;
    }

    public CuboidArea getWatchCuboidArea() {
        return watchCuboidArea;
    }

    public Booster getBooster() {
        return booster;
    }

    public FloorLeaderboard getFloorLeaderboard() {
        return floorLeaderboard;
    }

    public int getBoosterBroadcastRange() {
        return boosterBroadcastRange;
    }

    /**************************************************************************
     * Arena Management Methods
     *************************************************************************/

    public void loadConfig(FileConfiguration config) {
        minimumPlayers = config.getInt("MinimumPlayers");
        minimumRewardPlayers = config.getInt("MinimumRewardPlayers");
        baseReward = config.getInt("BaseReward");
        winnerReward = config.getInt("WinnerReward");
        maxCountdown = config.getInt("CountdownInSeconds");
        worldName = config.getString("WorldName");
        boosterBroadcastRange = config.getInt("BoosterBroadcastRange");
        prestartCommands = config.getStringList("PrestartCommands");
        whitelistCommands = config.getStringList("WhitelistCommands");
        ticksPerCheck = config.getInt("TicksPerCheck");
        startDegradeOn = config.getInt("StartDegradeOn");
        degradeOn = config.getInt("DegradeOnTick");
        disablePerksDegradationLevel = config.getInt("DisablePerksTick");

        arenaCuboidArea = new CuboidArea(
                config.getConfigurationSection("ArenaArea.One"),
                config.getConfigurationSection("ArenaArea.Two"));

        perkHandler = new PerkHandler(arenaCuboidArea, plugin);
        voteHandler = new VoteHandler();

        watchCuboidArea = new CuboidArea(
                config.getConfigurationSection("WaitArea.One"),
                config.getConfigurationSection("WaitArea.Two"));

        arenaBlocks = new ArenaBlocks(arenaCuboidArea);
    }

    public void enableArena(CommandSender sender) {
        enabled = true;
        sender.sendMessage(GOOD + "FloorIsLava enabled.");
    }

    public void disableArena(CommandSender sender) {
        forceStop(sender, true);
        enabled = false;
        sender.sendMessage(GOOD + "FloorIsLava disabled. " +
                "Players will not be able to join until renabled.");
    }

    public void forceStart(CommandSender sender) {
        if (!enabled) {
            sender.sendMessage(BAD + "The arena is currently disabled!");
        } else if (started) {
            sender.sendMessage(BAD + "The arena has already started!");
        } else {
            if (countdownTask != null) {
                countdownTask.cancel();
                countdownTask = null;
            }

            start();
            sender.sendMessage(GOOD + "Force-Started FloorIsLava.");
        }
    }

    public void forceStop(CommandSender sender, boolean recalcLeaderboard) {
        if (started) {
            if (arenaTask != null) {
                arenaTask.cancel();
                arenaTask = null;
            }

            if (countdownTask != null) {
                countdownTask.cancel();
                countdownTask = null;
            }

            for (Map.Entry<String, PlayerState> entry : playing.entrySet()) {
                Player player = Bukkit.getPlayerExact(entry.getKey());
                if (player == null)
                    continue;
                PlayerState state = entry.getValue();
                state.restoreInventory(player);
                state.restoreLocation(player);
                state.restoreGameMode(player);
            }

            int oldWager = wager;
            postStopCleanup(recalcLeaderboard);
            wager = oldWager;
        }

        sender.sendMessage(GOOD + "Force-Stopped FloorIsLava.");
    }

    /**************************************************************************
     * Arena Event Methods
     *************************************************************************/

    @EventHandler
    public void onPlayerChestClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Inventory inventory = player.getInventory();
        String playerName = player.getName();

        if (!started || !playing.containsKey(playerName) || degradeLevel >= disablePerksDegradationLevel) {
            return;
        }
        Block clickedBlock = event.getClickedBlock();
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && clickedBlock != null &&
                clickedBlock.getType().equals(Material.CHEST)) {
            event.setCancelled(true);
            FireworkEffect effect = FireworkEffect.builder()
                    .flicker(true)
                    .trail(false)
                    .with(Type.STAR)
                    .withColor(Color.GREEN)
                    .withFade(Color.WHITE)
                    .build();
            FireworkSpark.spark(effect, clickedBlock.getLocation());
            clickedBlock.setType(Material.AIR);
            if (isNoPerks()) {
                event.getPlayer().sendMessage(BAD + "Since this is a no-perk game, you have been given no perks.");
                return;
            }
            player.sendMessage(GOOD + "You have collected a treasure chest, enjoy your items!");
            //TODO should probably be optimized later on
            Random random = new Random();
            ItemStack[] newItems = new ItemStack[]{
                    Loadout.BOOST_ITEM.clone(),
                    Loadout.CHIKUN_ITEM.clone(),
                    Loadout.HOOK_ITEM.clone(),
                    Loadout.INVIS_ITEM.clone(),
                    Loadout.STEAL_ITEM.clone(),
                    Loadout.WEB_ITEM.clone(),
                    Loadout.TNT_ITEM.clone()};
            int i, choice, itemIndex;
            ItemStack item;
            int chestItemAmount = 2;
            for (i = 0; i < chestItemAmount; i++) {
                choice = random.nextInt(newItems.length);
                itemIndex = inventory.first(newItems[choice].getType());
                if (itemIndex == -1)
                    inventory.addItem(newItems[choice]);
                else {
                    item = inventory.getItem(itemIndex);
                    item.setAmount(item.getAmount() + 1);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerBlockClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        ItemStack heldItem = event.getItem();
        Block clickedBlock = event.getClickedBlock();
        if (!started || !playing.containsKey(playerName)) {
            return;
        }
        event.setCancelled(true);
        if (event.getAction() == Action.LEFT_CLICK_BLOCK && clickedBlock != null) {
            Location clicked = clickedBlock.getLocation();
            if (arenaBlocks.getCuboidArea().isInside(clicked)) {
                clickedBlock.setType(Material.AIR);
            }
            return;
        }
        if (heldItem == null || (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK))
            return;

        if (isNoPerks()) {
            event.getPlayer().sendMessage(BAD + "You cannot use perks in no-perk mode!");
            return;
        }

        Perk perk = perkHandler.getPerkFromMaterial(heldItem.getType());
        if (perk == null) return;
        perk.activate(event, null);
        player.updateInventory();
    }

    @EventHandler
    public void onPlayerInteractWithPlayer(PlayerInteractEntityEvent event) {
        Entity rightClickedEntity = event.getRightClicked();
        if (!(rightClickedEntity instanceof Player))
            return;
        Player player = event.getPlayer();
        String playerName = player.getName();
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (!started || !playing.containsKey(playerName)) return;
        event.setCancelled(true);
        if (isNoPerks()) {
            event.getPlayer().sendMessage(BAD + "You cannot use perks in no-perk mode!");
            return;
        }
        Perk perk = perkHandler.getPerkFromMaterial(heldItem.getType());
        if (perk == null)
            return;
        perk.activate(null, event);
        player.updateInventory();
    }

    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            String name = event.getEntity().getName();
            if (started && playing.containsKey(name)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        String name = event.getPlayer().getName();
        if (started && playing.containsKey(name)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDragItem(InventoryDragEvent event) {
        String name = event.getWhoClicked().getName();
        if (started && playing.containsKey(name)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClickArenaStarted(InventoryClickEvent event) {
        String name = event.getWhoClicked().getName();
        if (started && playing.containsKey(name)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityExplode(EntityExplodeEvent event) {
        List<Block> blocksToBeDestroyed = event.blockList();
        if (!event.getEntity().hasMetadata("FIL"))
            return;
        event.setCancelled(true);
        for (Block block : blocksToBeDestroyed) {
            if (arenaCuboidArea.isInside(block.getLocation())) {
                if (started) {
                    block.setType(Material.AIR);
                }
            }
        }
        blocksToBeDestroyed.clear();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        Entity entity = event.getEntity();
        if (!event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.EGG) ||
                !arenaCuboidArea.isInside(entity.getLocation()) ||
                !event.getEntity().getType().equals(EntityType.CHICKEN))
            return;
        event.setCancelled(false);
        entity.setCustomNameVisible(true);
        entity.setCustomName(ChatColor.LIGHT_PURPLE + "\\o/ CHIKUN \\o/");
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            FireworkEffect effect = FireworkEffect.builder()
                    .flicker(true)
                    .trail(false)
                    .with(Type.STAR)
                    .withColor(Color.GREEN)
                    .withFade(Color.WHITE)
                    .build();
            FireworkSpark.spark(effect, entity.getLocation());
            entity.remove();
        }, 200);
    }

    @EventHandler
    public void onPlayerEggThrow(PlayerEggThrowEvent event) {
        Egg egg = event.getEgg();
        Location location = egg.getLocation();
        Player player = event.getPlayer();
        if (!playing.containsKey(player.getName()))
            return;
        event.setHatching(true);
        event.setNumHatches((byte) 4);
        FireworkEffect effect = FireworkEffect.builder()
                .flicker(true)
                .trail(false)
                .with(Type.STAR)
                .withColor(Color.GREEN)
                .withFade(Color.WHITE)
                .build();
        FireworkSpark.spark(effect, location);
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntityType().equals(EntityType.PLAYER)) {
            String name = event.getEntity().getName();
            if (started && playing.containsKey(name))
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDamageByEntity(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        if (event.getEntityType().equals(EntityType.PLAYER) && damager instanceof TNTPrimed) {
            if (damager.hasMetadata("FIL")) {
                event.setCancelled(true);
                event.setDamage(0);
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!enabled)
            return;
        Player player = event.getPlayer();
        String playerName = player.getName();
        if (watching.contains(playerName) || (!started && playing.containsKey(playerName))) {
            if (!watchCuboidArea.isInside(player.getLocation())) {
                leave(player);
            }
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        Location locTo = event.getTo();
        if (locTo == null)
            return;
        if (arenaCuboidArea.isInside(locTo) &&
                !playing.containsKey(playerName) &&
                !player.hasPermission("FloorIsLava.Staff"))
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        if (playing.containsKey(playerName)) {
            String word = event.getMessage().split("\\s+")[0];
            if (player.hasPermission("FloorIsLava.Staff") || whitelistCommands.contains(word))
                return;
            player.sendMessage(BAD + "That command is not allowed while in FloorIsLava.");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        if (playing.containsKey(playerName))
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        leave(event.getPlayer());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!started && playing.containsKey(player.getName()) &&
                player.getInventory().getItemInMainHand().getType() == Material.TRIDENT &&
                (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK))
            event.setCancelled(true);
    }

    /*************************************************************************
     * Private Methods
     *************************************************************************/

    private void start() {
        if (started)
            throw new IllegalStateException("Arena#start() called while arena is running!");
        World world = Bukkit.getWorld(worldName);
        arenaBlocks.save(world);
        VoteHandler.VoteType type = voteHandler.choose();
        this.noPerks = type == VoteHandler.VoteType.NO_PERKS;
        Iterator<Map.Entry<String, PlayerState>> iter = playing.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, PlayerState> entry = iter.next();
            Player player = Bukkit.getPlayerExact(entry.getKey());
            if (player == null) {
                iter.remove();
            } else {
                String name = player.getName();
                Loadout loadout = loadoutMap.get(name);
                PlayerState playerState = new PlayerState();
                player.closeInventory();
                playerState.save(player);
                playing.put(entry.getKey(), playerState);

                for (PotionEffect e : player.getActivePotionEffects())
                    player.removePotionEffect(e.getType());
                for (String command : prestartCommands)
                    Bukkit.getServer().dispatchCommand(player, command);

                player.getInventory().setHelmet(null);
                player.getInventory().setChestplate(null);
                player.getInventory().setLeggings(null);
                player.getInventory().setBoots(null);
                player.teleport(arenaCuboidArea.getRandomLocationInside(world));
                if (!isNoPerks()) {
                    player.getInventory().setStorageContents(getContentsFromLoadout(loadout));
                    player.sendMessage(GOOD + "This is a " + ChatColor.DARK_PURPLE + "perk "
                            + ChatColor.GREEN + "round.");
                } else {
                    player.getInventory().setStorageContents(new ItemStack[0]);
                    player.sendMessage(GOOD + "This is a " + ChatColor.RED + "no perk "
                            + ChatColor.GREEN + "round.");
                }

                player.getInventory().setExtraContents(new ItemStack[0]);
            }
        }
        arenaTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 1, ticksPerCheck);
        started = true;
        initialPlayerCount = playing.size();
    }

    private void tick() {
        Iterator<Map.Entry<String, PlayerState>> iter = playing.entrySet().iterator();
        World world = Bukkit.getWorld(worldName);
        boolean boosterActive = booster.isActive();
        int scaledBaseReward = (boosterActive ? baseReward * 2 : baseReward);
        int scaledWinnerReward = (boosterActive ? winnerReward * 2 : winnerReward);

        losePrize.setAmount(boosterActive ? 2 : 1);
        winPrize.setAmount(boosterActive ? 2 : 1);

        while (iter.hasNext()) {
            Map.Entry<String, PlayerState> entry = iter.next();
            Player player = Bukkit.getPlayerExact(entry.getKey());
            PlayerState state = entry.getValue();
            if (player == null) {
                iter.remove();
                continue;
            }
            Location location = player.getLocation();
            if (!arenaCuboidArea.isInside(location)) {
                iter.remove();
                state.restoreInventory(player);
                state.restoreLocation(player);
                state.restoreGameMode(player);
                player.setFireTicks(0);
                player.setHealth(Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH),
                        "Players apparently no longer have GENERIC_MAX_HEALTH.").getValue());
                player.sendMessage(GOOD + "Thanks for playing!");
                if (shouldReward()) {
                    player.getInventory().addItem(losePrize);
                    plugin.getEconomy().depositPlayer(player, scaledBaseReward);
                }
                player.teleport(watchCuboidArea.getRandomLocationInside(world));
                watching.add(player.getName());
                broadcast(BAD + entry.getKey() + " fell! " + playing.size() + " left!");
            }
        }
        if (playing.size() > 1) {
            if (elapsedTicks >= startDegradeOn && (elapsedTicks % degradeOn) == 0) {
                arenaBlocks.degradeBlocks(world, degradeLevel);
                degradeLevel++;
            }
            if (degradeLevel == disablePerksDegradationLevel)
                removePerks();
            elapsedTicks++;
            return;
        }
        for (Map.Entry<String, PlayerState> entry : playing.entrySet()) {
            Player player = Bukkit.getPlayerExact(entry.getKey());
            if (player == null)
                continue;
            PlayerState state = entry.getValue();
            state.restoreInventory(player);
            state.restoreLocation(player);
            state.restoreGameMode(player);
            player.setFireTicks(0);
            player.setHealth(Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH),
                    "Players apparently no longer have GENERIC_MAX_HEALTH.").getValue());
            if (shouldReward()) {
                floorLeaderboard.addOneToScore(entry.getKey());
                plugin.getLogger().info(entry.getKey() + " won a round. Amount = " + (scaledWinnerReward + wager));
                player.sendMessage(GOOD + "You won! Here's a prize and $" + (scaledWinnerReward + wager));
                broadcast(GOOD + entry.getKey() + " won that round and a prize of $" +
                        (scaledWinnerReward + wager), player.getName());
                player.getInventory().addItem(winPrize);
                plugin.getEconomy().depositPlayer(player, (scaledWinnerReward + wager));
            } else {
                if (wager != 0) {
                    plugin.getLogger().info(entry.getKey() + " won a free round. Wager amount = " + wager);
                    player.sendMessage(GOOD + "You won a free round! Here's the wager: $" + wager);
                    broadcast(GOOD + entry.getKey() + " won that round and a wager of $" + wager, player.getName());
                    plugin.getEconomy().depositPlayer(player, wager);
                } else {
                    plugin.getLogger().info(entry.getKey() + " won a free round.");
                    player.sendMessage(GOOD + "You won a free round!");
                    broadcast(GOOD + entry.getKey() + " won that round!", player.getName());
                }
            }
            wager = 0;
            Firework firework = player.getWorld().spawn(
                    player.getLocation().add(0, 1, 0),
                    Firework.class);
            FireworkMeta fireworkMeta = firework.getFireworkMeta();
            fireworkMeta.addEffects(FireworkEffect.builder()
                    .flicker(false)
                    .trail(true)
                    .with(Type.BALL_LARGE)
                    .withColor(Color.BLUE)
                    .withFade(Color.WHITE)
                    .build());
            firework.setFireworkMeta(fireworkMeta);
        }
        postStopCleanup(true);
    }

    private void removePerks() {
        for (Map.Entry<String, PlayerState> entry: playing.entrySet()) {
            Player player = Bukkit.getPlayerExact(entry.getKey());
            if (player == null)
                continue;
            player.getInventory().clear();
        }
    }

    private boolean shouldReward() {
        return initialPlayerCount >= minimumRewardPlayers;
    }

    private void countdownTick() {
        if (countdown <= 0) {
            countdownTask.cancel();
            countdownTask = null;
            start();
        } else {
            broadcast(GOOD + "Starting in " + countdown);
            countdown--;
        }
    }

    private void resetCountdown() {
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
        if (playing.size() >= minimumPlayers) {
            countdown = maxCountdown;
            countdownTask = Bukkit.getScheduler().runTaskTimer(plugin,
                    this::countdownTick, 200, 20);
        } else {
            broadcast(BAD + "Too few players to start.", null);
        }
    }

    private void postStopCleanup(boolean recalcLeaderbaord) {
        degradeLevel = 0;
        elapsedTicks = 0;
        initialPlayerCount = 0;
        for (String playerName : playing.keySet()) {
            Player player = Bukkit.getPlayerExact(playerName);
            if (player != null) {
                for (Player other : Bukkit.getOnlinePlayers()) {
                    other.showPlayer(plugin, player);
                }
            }
        }
        playing.clear();
        watching.clear();
        arenaBlocks.restore();
        if (arenaTask != null) {
            arenaTask.cancel();
            arenaTask = null;
        }
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
        if (recalcLeaderbaord)
            floorLeaderboard.recalculate();
        perkHandler = new PerkHandler(arenaCuboidArea, plugin);
        voteHandler.resetVotes();
        started = false;
    }

    private void broadcast(String message) {
        broadcast(message, null);
    }

    private void broadcast(String message, String exclude) {
        for (String name : watching) {
            if (!name.equalsIgnoreCase(exclude)) {
                Player target = Bukkit.getPlayerExact(name);
                if (target != null) {
                    target.sendMessage(message);
                }
            }
        }
        for (String name : playing.keySet()) {
            if (!name.equalsIgnoreCase(exclude)) {
                Player target = Bukkit.getPlayerExact(name);
                if (target != null)
                    target.sendMessage(message);
            }
        }
    }

    private ItemStack[] getContentsFromLoadout(Loadout loadout) {
        ItemStack[] contents = new ItemStack[36];
        int c = 0;
        if (loadout == null)
            return contents;
        if (loadout.tnt > 0) {
            contents[c] = Loadout.TNT_ITEM.clone();
            contents[c].setAmount(loadout.tnt);
            c++;
        }
        if (loadout.hook > 0) {
            contents[c] = Loadout.HOOK_ITEM.clone();
            contents[c].setAmount(loadout.hook);
            c++;
        }
        if (loadout.web > 0) {
            contents[c] = Loadout.WEB_ITEM.clone();
            contents[c].setAmount(loadout.web);
            c++;
        }
        if (loadout.invis > 0) {
            contents[c] = Loadout.INVIS_ITEM.clone();
            contents[c].setAmount(loadout.invis);
            c++;
        }
        if (loadout.boost > 0) {
            contents[c] = Loadout.BOOST_ITEM.clone();
            contents[c].setAmount(loadout.boost);
            c++;
        }
        if (loadout.chikun > 0) {
            contents[c] = Loadout.CHIKUN_ITEM.clone();
            contents[c].setAmount((loadout.chikun));
            c++;
        }
        if (loadout.steal > 0) {
            contents[c] = Loadout.STEAL_ITEM.clone();
            contents[c].setAmount((loadout.steal));
        }
        return contents;
    }

    public boolean isNoPerks() {
        return noPerks;
    }

    public VoteHandler getVoteHandler() {
        return voteHandler;
    }
}
