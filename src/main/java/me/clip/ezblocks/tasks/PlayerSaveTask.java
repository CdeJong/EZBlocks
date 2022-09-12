package me.clip.ezblocks.tasks;

import me.clip.ezblocks.EZBlocks;

import java.util.UUID;

public class PlayerSaveTask implements Runnable {

	private EZBlocks plugin;
	private String uuid;
	private int toSave;

	public PlayerSaveTask(EZBlocks instance, String uuid, int amt) {
		plugin = instance;
		this.uuid = uuid;
		toSave = amt;
	}

	@Override
	public void run() {
		plugin.getStorage().setBlocksBroken(UUID.fromString(uuid), toSave);
	}
}
