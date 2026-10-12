package com.dsauction;

import java.util.*;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/** The little overlay: item being auctioned, top bidder and the TOTAL they've paid. */
public final class AuctionHud {
	private AuctionHud() {}

	public static void register() {
		HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("dsauction", "hud"), (g, tick) -> render(g));
	}

	private static void render(GuiGraphics g) {
		Minecraft mc = Minecraft.getInstance();
		if (!AuctionState.hudVisible || mc.options.hideGui) return;
		Font f = mc.font;
		List<Map.Entry<String, Double>> rank = AuctionState.ranking();
		int w = AuctionState.hudWidth, rows = Math.min(2, Math.max(0, rank.size() - 1));
		int h = 76 + rows * 10;
		int x = AuctionState.hudX < 0 ? g.guiWidth() - w - 6 : Math.max(0, Math.min(AuctionState.hudX, g.guiWidth() - w));
        int y = Math.max(0, Math.min(AuctionState.hudY, g.guiHeight() - h));

		g.fill(x, y, x + w, y + h, 0xC0101010);
		g.fill(x, y, x + w, y + 1, 0xFFFFD700);
		g.fill(x, y + h - 1, x + w, y + h, 0xFFFFD700);
		g.drawString(f, "AUCTION", x + 6, y + 5, 0xFFFFD700);
		String st = AuctionState.active ? "LIVE" : "ENDED";
		g.drawString(f, st, x + w - 6 - f.width(st), y + 5, AuctionState.active ? 0xFF55FF55 : 0xFFFF5555);

		g.renderItem(new ItemStack(AuctionState.item), x + 6, y + 17);
		String name = BlockList.name(AuctionState.item);
		while (f.width(name) > w - 34 && name.length() > 3) name = name.substring(0, name.length() - 2);
		g.drawString(f, name, x + 28, y + 22, 0xFFFFFFFF);

		g.fill(x + 6, y + 37, x + w - 6, y + 38, 0x55FFFFFF);
		if (rank.isEmpty()) {
			g.drawString(f, "No bids yet", x + 6, y + 46, 0xFFAAAAAA);
			return;
		}
		Map.Entry<String, Double> top = rank.get(0);
		g.drawString(f, "TOP BIDDER", x + 6, y + 42, 0xFF888888);
		g.drawString(f, top.getKey(), x + 6, y + 52, 0xFFFFFFFF);
		String amt = AuctionState.money(top.getValue());
		g.drawString(f, amt, x + w - 6 - f.width(amt), y + 52, 0xFF55FF55);
		g.drawString(f, "total paid", x + w - 6 - f.width("total paid"), y + 42, 0xFF888888);
		for (int i = 1; i <= rows; i++) {
			Map.Entry<String, Double> e = rank.get(i);
			g.drawString(f, (i + 1) + ". " + e.getKey(), x + 6, y + 56 + i * 10, 0xFFAAAAAA);
			String a = AuctionState.money(e.getValue());
			g.drawString(f, a, x + w - 6 - f.width(a), y + 56 + i * 10, 0xFFAAAAAA);
		}
	}
}
