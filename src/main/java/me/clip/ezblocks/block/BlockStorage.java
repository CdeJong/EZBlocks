package me.clip.ezblocks.block;

import java.util.Map;
import java.util.UUID;

public interface BlockStorage {

    void setBlocksBroken(UUID uuid, int amount);

    int getBlocksBroken(UUID uuid);

    Map<Integer, BlockTopEntry> getBlocksTop();

    // todo reset blocks.

}
