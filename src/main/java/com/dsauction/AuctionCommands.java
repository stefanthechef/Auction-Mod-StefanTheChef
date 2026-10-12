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
                    AuctionState.stopTimer();
					c.getSource().sendFeedback(Component.literal("Auction stopped.")); return 1; }))
				.then(ClientCommandManager.literal("reset").executes(c -> {
					AuctionState.reset();
					c.getSource().sendFeedback(Component.literal("Totals cleared.")); return 1; }))
				.then(ClientCommandManager.literal("hud").executes(c -> {
					AuctionState.hudVisible = !AuctionState.hudVisible;
					c.getSource().sendFeedback(Component.literal("HUD " + (AuctionState.hudVisible ? "on" : "off"))); return 1; }))
                .then(ClientCommandManager.literal("left").executes(c -> { AuctionState.hudX = 6; c.getSource().sendFeedback(Component.literal("HUD moved left.")); return 1; }))
                .then(ClientCommandManager.literal("right").executes(c -> { AuctionState.hudX = -1; c.getSource().sendFeedback(Component.literal("HUD moved right.")); return 1; }))
                .then(ClientCommandManager.literal("up").executes(c -> { AuctionState.hudY = Math.max(0, AuctionState.hudY - 10); return 1; }))
                .then(ClientCommandManager.literal("down").executes(c -> { AuctionState.hudY += 10; return 1; }))
                .then(ClientCommandManager.literal("wider").executes(c -> { AuctionState.hudWidth = Math.min(280, AuctionState.hudWidth + 10); return 1; }))
                .then(ClientCommandManager.literal("narrower").executes(c -> { AuctionState.hudWidth = Math.max(150, AuctionState.hudWidth - 10); return 1; }))));
	}
}
