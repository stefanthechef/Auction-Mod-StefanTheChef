package com.dsauction;

import java.util.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/** Holds the live auction: the item, on/off flag and each player's TOTAL paid. */
public final class AuctionState {
	public static Item item = Items.DIAMOND_BLOCK;
	public static boolean active = false;
	public static boolean hudVisible = true;
    public static int hudX = -1; // -1 means right aligned
    public static int hudY = 6;
    public static int hudWidth = 190;
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
