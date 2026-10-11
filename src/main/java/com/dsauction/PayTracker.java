package com.dsauction;

import java.util.regex.*;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;

/** Reads chat for DonutSMP's "<Player> paid you $<amount>" line and adds it to that player's total. */
public final class PayTracker {
	private static final Pattern PAID = Pattern.compile(
		"([A-Za-z0-9_.]{3,20}) paid you \\$([0-9][0-9,]*(?:\\.[0-9]+)?)\\s*([kKmMbBtT]?)");

	private PayTracker() {}

	public static void register() {
		ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
			if (overlay) return; // ignore action bar so a payment is never counted twice
			handle(message.getString());
		});
	}

	static void handle(String text) {
		Matcher m = PAID.matcher(text);
		if (!m.find()) return;
		try {
			double amount = Double.parseDouble(m.group(2).replace(",", ""));
			switch (m.group(3).toLowerCase()) {
				case "k" -> amount *= 1_000d;
				case "m" -> amount *= 1_000_000d;
				case "b" -> amount *= 1_000_000_000d;
				case "t" -> amount *= 1_000_000_000_000d;
				default -> {}
			}
			AuctionState.addPayment(m.group(1), amount);
		} catch (NumberFormatException ignored) {}
	}
}
