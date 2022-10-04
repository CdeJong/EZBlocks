package me.clip.ezblocks.block;

import me.clip.ezblocks.EZBlocks;
import me.clip.ezblocks.storage.Storage;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BlockControllerImpl implements BlockController, Listener {

    private final EZBlocks plugin;
    private final Storage storage;
    private final Map<UUID, Integer> localBlocksBroken = new HashMap<>();
    private Map<Integer, BlockTopEntry> localBlocksTop = new HashMap<>();
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public BlockControllerImpl(EZBlocks plugin) {
        this.plugin = plugin;
        this.storage = plugin.getStorage();

        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        executorService.scheduleAtFixedRate(this::updateStorage, 0L, 5L, TimeUnit.MINUTES); // todo add saveInterval from config
        //todo add scheduler for updateting blockstop
    }

    @Override
    public void setBlocksBroken(Player player, int amount) {
        localBlocksBroken.put(player.getUniqueId(), amount);
    }

    @Override
    public int getBlocksBroken(Player player) {
        Integer blocksBroken = localBlocksBroken.get(player.getUniqueId());
        if (blocksBroken == null) {
            blocksBroken = storage.getBlocksBroken(player.getUniqueId());
            localBlocksBroken.put(player.getUniqueId(), blocksBroken);
        }
        return blocksBroken;
    }

    @Override
    public BlockTopEntry getBlocksTop(int position) {
        return localBlocksTop.get(position);
    }

    @Override
    public Map<Integer, BlockTopEntry> getBlocksTop() {
        return Collections.unmodifiableMap(localBlocksTop);
    }

    private void updateStorage() {
        // save current
        localBlocksBroken.forEach(storage::setBlocksBroken);

        // load blockstop
        localBlocksTop = storage.getBlocksTop();
    }

    @Override
    public void close() {
        executorService.shutdown();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        UUID uuid = event.getPlayer().getUniqueId();

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {

            if (localBlocksBroken.containsKey(uuid)) {
                return; // already loaded
            }
            localBlocksBroken.put(uuid, storage.getBlocksBroken(uuid));

        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {

        UUID uuid = event.getPlayer().getUniqueId();

        if (localBlocksBroken.containsKey(uuid)) {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                plugin.getStorage().setBlocksBroken(uuid, localBlocksBroken.get(uuid));
            });
            localBlocksBroken.remove(uuid);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {

        if (check(event.getPlayer(), event.getBlock())) {
            handleBlockBreakEvent(event.getPlayer(), event.getBlock());
        }
    }

    private boolean isAllowedBlock(Material material) { // todo move to Config

        if (plugin.getOptions().getBlacklistedBlocks() == null
                || plugin.getOptions().getBlacklistedBlocks().isEmpty()) {
            return true;
        }

        if (plugin.getOptions().blacklistIsWhitelist()) {
            return plugin.getOptions().getBlacklistedBlocks().contains(material.toString());
        } else {
            return !plugin.getOptions().getBlacklistedBlocks().contains(material.toString());
        }
    }

    private boolean isTool(ItemStack item) { // todo move to Config

        return plugin.getOptions().getTrackedTools() != null
                && plugin.getOptions().getTrackedTools().contains(item.getType().name());
    }

    private String getName(ItemStack item) { // todo find a better solution. reflection?
        String type = null;
        switch (item.getType().name()) {
            case "WOOD_PICKAXE":
            case "WOODEN_PICKAXE":
                type = "Wood Pickaxe";
                break;
            case "STONE_PICKAXE":
                type = "Stone Pickaxe";
                break;
            case "IRON_PICKAXE":
                type = "Iron Pickaxe";
                break;
            case "GOLD_PICKAXE":
                type = "Golden Pickaxe";
                break;
            case "DIAMOND_PICKAXE":
                type = "Diamond Pickaxe";
                break;
            case "WOOD_AXE":
            case "WOODEN_AXE":
                type = "Wood Axe";
                break;
            case "STONE_AXE":
                type = "Stone Axe";
                break;
            case "IRON_AXE":
                type = "Iron Axe";
                break;
            case "GOLD_AXE":
                type = "Golden Axe";
                break;
            case "DIAMOND_AXE":
                type = "Diamond Axe";
                break;
            case "WOOD_SPADE":
            case "WOODEN_SHOVEL":
                type = "Wood Spade";
                break;
            case "STONE_SPADE":
            case "STONE_SHOVEL":
                type = "Stone Spade";
                break;
            case "IRON_SPADE":
            case "IRON_SHOVEL":
                type = "Iron Spade";
                break;
            case "GOLD_SPADE":
            case "GOLDEN_SHOVEL":
                type = "Golden Spade";
                break;
            case "DIAMOND_SPADE":
            case "DIAMOND_SHOVEL":
                type = "Diamond Spade";
                break;
        }

        if (type == null) {
            return item.getType().name();
        }

        return type;
    }

    @SuppressWarnings("deprecation")
    public boolean check(Player player, Block block) {
        if (!isAllowedBlock(block.getType())) {
            return false;
        }

        ItemStack item = player.getInventory().getItemInHand(); // getItemInMainHand()

        if (item == null) {
            return false;
        }

        if (!isTool(item)) {
            return false;
        }

        if (plugin.getOptions().survivalOnly() && !player.getGameMode().equals(GameMode.SURVIVAL)) {
            return false;
        }

        if (!plugin.getOptions().getEnabledWorlds().contains(player.getWorld().getName())
                && !plugin.getOptions().getEnabledWorlds().contains("all")) {
            return false;
        }

        if (plugin.getOptions().onlyBelowY()
                && block.getLocation().getBlockY() > plugin.getOptions().getBelowYCoord()) {
            return false;
        }

        return true;
    }

    @SuppressWarnings("deprecation")
    private void handleBlockBreakEvent(Player player, final Block block) {

        ItemStack item = player.getInventory().getItemInHand();

        UUID uuid = player.getUniqueId();

        int blocksBroken;

        if (!localBlocksBroken.containsKey(uuid)) {

            blocksBroken = plugin.getStorage().getBlocksBroken(uuid) + 1; // getBlocksBroken returns 0 if it has no data.

        } else {

            blocksBroken = localBlocksBroken.get(uuid) + 1;
        }

        localBlocksBroken.put(uuid, blocksBroken);

        plugin.getRewardHandler().giveReward(player, blocksBroken);

        plugin.getRewardHandler().giveIntervalReward(player, blocksBroken);

        if (plugin.getOptions().pickaxeNeverBreaks()) {
            item.setDurability((short) 0);
        }

        if (plugin.getOptions().usePickCounter() && player.hasPermission("ezblocks.pickaxecounter")) {
            handlePickCounter(player, item);
        }

    }

    private void handlePickCounter(Player player, ItemStack item) {
        // todo redo this
        /*

        blocks should be saved in nbt, so other plugins / anvils cannot reset the count (that easily).

        this way we don't need the format/trim/parse stuff

         */

        //String format = ChatColor.translateAlternateColorCodes('&', plugin.getOptions().getPickCounterFormat());
        //int one = format.indexOf('%');
        //int two = format.lastIndexOf('%');
        //String first = format.substring(0, one);
        //String second = format.substring(two+1);
        //
        //if (plugin.getOptions().usePickCounterDisplayName()) {
        //
        //    ItemMeta meta = item.getItemMeta();
        //    int blocksBroken = 1;
        //
        //    if (meta != null && item.getItemMeta().hasDisplayName()) {
        //
        //        String displayName = meta.getDisplayName();
        //
        //        if (displayName.startsWith(first) && displayName.endsWith(second)) {
        //
        //            String f = displayName.replace(first, "");
        //            f = f.replace(second, "").trim();
        //            int amt = getInt(f);
        //            breaks = amt+1;
        //            meta.setDisplayName(format.replace("%blocks%", String.valueOf(breaks)));
        //            i.setItemMeta(meta);
        //            plugin.getRewardHandler().givePickaxeReward(p, breaks);
        //            plugin.getRewardHandler().givePickaxeIntervalReward(p, breaks);
        //
        //        } else if (displayName.contains(" "+first) && displayName.endsWith(second)) {
        //
        //            int split = displayName.indexOf(first, 0);
        //            String name = displayName.substring(0, split);
        //            String f = displayName.substring(split);
        //            f = f.replace(first, "");
        //            f = f.replace(second, "").trim();
        //
        //            int amt = getInt(f);
        //            breaks = amt+1;
        //            meta.setDisplayName(name+format.replace("%blocks%", String.valueOf(breaks)));
        //            i.setItemMeta(meta);
        //            plugin.getRewardHandler().givePickaxeReward(p, breaks);
        //            plugin.getRewardHandler().givePickaxeIntervalReward(p, breaks);
        //
        //        } else {
        //
        //            meta.setDisplayName(displayName+" "+format.replace("%blocks%", "1"));
        //            i.setItemMeta(meta);
        //            plugin.getRewardHandler().givePickaxeReward(p, 1);
        //            plugin.getRewardHandler().givePickaxeIntervalReward(p, 1);
        //        }
        //
        //    } else {
        //
        //        String type = getName(i);
        //
        //        meta.setDisplayName(type+" "+format.replace("%blocks%", "1"));
        //        i.setItemMeta(meta);
        //        plugin.getRewardHandler().givePickaxeReward(p, 1);
        //        plugin.getRewardHandler().givePickaxeIntervalReward(p, 1);
        //    }
        //
        //} else {
        //
        //    if (i.hasItemMeta() && i.getItemMeta().hasLore()) {
        //
        //        int breaks = 0;
        //        boolean contains = false;
        //        List<String> lore = meta.getLore();
        //        List<String> newLore = new ArrayList<String>();
        //
        //        for (String line : lore) {
        //
        //            if (line.startsWith(first) && line.endsWith(second)) {
        //
        //                contains = true;
        //                String amount = line.replace(first, "").replace(second, "");
        //
        //                breaks = getInt(amount);
        //
        //                newLore.add(format.replace("%blocks%", String.valueOf(breaks+1)));
        //
        //            } else {
        //
        //                newLore.add(line);
        //            }
        //        }
        //
        //        if (!contains) {
        //
        //            newLore.add(format.replace("%blocks%", "1"));
        //        }
        //
        //        meta.setLore(newLore);
        //        i.setItemMeta(meta);
        //        plugin.getRewardHandler().givePickaxeReward(p, breaks);
        //        plugin.getRewardHandler().givePickaxeIntervalReward(p, breaks);
        //
        //    } else {
        //
        //        List<String> lore = new ArrayList<String>();
        //        lore.add(format.replace("%blocks%", "1"));
        //        meta.setLore(lore);
        //        i.setItemMeta(meta);
        //        plugin.getRewardHandler().givePickaxeReward(p, 1);
        //        plugin.getRewardHandler().givePickaxeIntervalReward(p, 1);
        //
        //    }
        //}
    }
}
