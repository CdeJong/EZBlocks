package me.clip.ezblocks.block;

import org.bukkit.entity.Player;

import java.util.Map;

public interface BlockController {

    void setBlocksBroken(Player player, int amount);

    int getBlocksBroken(Player player);

    BlockTopEntry getBlocksTop(int position);

    Map<Integer, BlockTopEntry> getBlocksTop();

    void close();




}
