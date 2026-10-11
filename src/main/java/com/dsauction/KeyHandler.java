package com.dsauction;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

/** Press K to open the menu. (If this file ever fails to compile, delete KeyHandler.register() in DsAuctionClient and use /ac.) */
public final class KeyHandler {
	private KeyHandler() {}

	public static void register() {
		KeyMapping.Category cat = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("dsauction", "main"));
		KeyMapping open = KeyBindingHelper.registerKeyBinding(
			new KeyMapping("key.dsauction.open", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, cat));
		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			while (open.consumeClick()) if (mc.screen == null) mc.setScreen(new BlockPickerScreen());
		});
	}
}
