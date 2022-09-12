package me.clip.ezblocks;

import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import me.clip.ezblocks.database.Database;
import me.clip.ezblocks.database.MySQL;
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
import me.clip.ezblocks.tasks.IntervalSaveTask;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class EZBlocks extends JavaPlugin {

	//private PlayerConfig playerConfig = new PlayerConfig(this); // todo remove
	private Storage storage;
	private EZBlocksConfig config = new EZBlocksConfig(this);
	private BreakHandler breakHandler = new BreakHandler(this);
	private RewardHandler rewardHandler = new RewardHandler(this);
	private EZBlocksCommands commands = new EZBlocksCommands(this);

	private BlockOptions options;
	private int saveInterval;
	private BukkitTask savetask;
	private Database database = null;
	private static EZBlocks instance;


	@Override
	public void onEnable() {

		instance = this;

		config.loadConfigurationFile();
		
		loadOptions();

		//initDb(); // todo remove
		if (!getConfig().getBoolean("database.enabled")) {
			storage = new YMLStorage(this);
		} else {
			storage = new MySQLStorage();
		}
		storage.initialize();
		
		breakHandler = new BreakHandler(this);

		Bukkit.getServer().getPluginManager().registerEvents(breakHandler, this);
		
		registerBlockBreakListener();
		
		startSaveTask();
		
		getCommand("blocks").setExecutor(commands);
		
		getLogger().info(config.loadGlobalRewards() + " global rewards loaded!");
		getLogger().info(config.loadIntervalRewards() + " interval rewards loaded!");
		getLogger().info(config.loadPickaxeGlobalRewards() + " global pickaxe rewards loaded!");
		getLogger().info(config.loadPickaxeIntervalRewards() + " interval pickaxe rewards loaded!");
		
		if (Bukkit.getPluginManager().isPluginEnabled("TokenEnchant") && config.hookTokenEnchant()) {
			new TEListener(this);
			getLogger().info("Hooked into TokenEnchant for TEBlockExplodeEvent listener");
		}
	}
	
	//private void initDb() { // todo remove
	//	if (!getConfig().getBoolean("database.enabled")) {
	//		playerConfig.reload();
	//		playerConfig.save();
	//		getLogger().info("Saving/loading via flatfile!");
	//	} else {
	//		// Make connection to the database
	//		try {
	//			getLogger().info("Creating MySQL connection ...");
	//			database = new MySQL(getConfig().getString("database.prefix"),
	//					getConfig().getString("database.hostname"), getConfig()
	//							.getInt("database.port") + "", getConfig()
	//							.getString("database.database"), getConfig()
	//							.getString("database.username"), getConfig()
	//							.getString("database.password"));
	//			database.open();
	//			// Check if table exists
	//			if (!database.checkTable("playerblocks")) {
	//				// Create table
	//				getLogger().info("Creating MySQL table ...");
	//
	//				database.createTable("CREATE TABLE IF NOT EXISTS `"
	//						+ database.getTablePrefix() + "data` ("
	//						+ "  `uuid` varchar(50) NOT NULL,"
	//						+ "  `blocks` integer NOT NULL,"
	//						+ "  PRIMARY KEY (`uuid`)"
	//						+ ") ENGINE=InnoDB DEFAULT CHARSET=latin1;");
	//			}
	//		} catch (Exception ex) {
	//			ex.printStackTrace();
	//			getLogger().severe("Falling back to flatfiles ...");
	//			database = null;
	//			playerConfig.reload();
	//			playerConfig.save();
	//		}
	//	}
	//}

	private void loadOptions() {
		saveInterval = getConfig().getInt("save_interval");
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
		stopSaveTask();
		// getServer().getScheduler().runTask(this, new IntervalSaveTask(this)); // todo remove
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

		startSaveTask();
		getLogger().info(config.loadGlobalRewards() + " global rewards loaded!");
		getLogger().info(config.loadIntervalRewards() + " interval rewards loaded!");
		getLogger().info(config.loadPickaxeGlobalRewards() + " global pickaxe rewards loaded!");
		getLogger().info(config.loadPickaxeIntervalRewards() + " interval pickaxe rewards loaded!");
	}

	@Override
	public void onDisable() {
		stopSaveTask();
		if (BreakHandler.breaks != null) {
			Set<String> save = BreakHandler.breaks.keySet();
		
			Iterator<String> si = save.iterator();
		
			while (si.hasNext()) {
				
				String uuid = si.next();
				
				int broken = BreakHandler.breaks.get(uuid);
				
				// playerConfig.savePlayer(uuid, broken); // todo remove
				storage.setBlocksBroken(UUID.fromString(uuid), broken); // todo change breaks map key to UUID
				
			}
		
			System.out.println("[EZBlocks] "+save.size()+" players saved!");
		}
	}

	protected void registerBlockBreakListener() {
		
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

	private void startSaveTask() {
		if (savetask == null) {
			getLogger().info("Saving all players every " + saveInterval + " minutes");
			savetask = getServer().getScheduler().runTaskTimerAsynchronously(
					this, new IntervalSaveTask(this), 1L,
					((20L * 60L) * saveInterval));
		} else {
			savetask.cancel();
			savetask = null;
			getLogger().info(
					"Saving all players every " + saveInterval + " minutes");
			savetask = getServer().getScheduler().runTaskTimerAsynchronously(
					this, new IntervalSaveTask(this), 1L,
					((20L * 60L) * saveInterval));
		}

	}

	private void stopSaveTask() {
		if (savetask != null) {
			savetask.cancel();
			savetask = null;
		}
	}

	public int getBlocksBroken(Player player) {
		if (BreakHandler.breaks != null
				&& BreakHandler.breaks.containsKey(player.getUniqueId().toString())) {
			return BreakHandler.breaks.get(player.getUniqueId().toString());
		} else {
			return 0;
		}

	}

	public Storage getStorage() {
		return storage;
	}

	public EZBlocksConfig getPluginConfig() {
		return config;
	}

	public BreakHandler getBreakHandler() {
		return breakHandler;
	}

	public RewardHandler getRewardHandler() {
		return rewardHandler;
	}

	public BlockOptions getOptions() {
		return options;
	}

	public Database getPluginDatabase() {
		return database;
	}

	public static EZBlocks getInstance() {
		return instance;
	}
}
