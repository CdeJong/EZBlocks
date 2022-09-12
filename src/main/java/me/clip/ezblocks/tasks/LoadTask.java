package me.clip.ezblocks.tasks;

import me.clip.ezblocks.BreakHandler;
import me.clip.ezblocks.EZBlocks;

import java.util.UUID;

public class LoadTask implements Runnable {

	private EZBlocks plugin;
	private String uuid;

	public LoadTask(EZBlocks instance, String uuid) {
		plugin = instance;
		this.uuid = uuid;
	}

	@Override
	public void run() {
		if (BreakHandler.breaks != null) {
			if (!BreakHandler.breaks.containsKey(uuid)) {
				BreakHandler.breaks.put(uuid, plugin.getStorage().getBlocksBroken(UUID.fromString(uuid)));
			}
		}
	}
}