package com.dsauction;

import java.util.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** The mod menu: search + pick the auction block, start/stop the auction, reset totals, toggle the HUD. */
public class BlockPickerScreen extends Screen {
	private static final int COLS = 9, ROWS = 5, CELL = 22, PER_PAGE = COLS * ROWS;
	private final Button[] cells = new Button[PER_PAGE];
	private List<Item> filtered = new ArrayList<>();
	private EditBox search;
	private Button prev, next, startStop, hud, teleport;
	private int page = 0;
	private String query = "";

	public BlockPickerScreen() { super(Component.literal("DonutSMP Auction")); }

	@Override
	protected void init() {
		int gx = (width - COLS * CELL) / 2, gy = 50;
		search = new EditBox(font, width / 2 - 100, 24, 200, 18, Component.literal("Search"));
		search.setValue(query);
		search.setResponder(s -> { query = s; filter(); });
		addRenderableWidget(search);

		for (int i = 0; i < PER_PAGE; i++) {
			final int idx = i;
			cells[i] = Button.builder(Component.empty(), b -> pick(idx))
				.bounds(gx + (i % COLS) * CELL, gy + (i / COLS) * CELL, 20, 20).build();
			addRenderableWidget(cells[i]);
		}
		int ny = gy + ROWS * CELL + 6;
		prev = addRenderableWidget(Button.builder(Component.literal("<"), b -> { page--; refresh(); })
			.bounds(gx, ny, 30, 20).build());
		next = addRenderableWidget(Button.builder(Component.literal(">"), b -> { page++; refresh(); })
			.bounds(gx + COLS * CELL - 30, ny, 30, 20).build());

		int by = ny + 30, bw = 92, gap = 6, total = bw * 4 + gap * 3, bx = (width - total) / 2;
		startStop = addRenderableWidget(Button.builder(Component.empty(), b -> {
			AuctionState.active = !AuctionState.active; updateLabels(); })
			.bounds(bx, by, bw, 20).build());
		addRenderableWidget(Button.builder(Component.literal("Reset totals"), b -> AuctionState.reset())
			.bounds(bx + (bw + gap), by, bw, 20).build());
		hud = addRenderableWidget(Button.builder(Component.empty(), b -> {
			AuctionState.hudVisible = !AuctionState.hudVisible; updateLabels(); })
			.bounds(bx + (bw + gap) * 2, by, bw, 20).build());
		addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
			.bounds(bx + (bw + gap) * 3, by, bw, 20).build());
        teleport = addRenderableWidget(Button.builder(Component.literal("TP to winner (prefill /tpa)"), b -> {
            List<Map.Entry<String, Double>> ranking = AuctionState.ranking();
            if (!ranking.isEmpty() && minecraft != null) {
                minecraft.setScreen(new ChatScreen("/tpa " + ranking.get(0).getKey(), false));
            }
        }).bounds(width / 2 - 100, by + 24, 200, 20).build());

		filter();
		updateLabels();
	}

	private void updateLabels() {
		startStop.setMessage(Component.literal(AuctionState.active ? "Stop auction" : "Start auction"));
		hud.setMessage(Component.literal("HUD: " + (AuctionState.hudVisible ? "ON" : "OFF")));
	}

	private void filter() {
		String q = query.trim().toLowerCase(Locale.ROOT);
		filtered = new ArrayList<>();
		for (Item it : BlockList.all())
			if (q.isEmpty() || BlockList.name(it).toLowerCase(Locale.ROOT).contains(q)) filtered.add(it);
		page = 0;
		refresh();
	}

	private void refresh() {
		int pages = Math.max(1, (filtered.size() + PER_PAGE - 1) / PER_PAGE);
		page = Math.max(0, Math.min(page, pages - 1));
		for (int i = 0; i < PER_PAGE; i++) {
			boolean has = page * PER_PAGE + i < filtered.size();
			cells[i].visible = has;
			cells[i].active = has;
		}
		prev.active = page > 0;
		next.active = page < pages - 1;
	}

	private void pick(int idx) {
		int n = page * PER_PAGE + idx;
		if (n < filtered.size()) AuctionState.item = filtered.get(n);
	}

	@Override
	public void render(GuiGraphics g, int mx, int my, float delta) {
		super.render(g, mx, my, delta);
		g.drawCenteredString(font, "DonutSMP Auction - pick an item", width / 2, 8, 0xFFFFFFFF);
		String hover = null;
		for (int i = 0; i < PER_PAGE; i++) {
			if (!cells[i].visible) continue;
			int n = page * PER_PAGE + i;
			Item it = filtered.get(n);
			g.renderItem(new ItemStack(it), cells[i].getX() + 2, cells[i].getY() + 2);
			if (it == AuctionState.item)
				g.fill(cells[i].getX(), cells[i].getY(), cells[i].getX() + 20, cells[i].getY() + 1, 0xFFFFD700);
			if (cells[i].isMouseOver(mx, my)) hover = BlockList.name(it);
		}
		int pages = Math.max(1, (filtered.size() + PER_PAGE - 1) / PER_PAGE);
		g.drawCenteredString(font, "Page " + (page + 1) + "/" + pages + "  (" + filtered.size() + " items)",
			width / 2, prev.getY() + 6, 0xFFAAAAAA);
		int sy = teleport.getY() + 26;
		g.renderItem(new ItemStack(AuctionState.item), width / 2 - 80, sy - 4);
		g.drawString(font, "Auction item: " + BlockList.name(AuctionState.item), width / 2 - 58, sy, 0xFFFFD700);
		g.drawCenteredString(font, AuctionState.active ? "Auction is LIVE - counting /pay totals" : "Auction stopped",
			width / 2, sy + 14, AuctionState.active ? 0xFF55FF55 : 0xFFFF5555);
		if (hover != null) g.drawCenteredString(font, hover, width / 2, 40, 0xFFFFFFAA);
	}

	@Override
	public boolean isPauseScreen() { return false; }
}
