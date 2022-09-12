package me.clip.ezblocks.storage;

import me.clip.ezblocks.block.BlockStorage;

public interface Storage extends BlockStorage {

    void initialize();

    void close();

}
