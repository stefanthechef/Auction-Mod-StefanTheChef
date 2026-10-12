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
	private Button prev, next, startStop, hud, teleport, startTimer, stopTimer;
	private EditBox timerHours, timerMinutes, timerSeconds, minimumPaymentField;
	private Button setMinimumButton;
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

        // Auction timer controls live directly inside the /ac menu.
        int timerY = by + 54;
        int timerStartX = width / 2 - 135;
        timerHours = new EditBox(font, timerStartX, timerY, 42, 18, Component.literal("Hours"));
        timerMinutes = new EditBox(font, timerStartX + 47, timerY, 42, 18, Component.literal("Minutes"));
        timerSeconds = new EditBox(font, timerStartX + 94, timerY, 42, 18, Component.literal("Seconds"));
        timerHours.setValue("0");
        timerMinutes.setValue("5");
        timerSeconds.setValue("0");
        timerHours.setFilter(v -> v.matches("\\d{0,4}"));
        timerMinutes.setFilter(v -> v.matches("\\d{0,2}"));
        timerSeconds.setFilter(v -> v.matches("\\d{0,2}"));
        addRenderableWidget(timerHours);
        addRenderableWidget(timerMinutes);
        addRenderableWidget(timerSeconds);
        startTimer = addRenderableWidget(Button.builder(Component.literal("Start timer"), b -> startTimerFromFields())
            .bounds(timerStartX + 141, timerY, 76, 18).build());
        stopTimer = addRenderableWidget(Button.builder(Component.literal("Stop"), b -> {
            AuctionState.stopTimer();
            AuctionState.active = false;
            updateLabels();
        }).bounds(timerStartX + 222, timerY, 48, 18).build());

        // Minimum accepted payment filter, configured directly in /ac.
        int minY = timerY + 43;
        minimumPaymentField = new EditBox(font, width / 2 - 135, minY, 155, 18, Component.literal("Minimum payment"));
        minimumPaymentField.setHint(Component.literal("0 = no minimum"));
        minimumPaymentField.setValue(formatInputAmount(AuctionState.minimumPayment));
        minimumPaymentField.setFilter(v -> v.matches("[0-9,]*(?:\\.[0-9]*)?"));
        addRenderableWidget(minimumPaymentField);
        setMinimumButton = addRenderableWidget(Button.builder(Component.literal("Set minimum"), b -> applyMinimumPayment())
            .bounds(width / 2 + 25, minY, 110, 18).build());

		filter();
		updateLabels();
	}

	private void updateLabels() {
		startStop.setMessage(Component.literal(AuctionState.active ? "Stop auction" : "Start auction"));
		hud.setMessage(Component.literal("HUD: " + (AuctionState.hudVisible ? "ON" : "OFF")));
	}

    private static String formatInputAmount(double amount) {
        if (amount == Math.floor(amount)) return Long.toString((long) amount);
        return Double.toString(amount);
    }

    private void applyMinimumPayment() {
        try {
            String raw = minimumPaymentField.getValue().replace(",", "").trim();
            double amount = raw.isEmpty() ? 0.0 : Double.parseDouble(raw);
            if (!Double.isFinite(amount) || amount < 0 || amount > 1.0e15) throw new NumberFormatException();
            AuctionState.minimumPayment = amount;
            AuctionState.saveHudSettings();
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.displayClientMessage(Component.literal("Minimum payment set to " + AuctionState.money(amount) + (amount == 0 ? " (disabled)." : ".")), false);
            }
        } catch (NumberFormatException ignored) {
            if (minecraft != null && minecraft.player != null) minecraft.player.displayClientMessage(Component.literal("Enter a valid minimum amount, e.g. 500000."), false);
        }
    }

    private void startTimerFromFields() {
        try {
            long hours = timerHours.getValue().isBlank() ? 0 : Long.parseLong(timerHours.getValue());
            long minutes = timerMinutes.getValue().isBlank() ? 0 : Long.parseLong(timerMinutes.getValue());
            long seconds = timerSeconds.getValue().isBlank() ? 0 : Long.parseLong(timerSeconds.getValue());
            long duration = Math.addExact(Math.addExact(Math.multiplyExact(hours, 3600L), Math.multiplyExact(minutes, 60L)), seconds);
            if (duration < 1 || duration > 604800) {
                if (minecraft != null && minecraft.player != null) minecraft.player.displayClientMessage(Component.literal("Choose a duration between 1 second and 7 days."), false);
                return;
            }
            AuctionState.startTimer(duration);
            if (minecraft != null && minecraft.player != null) minecraft.player.displayClientMessage(Component.literal("Auction timer started."), false);
        } catch (NumberFormatException | ArithmeticException ignored) {
            if (minecraft != null && minecraft.player != null) minecraft.player.displayClientMessage(Component.literal("Invalid timer duration."), false);
        }
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
        // Separate, softly framed settings rows prevent timer/minimum controls from overlapping.
        int panelLeft = width / 2 - 150;
        int panelRight = width / 2 + 150;
        int panelTop = timerHours.getY() - 21;
        int panelBottom = minimumPaymentField.getY() + 24;
        // Panels are painted before widgets below, so they never cover text fields/buttons.
        g.fill(panelLeft, panelTop, panelRight, panelBottom, 0xB90A1019);
        g.fill(panelLeft, panelTop, panelRight, panelTop + 1, 0xFF31506B);
        g.fill(panelLeft, panelBottom - 1, panelRight, panelBottom, 0xFF31506B);
        g.fill(panelLeft, panelTop, panelLeft + 1, panelBottom, 0xFF31506B);
        g.fill(panelRight - 1, panelTop, panelRight, panelBottom, 0xFF31506B);
        // Draw widgets after panel backgrounds so input boxes and buttons remain visible.
        super.render(g, mx, my, delta);
        g.drawCenteredString(font, "Auction timer", width / 2, timerHours.getY() - 17, 0xFF8CCBFF);
        g.drawString(font, "Hours", timerHours.getX() + 7, timerHours.getY() + 19, 0xFFB7C7D9);
        g.drawString(font, "Minutes", timerMinutes.getX() + 3, timerMinutes.getY() + 19, 0xFFB7C7D9);
        g.drawString(font, "Seconds", timerSeconds.getX() + 3, timerSeconds.getY() + 19, 0xFFB7C7D9);
        g.drawString(font, "Minimum payment", minimumPaymentField.getX(), minimumPaymentField.getY() - 10, 0xFFFFD166);
        g.drawString(font, "Ignore payments below this amount (0 = off)", minimumPaymentField.getX(), minimumPaymentField.getY() + 20, 0xFFAAAAAA);
		int sy = minimumPaymentField.getY() + 31;
        g.fill(width / 2 - 150, sy - 5, width / 2 + 150, sy + 14, 0xB90A1019);
        g.fill(width / 2 - 150, sy - 5, width / 2 + 150, sy - 4, 0xFF174A73);
		g.renderItem(new ItemStack(AuctionState.item), width / 2 - 115, sy - 4);
		g.drawString(font, "Auction item: " + BlockList.name(AuctionState.item), width / 2 - 92, sy, 0xFFFFD700);
		g.drawCenteredString(font, AuctionState.active ? "Auction is LIVE - counting /pay totals" : "Auction stopped",
			width / 2, sy + 14, AuctionState.active ? 0xFF55FF55 : 0xFFFF5555);
		if (hover != null) g.drawCenteredString(font, hover, width / 2, 40, 0xFFFFFFAA);
	}

	@Override
	public boolean isPauseScreen() { return false; }
}
