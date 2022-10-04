package me.clip.ezblocks.block;

import me.clip.ezblocks.EZBlocks;
import me.clip.ezblocks.storage.Storage;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BlockControllerImpl implements BlockController {

    private final Storage storage;
    private final Map<UUID, Integer> localBlocksBroken = new HashMap<>();
    private Map<Integer, BlockTopEntry> localBlocksTop = new HashMap<>();
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public BlockControllerImpl(EZBlocks plugin) {
        this.storage = plugin.getStorage();

        executorService.scheduleAtFixedRate(this::updateStorage, 0L, 5L, TimeUnit.MINUTES);
    }

    @Override
    public void setBlocksBroken(Player player, int amount) {
        localBlocksBroken.put(player.getUniqueId(), amount);
    }

    @Override
    public int getBlocksBroken(Player player) {
        Integer blocksBroken = localBlocksBroken.get(player.getUniqueId());
        if (blocksBroken == null) {
            blocksBroken = storage.getBlocksBroken(player.getUniqueId());
            localBlocksBroken.put(player.getUniqueId(), blocksBroken);
        }
        return blocksBroken;
    }

    @Override
    public void resetBlocks(Player player) {
        setBlocksBroken(player, 0);
    }

    @Override
    public void resetAll() {
        // todo
    }

    @Override
    public BlockTopEntry getBlocksTop(int position) {
        return localBlocksTop.get(position);
    }

    @Override
    public Map<Integer, BlockTopEntry> getBlocksTop() {
        return Collections.unmodifiableMap(localBlocksTop);
    }

    private void updateStorage() {
        // save current
        localBlocksBroken.forEach(storage::setBlocksBroken);

        // load blockstop
        localBlocksTop = storage.getBlocksTop();
    }

    @Override
    public void close() {
        executorService.shutdown();
    }

    // todo quit and join events
}
