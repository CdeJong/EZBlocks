package me.clip.ezblocks;

import me.clip.ezblocks.block.BlockController;
import me.clip.ezblocks.block.BlockControllerImpl;
import me.clip.ezblocks.listeners.AutoSellListener;
import me.clip.ezblocks.listeners.BreakListenerHigh;
import me.clip.ezblocks.listeners.BreakListenerHighest;
import me.clip.ezblocks.listeners.BreakListenerLow;
import me.clip.ezblocks.listeners.BreakListenerLowest;
import me.clip.ezblocks.listeners.BreakListenerMonitor;
import me.clip.ezblocks.listeners.BreakListenerNormal;
import me.clip.ezblocks.listeners.TEListener;
import me.clip.ezblocks.reward.RewardHandler;
import me.clip.ezblocks.storage.MySQLStorage;
import me.clip.ezblocks.storage.Storage;
import me.clip.ezblocks.storage.YMLStorage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class EZBlocks extends JavaPlugin {

	private static EZBlocks instance;

	private Storage storage;
	private EZBlocksConfig config = new EZBlocksConfig(this);
	private BlockController blockController; // todo implement this class
	private RewardHandler rewardHandler = new RewardHandler(this);
	private EZBlocksCommands commands = new EZBlocksCommands(this);

	private BlockOptions options; // todo remove


	@Override
	public void onEnable() {

		instance = this;

		// Config
		// todo replace for new Config object
		config.loadConfigurationFile();
		loadOptions();

		// Storage
		if (!getConfig().getBoolean("database.enabled")) {
			storage = new YMLStorage(this);
		} else {
			storage = new MySQLStorage();
		}
		storage.initialize();

		// Controllers
		blockController = new BlockControllerImpl(this);

		// registerBlockBreakListener(); // todo move to blockcontroller

		// Commands
		getCommand("blocks").setExecutor(commands);

		// todo add getter to Config and load method with logger to blockscontroller
		getLogger().info(config.loadGlobalRewards() + " global rewards loaded!");
		getLogger().info(config.loadIntervalRewards() + " interval rewards loaded!");
		getLogger().info(config.loadPickaxeGlobalRewards() + " global pickaxe rewards loaded!");
		getLogger().info(config.loadPickaxeIntervalRewards() + " interval pickaxe rewards loaded!");
		
		if (Bukkit.getPluginManager().isPluginEnabled("TokenEnchant") && config.hookTokenEnchant()) {
			new TEListener(this);
			getLogger().info("Hooked into TokenEnchant for TEBlockExplodeEvent listener");
		}
	}

	private void loadOptions() { // todo remove
		//saveInterval = getConfig().getInt("save_interval");
		options = new BlockOptions();
		options.setUseBlocksCommand(getConfig().getBoolean("blocks_broken_command_enabled"));
		options.setBrokenMsg(getConfig().getString("blocks_broken_message"));
		options.setEnabledWorlds(getConfig().getStringList("enabled_worlds"));
		options.setUsePickCounter(config.pickCounterEnabled());
		options.setUsePickCounterDisplayName(config.pickCounterInDisplay());
		options.setPickCounterFormat(config.pickCounterFormat());
		options.setPickaxeNeverBreaks(getConfig().getBoolean("pickaxe_never_breaks"));
		options.setOnlyBelowY(getConfig().getBoolean("only_track_below_y.enabled"));
		options.setBelowYCoord(getConfig().getInt("only_track_below_y.coord"));
		options.setSurvivalOnly(getConfig().getBoolean("survival_mode_only"));
		options.setBlacklistedBlocks(getConfig().getStringList("material_blacklist"));
		options.setTrackedTools(config.trackedTools());
		options.setBlacklistIsWhitelist(config.blacklistIsWhitelist());
		options.setGiveRewardsOnAddCommand(config.giveRewardsOnAddCommand());
	}

	protected void reload() {
		storage.close(); // save & close storage
		reloadConfig(); // load from file, add missing defaults
		saveConfig(); // save (with added defaults)
		loadOptions(); // loadOptions from config

		// maybe the storage preference changed...
		if (!getConfig().getBoolean("database.enabled")) {
			storage = new YMLStorage(this);
		} else {
			storage = new MySQLStorage();
		}
		storage.initialize(); // init storage

		getLogger().info(config.loadGlobalRewards() + " global rewards loaded!");
		getLogger().info(config.loadIntervalRewards() + " interval rewards loaded!");
		getLogger().info(config.loadPickaxeGlobalRewards() + " global pickaxe rewards loaded!");
		getLogger().info(config.loadPickaxeIntervalRewards() + " interval pickaxe rewards loaded!");
	}

	@Override
	public void onDisable() {
		blockController.close();
		//if (BreakHandler.breaks != null) { // todo move to blockController.close()
		//	Set<String> save = BreakHandler.breaks.keySet();
		//
		//	Iterator<String> si = save.iterator();
		//
		//	while (si.hasNext()) {
		//
		//		String uuid = si.next();
		//
		//		int broken = BreakHandler.breaks.get(uuid);
		//
		//		// playerConfig.savePlayer(uuid, broken); // todo remove
		//		storage.setBlocksBroken(UUID.fromString(uuid), broken); // todo change breaks map key to UUID
		//
		//	}
		//
		//	System.out.println("[EZBlocks] "+save.size()+" players saved!");
		//}
	}

	protected void registerBlockBreakListener() { // todo redo this
		
		if (config.useAutoSellEvents() && Bukkit.getPluginManager().getPlugin("AutoSell") != null) {
			getLogger().info("Using AutoSell events for block break and sell detection...");
			new AutoSellListener(this);
			return;
		} else {
			getLogger().info("Failed to detect AutoSell. Defaulting to bukkit event listeners...");
		}
		//register break listener
		String priority = config.getListenerPriority();
		
		if (priority.equalsIgnoreCase("lowest")) {
			getLogger().info("BlockBreakEvent listener registered on LOWEST");
			new BreakListenerLowest(this); 
		} else if (priority.equalsIgnoreCase("low")) {
			getLogger().info("BlockBreakEvent listener registered on LOW");
			new BreakListenerLow(this);
		} else if (priority.equalsIgnoreCase("normal")) {
			getLogger().info("BlockBreakEvent listener registered on NORMAL");
			new BreakListenerNormal(this);
		} else if (priority.equalsIgnoreCase("high")) {
			getLogger().info("BlockBreakEvent listener registered on HIGH");
			new BreakListenerHigh(this);
		} else if (priority.equalsIgnoreCase("highest")) {
			getLogger().info("BlockBreakEvent listener registered on HIGHEST");
			new BreakListenerHighest(this);
		} else if (priority.equalsIgnoreCase("monitor")) {
			getLogger().info("BlockBreakEvent listener registered on MONITOR");
			new BreakListenerMonitor(this);
		} else {
			getLogger().info("BlockBreakEvent listener registered on HIGHEST");
			new BreakListenerHighest(this);
		}
	}

	public Storage getStorage() {
		return storage;
	}

	public EZBlocksConfig getPluginConfig() {
		return config;
	}

	public BlockController getBlockController() {
		return blockController;
	}

	public RewardHandler getRewardHandler() {
		return rewardHandler;
	}

	public BlockOptions getOptions() {
		return options;
	}

	public static EZBlocks getInstance() {
		return instance;
	}

	// todo used in the original PlaceholderAPI expansion
	@Deprecated
	public int getBlocksBroken(Player player) {
		return blockController.getBlocksBroken(player);
	}

	// todo used in the original PlaceholderAPI expansion
	@Deprecated
	public static EZBlocks getEZBlocks() {
		return instance;
	}
}
