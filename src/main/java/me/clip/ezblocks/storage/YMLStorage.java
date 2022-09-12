package me.clip.ezblocks.storage;

import me.clip.ezblocks.EZBlocks;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class YMLStorage implements Storage {

    private static final String FILENAME = "stats.yml";

    private final EZBlocks plugin;

    private FileConfiguration dataConfig;
    private File dataFile;

    public YMLStorage(EZBlocks plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize() {
        dataFile = new File(plugin.getDataFolder(), FILENAME);
        dataConfig = YamlConfiguration.loadConfiguration(this.dataFile);
    }

    @Override
    public void setBlocksBroken(UUID uuid, int amount) {
        dataConfig.set(uuid + ".blocks_broken", amount);
        save(); // todo should it save each time?
    }

    @Override
    public int getBlocksBroken(UUID uuid) {
        return dataConfig.getInt(uuid + ".blocks_broken"); // if no record or file, default 0
    }

    @Override
    public void close() {
        save();
        dataConfig = null;
        dataFile = null;
    }

    private void save() {
        try {
            dataConfig.save(dataFile); // should create folder/file
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
