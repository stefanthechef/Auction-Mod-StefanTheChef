package com.dsauction;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.network.chat.Component;

/** /ac (menu), /ac start, /ac stop, /ac reset, /ac hud */
public final class AuctionCommands {
	private AuctionCommands() {}

	public static void register() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
			ClientCommandManager.literal("ac")
				.executes(c -> { DsAuctionClient.pendingOpen = true; return 1; })
				.then(ClientCommandManager.literal("start").executes(c -> {
					AuctionState.active = true;
					c.getSource().sendFeedback(Component.literal("Auction started - counting /pay totals.")); return 1; }))
				.then(ClientCommandManager.literal("stop").executes(c -> {
					AuctionState.active = false;
					c.getSource().sendFeedback(Component.literal("Auction stopped.")); return 1; }))
				.then(ClientCommandManager.literal("reset").executes(c -> {
					AuctionState.reset();
					c.getSource().sendFeedback(Component.literal("Totals cleared.")); return 1; }))
				.then(ClientCommandManager.literal("hud").executes(c -> {
					AuctionState.hudVisible = !AuctionState.hudVisible;
					c.getSource().sendFeedback(Component.literal("HUD " + (AuctionState.hudVisible ? "on" : "off"))); return 1; }))));
	}
}
