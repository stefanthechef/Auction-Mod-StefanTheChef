package com.dsauction;

import com.google.gson.*;
import java.nio.file.*;
import java.util.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.*;

/** Survival-obtainable item picker. Excludes spawn eggs, potion variants and admin/command-only items. */
public final class BlockList {
	private static final List<String> DEFAULT_DENY = List.of(
        "bedrock", "barrier", "light", "command_block", "chain_command_block", "repeating_command_block",
        "structure_block", "structure_void", "jigsaw", "test_block", "test_instance_block", "spawner",
        "trial_spawner", "vault", "reinforced_deepslate", "end_portal_frame", "budding_amethyst",
        "petrified_oak_slab", "farmland", "dirt_path", "frogspawn", "player_head", "player_wall_head",
        "infested_stone", "infested_cobblestone", "infested_stone_bricks", "infested_mossy_stone_bricks",
        "infested_cracked_stone_bricks", "infested_chiseled_stone_bricks", "infested_deepslate",
        "debug_stick", "knowledge_book", "structure_block", "barrier", "lightning_rod_unused");
	private static Set<String> deny;
	private static List<Item> cache;

	private BlockList() {}

	private static void loadConfig() {
		deny = new HashSet<>(DEFAULT_DENY);
		Path p = FabricLoader.getInstance().getConfigDir().resolve("dsauction.json");
		try {
			if (Files.exists(p)) {
				JsonObject o = JsonParser.parseString(Files.readString(p)).getAsJsonObject();
				if (o.has("deniedItems")) {
					deny.clear();
					for (JsonElement e : o.getAsJsonArray("deniedItems")) deny.add(e.getAsString());
				}
			} else {
				JsonObject o = new JsonObject();
				JsonArray a = new JsonArray();
				DEFAULT_DENY.forEach(a::add);
				o.add("deniedItems", a);
				Files.writeString(p, new GsonBuilder().setPrettyPrinting().create().toJson(o));
			}
		} catch (Exception ignored) {}
	}

	public static List<Item> all() {
		if (cache != null) return cache;
		loadConfig();
		List<Item> out = new ArrayList<>();
		for (Item item : BuiltInRegistries.ITEM) {
			String id = BuiltInRegistries.ITEM.getKey(item).getPath();
            // Keep the picker focused on items players can normally obtain; no spawn eggs, potions,
            // command-only items, or technical item variants.
            if (deny.contains(id) || id.startsWith("infested_") || id.endsWith("_spawn_egg")
                    || id.equals("potion") || id.equals("splash_potion") || id.equals("lingering_potion")
                    || id.equals("tipped_arrow") || id.startsWith("unused_")) continue;
			out.add(item);
		}
		out.sort(Comparator.comparing(i -> name(i)));
		cache = out;
		return cache;
	}

	public static String name(Item item) { return new ItemStack(item).getHoverName().getString(); }
}
