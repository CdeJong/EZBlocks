package me.clip.ezblocks.config;

import javafx.scene.paint.Material;

import java.util.List;

public interface Config {

    void initialize();

    List<String> getEnabledWorlds();

    String getBrokenMsg();

    boolean usePickCounter();

    boolean pickaxeNeverBreaks();

    boolean onlyBelowY();

    int getBelowYCoordinate();

    boolean usePickCounterDisplayName();

    String getPickCounterFormat();

    boolean useBlocksCommand();

    boolean survivalOnly();

    List<Material> getBlacklistedBlocks();

    List<Material> getTrackedTools();

    boolean blacklistIsWhitelist();

    boolean giveRewardsOnAddCommand();

    void close();

    // todo add database stuff

}
