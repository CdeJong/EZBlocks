package me.clip.ezblocks;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class PlayerConfig {

	private EZBlocks plugin;
	private FileConfiguration dataConfig = null;
	private File dataFile = null;

	public PlayerConfig(EZBlocks i) {
		this.plugin = i;
	}

	public void reload() {
		if (this.dataFile == null) {
			this.dataFile = new File(this.plugin.getDataFolder(), "stats.yml");
		}
		this.dataConfig = YamlConfiguration.loadConfiguration(this.dataFile);
	}

	public FileConfiguration load() {
		if (this.dataConfig == null) {
			reload();
		}
		return this.dataConfig;
	}

	public void save() {
		if ((this.dataConfig == null) || (this.dataFile == null))
			return;
		try {
			load().save(this.dataFile);
		} catch (IOException ex) {
			this.plugin.getLogger().log(Level.SEVERE,
					"Could not save to " + this.dataFile, ex);
		}
	}

	public Set<String> getAllEntries() {
		return this.dataConfig.getKeys(false);
	}

	public void savePlayer(String uuid, int broken) {
		
		if (broken == 0) {
			return;
		}
		
		if (plugin.getConfig().getBoolean("database.enabled")
				&& plugin.getPluginDatabase() != null) {
			
			if (plugin.getPluginDatabase().getConnection() == null) {
				plugin.getLogger().warning("Could not connect to database! Failed to save data for uuid: " + uuid + "!");
				return;
			}

			try {
				String query = "SELECT blocksmined FROM `"
						+ plugin.getPluginDatabase().getTablePrefix()
						+ "playerblocks` WHERE uuid=?";
				PreparedStatement statement = plugin.getPluginDatabase().prepare(query);
				statement.setString(1, uuid);
				ResultSet result = statement.executeQuery();
				
				if (result.next()) {
					
					query = "UPDATE `"+plugin.getPluginDatabase().getTablePrefix()+"playerblocks` SET blocksmined=? WHERE uuid=?";
					
					PreparedStatement updateStatement = plugin.getPluginDatabase().prepare(query);
					
					updateStatement.setInt(1, broken);
					updateStatement.setString(2, uuid);
					updateStatement.execute();
					updateStatement.close();
				
				} else {
					// INSERT
					
					query = "INSERT INTO `"+plugin.getPluginDatabase().getTablePrefix()+"playerblocks` (uuid,blocksmined) VALUES (?,?)";
					
					PreparedStatement insertStatement = plugin.getPluginDatabase().prepare(query);
					insertStatement.setString(1, uuid);
					insertStatement.setInt(2, broken);
					insertStatement.execute();
					insertStatement.close();
					
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else {
			synchronized(this.dataConfig) {
				this.dataConfig.set(uuid + ".blocks_broken", broken);
				save();
			}
			
		}
	}

	public int getBlocksBroken(String uuid) {
		if (plugin.getConfig().getBoolean("database.enabled")
				&& plugin.getPluginDatabase() != null) {
			
			if (plugin.getPluginDatabase().getConnection() == null) {
				plugin.getLogger().warning("Could not connect to database! Failed to load data for uuid: " + uuid + "!");
				return 0;
			}
			// Get data from mysql
			try {
				String query = "SELECT blocksmined FROM `"
						+ plugin.getPluginDatabase().getTablePrefix()
						+ "playerblocks` WHERE uuid=?";
				PreparedStatement statement = plugin.getPluginDatabase().prepare(query);
				// Prepare statement will prevent sql injections.
				// However its rather save
				// with this data
				statement.setString(1, uuid);
				// Execute select and get the result table
				ResultSet result = statement.executeQuery();
				if (result.next()) {
					return result.getInt(1);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else {
			return this.dataConfig.getInt(uuid + ".blocks_broken");
		}
		return 0;
	}

	public boolean hasData(String uuid) {
		if (plugin.getConfig().getBoolean("database.enabled")
				&& plugin.getPluginDatabase() != null) {
			return true;
		}
		return this.dataConfig.contains(uuid + ".blocks_broken")
				&& this.dataConfig.isInt(uuid + ".blocks_broken");
	}

}
