package me.clip.ezblocks.reward;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.clip.ezblocks.EZBlocks;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;


public class RewardHandler {

	private static final String PLAYER_NAME_PLACEHOLDER = "%player%";
	private static final String BLOCKS_BROKEN_PLACEHOLDER = "%blocksbroken%";
	private static final String MESSAGE_COMMAND = "ezmsg ";
	private static final String BROADCAST_COMMAND = "ezbroadcast ";

	private final Server server;
	
	private final Map<Integer, Reward> globalRewards = new HashMap<>();
	private final Map<Integer, Reward> intervalRewards = new HashMap<>();
	private final Map<Integer, Reward> pickaxeGlobalRewards = new HashMap<>();
	private final Map<Integer, Reward> pickaxeIntervalRewards = new HashMap<>();
	
	public RewardHandler(EZBlocks plugin) {
		this.server = plugin.getServer();
	}
	
	public void setReward(int breaks, Reward reward) {
		globalRewards.put(breaks, reward);
	}
	
	public void setIntervalReward(int breaks, Reward reward) {
		intervalRewards.put(breaks, reward);
	}
	
	public void setPickaxeReward(int breaks, Reward reward) {
		pickaxeGlobalRewards.put(breaks, reward);
	}
	
	public void setPickaxeIntervalReward(int breaks, Reward reward) {
		pickaxeIntervalRewards.put(breaks, reward);
	}

	public void giveReward(Player player, int breaks) {
		Reward reward = globalRewards.get(breaks);

		if (reward == null || reward.getCommands() == null) {
			return;
		}

		executeCommands(player, breaks, reward.getCommands());
	}
	
	public void givePickaxeReward(Player player, int breaks) {
		Reward reward = pickaxeGlobalRewards.get(breaks);

		if (reward == null || reward.getCommands() == null) {
			return;
		}

		executeCommands(player, breaks, reward.getCommands());
	}
	
	// interval
	public void giveIntervalReward(Player player, int breaks) {
		handleIntervalReward(player, breaks, intervalRewards);
	}
	
	public void givePickaxeIntervalReward(Player player, int breaks) {
		handleIntervalReward(player, breaks, pickaxeIntervalRewards);
	}

	// private
	private void handleIntervalReward(Player player, int breaks, Map<Integer, Reward> rewards) {
		if (rewards.isEmpty()) {
			return;
		}

		for (Reward reward : rewards.values()) {
			if (breaks % reward.getBlocksNeeded() != 0 || reward.getCommands() == null) {
				continue;
			}
			executeCommands(player, breaks, reward.getCommands());
		}
	}

	private void executeCommands(Player player, int breaks, List<String> commands) {
		for (String command : commands) {
			String formattedCommand = command.replace(PLAYER_NAME_PLACEHOLDER, player.getName())
					.replace(BLOCKS_BROKEN_PLACEHOLDER, String.valueOf(breaks));

			if (formattedCommand.startsWith(MESSAGE_COMMAND)) {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', formattedCommand.substring(5)));
				continue;
			}

			if (command.startsWith(BROADCAST_COMMAND)) {
				server.broadcastMessage(ChatColor.translateAlternateColorCodes('&', formattedCommand.substring(12)));
				continue;
			}

			server.dispatchCommand(server.getConsoleSender(), formattedCommand);
		}
	}

	public Map<Integer, Reward> getGlobalRewards() {
		return Collections.unmodifiableMap(globalRewards);
	}

	public Map<Integer, Reward> getIntervalRewards() {
		return Collections.unmodifiableMap(intervalRewards);
	}

	public Map<Integer, Reward> getPickaxeGlobalRewards() {
		return Collections.unmodifiableMap(pickaxeGlobalRewards);
	}

	public Map<Integer, Reward> getPickaxeIntervalRewards() {
		return Collections.unmodifiableMap(pickaxeIntervalRewards);
	}
}
