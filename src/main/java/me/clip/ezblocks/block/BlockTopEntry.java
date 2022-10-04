package me.clip.ezblocks.block;

import org.bukkit.Bukkit;

import java.util.UUID;

public class BlockTopEntry {

    private final UUID uuid;
    private final int blocksBroken;

    public BlockTopEntry(UUID uuid, int blocksBroken) {
        this.uuid = uuid;
        this.blocksBroken = blocksBroken;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getUsername() {
        return Bukkit.getOfflinePlayer(uuid).getName();
    }

    public int getBlocksBroken() {
        return blocksBroken;
    }



}
