package me.clip.ezblocks.block;

import java.util.UUID;

public interface BlockStorage {

    void setBlocksBroken(UUID uuid, int amount);

    int getBlocksBroken(UUID uuid);

    // todo reset blocks.

}
