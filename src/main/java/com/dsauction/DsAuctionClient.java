package com.dsauction;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class DsAuctionClient implements ClientModInitializer {
	/** Set by /ac; the menu opens on the next tick so the chat screen doesn't close it. */
	public static volatile boolean pendingOpen = false;

	@Override
	public void onInitializeClient() {
		PayTracker.register();
		AuctionHud.register();
		AuctionCommands.register();
		KeyHandler.register();
		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			if (pendingOpen && mc.screen == null) {
				pendingOpen = false;
				mc.setScreen(new BlockPickerScreen());
			}
		});
	}
}
