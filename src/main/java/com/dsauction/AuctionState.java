package com.dsauction;

import java.util.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import net.minecraft.client.Minecraft;

/** Holds the live auction: the item, on/off flag and each player's TOTAL paid. */
public final class AuctionState {
	public static Item item = Items.DIAMOND_BLOCK;
	public static boolean active = false;
	public static boolean hudVisible = true;
    public static int hudX = -1; // -1 means right aligned
    public static int hudY = 6;
    public static int hudWidth = 190;
    public static double hudScale = 1.0;
    public static boolean hudDragging = false;
    public static int dragOffsetX = 0;
    public static int dragOffsetY = 0;
    private static boolean previousLeftPressed = false;

    public static void loadHudSettings() {
        Path path = Minecraft.getInstance().gameDirectory.toPath().resolve("config/dsauction.properties");
        Properties p = new Properties();
        try {
            if (Files.exists(path)) {
                try (var in = Files.newInputStream(path)) { p.load(in); }
                hudX = Integer.parseInt(p.getProperty("hudX", "-1"));
                hudY = Integer.parseInt(p.getProperty("hudY", "6"));
                hudWidth = Math.max(150, Math.min(280, Integer.parseInt(p.getProperty("hudWidth", "190"))));
                hudScale = Math.max(0.6, Math.min(2.0, Double.parseDouble(p.getProperty("hudScale", "1.0"))));
            }
        } catch (IOException | NumberFormatException ignored) { }
    }

    public static void saveHudSettings() {
        Path path = Minecraft.getInstance().gameDirectory.toPath().resolve("config/dsauction.properties");
        Properties p = new Properties();
        p.setProperty("hudX", Integer.toString(hudX));
        p.setProperty("hudY", Integer.toString(hudY));
        p.setProperty("hudWidth", Integer.toString(hudWidth));
        p.setProperty("hudScale", Double.toString(hudScale));
        try {
            Files.createDirectories(path.getParent());
            try (var out = Files.newOutputStream(path)) { p.store(out, "DonutSMP Auction HUD settings"); }
        } catch (IOException ignored) { }
    }

    public static boolean wasLeftPressed() { return previousLeftPressed; }
    public static void setPreviousLeftPressed(boolean pressed) { previousLeftPressed = pressed; }
	/** player name -> TOTAL of every /pay they sent you during the auction. */
	public static final Map<String, Double> totals = new LinkedHashMap<>();

	private AuctionState() {}

	public static void addPayment(String player, double amount) {
		if (!active) return;
		totals.merge(player, amount, Double::sum);
	}

	public static void reset() { totals.clear(); }

	public static List<Map.Entry<String, Double>> ranking() {
		List<Map.Entry<String, Double>> list = new ArrayList<>(totals.entrySet());
		list.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
		return list;
	}

	public static String money(double v) {
		String[] suf = {"", "K", "M", "B", "T"};
		int i = 0;
		while (v >= 1000 && i < suf.length - 1) { v /= 1000; i++; }
		String s = (v == Math.floor(v)) ? String.valueOf((long) v) : String.format(Locale.ROOT, "%.2f", v);
		if (s.contains(".")) s = s.replaceAll("0+$", "").replaceAll("\\.$", "");
		return "$" + s + suf[i];
	}
}
