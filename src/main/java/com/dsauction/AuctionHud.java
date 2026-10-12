package com.dsauction;

import java.util.*;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

/** The little overlay: item being auctioned, top bidder and the TOTAL they've paid. */
public final class AuctionHud {
	private AuctionHud() {}

	public static void register() {
		HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("dsauction", "hud"), (g, tick) -> render(g));
        ClientTickEvents.END_CLIENT_TICK.register(AuctionHud::handleMouse);
	}

    private static void handleMouse(Minecraft mc) {
        if (mc == null || mc.options.hideGui || (mc.screen != null && !(mc.screen instanceof ChatScreen))) {
            AuctionState.hudDragging = false;
            AuctionState.setPreviousLeftPressed(false);
            return;
        }
        boolean pressed = GLFW.glfwGetMouseButton(mc.getWindow().handle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        // MouseHandler xpos/ypos are in window coordinates, not framebuffer pixels.
        double scaleX = (double) mc.getWindow().getGuiScaledWidth() / Math.max(1, mc.getWindow().getWidth());
        double scaleY = (double) mc.getWindow().getGuiScaledHeight() / Math.max(1, mc.getWindow().getHeight());
        int mouseX = (int) (mc.mouseHandler.xpos() * scaleX);
        int mouseY = (int) (mc.mouseHandler.ypos() * scaleY);
        int w = (int) Math.ceil(AuctionState.hudWidth * AuctionState.hudScale);
        List<Map.Entry<String, Double>> rank = AuctionState.ranking();
        int rows = Math.min(2, Math.max(0, rank.size() - 1));
        int h = (int) Math.ceil((98 + rows * 10) * AuctionState.hudScale);
        int x = AuctionState.hudX < 0 ? mc.getWindow().getGuiScaledWidth() - w - 6 : AuctionState.hudX;
        int y = AuctionState.hudY;
        if (pressed && !AuctionState.wasLeftPressed() && AuctionState.hudVisible && mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h) {
            AuctionState.hudDragging = true;
            AuctionState.dragOffsetX = mouseX - x;
            AuctionState.dragOffsetY = mouseY - y;
        }
        if (pressed && AuctionState.hudDragging) {
            AuctionState.hudX = Math.max(0, Math.min(mouseX - AuctionState.dragOffsetX, mc.getWindow().getGuiScaledWidth() - w));
            AuctionState.hudY = Math.max(0, Math.min(mouseY - AuctionState.dragOffsetY, mc.getWindow().getGuiScaledHeight() - h));
        }
        if (!pressed && AuctionState.wasLeftPressed() && AuctionState.hudDragging) {
            AuctionState.hudDragging = false;
            AuctionState.saveHudSettings();
        }
        AuctionState.setPreviousLeftPressed(pressed);
    }

    /** Called by the mouse scroll mixin. Positive wheel movement enlarges the HUD. */
    public static void onScroll(double vertical) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || (mc.screen != null && !(mc.screen instanceof ChatScreen)) || !AuctionState.hudVisible || vertical == 0) return;
        double scaleX = (double) mc.getWindow().getGuiScaledWidth() / Math.max(1, mc.getWindow().getWidth());
        double scaleY = (double) mc.getWindow().getGuiScaledHeight() / Math.max(1, mc.getWindow().getHeight());
        int mouseX = (int) (mc.mouseHandler.xpos() * scaleX);
        int mouseY = (int) (mc.mouseHandler.ypos() * scaleY);
        int w = (int) Math.ceil(AuctionState.hudWidth * AuctionState.hudScale);
        int rows = Math.min(2, Math.max(0, AuctionState.ranking().size() - 1));
        int h = (int) Math.ceil((98 + rows * 10) * AuctionState.hudScale);
        int x = AuctionState.hudX < 0 ? mc.getWindow().getGuiScaledWidth() - w - 6 : AuctionState.hudX;
        int y = AuctionState.hudY;
        if (AuctionState.hudDragging || (mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h)) {
            AuctionState.hudScale = Math.max(0.6, Math.min(2.0, AuctionState.hudScale + (vertical > 0 ? 0.1 : -0.1)));
            AuctionState.hudX = Math.max(0, Math.min(x, mc.getWindow().getGuiScaledWidth() - (int)Math.ceil(AuctionState.hudWidth * AuctionState.hudScale)));
            AuctionState.hudY = Math.max(0, Math.min(y, mc.getWindow().getGuiScaledHeight() - (int)Math.ceil((98 + rows * 10) * AuctionState.hudScale)));
            AuctionState.saveHudSettings();
        }
    }

    public static boolean isHudHovered(Minecraft mc) {
        if (mc == null || !AuctionState.hudVisible || mc.options.hideGui || mc.screen == null || !(mc.screen instanceof ChatScreen)) return false;
        double scaleX = (double) mc.getWindow().getGuiScaledWidth() / Math.max(1, mc.getWindow().getWidth());
        double scaleY = (double) mc.getWindow().getGuiScaledHeight() / Math.max(1, mc.getWindow().getHeight());
        int mouseX = (int) (mc.mouseHandler.xpos() * scaleX);
        int mouseY = (int) (mc.mouseHandler.ypos() * scaleY);
        int w = (int)Math.ceil(AuctionState.hudWidth * AuctionState.hudScale);
        int rows = Math.min(2, Math.max(0, AuctionState.ranking().size() - 1));
        int h = (int)Math.ceil((98 + rows * 10) * AuctionState.hudScale);
        int x = AuctionState.hudX < 0 ? mc.getWindow().getGuiScaledWidth() - w - 6 : AuctionState.hudX;
        int y = AuctionState.hudY;
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    private static void render(GuiGraphics g) {
        Minecraft mc = Minecraft.getInstance();
        if (!AuctionState.hudVisible || mc.options.hideGui) return;
        Font f = mc.font;
        List<Map.Entry<String, Double>> rank = AuctionState.ranking();
        int baseW = AuctionState.hudWidth, rows = Math.min(2, Math.max(0, rank.size() - 1));
        int baseH = 98 + rows * 10;
        int w = (int)Math.ceil(baseW * AuctionState.hudScale);
        int h = (int)Math.ceil(baseH * AuctionState.hudScale);
        int x = AuctionState.hudX < 0 ? g.guiWidth() - w - 6 : Math.max(0, Math.min(AuctionState.hudX, g.guiWidth() - w));
        int y = Math.max(0, Math.min(AuctionState.hudY, g.guiHeight() - h));

        if (mc.screen instanceof ChatScreen && isHudHovered(mc)) {
            int outline = 0xFFFFFFFF;
            g.fill(x - 1, y - 1, x + w + 1, y, outline);
            g.fill(x - 1, y + h, x + w + 1, y + h + 1, outline);
            g.fill(x - 1, y, x, y + h, outline);
            g.fill(x + w, y, x + w + 1, y + h, outline);
        }
        g.pose().pushMatrix();
        g.pose().translate((float)x, (float)y);
        g.pose().scale((float)AuctionState.hudScale, (float)AuctionState.hudScale);
        g.fill(0, 0, baseW, baseH, 0xC0101010);
        g.fill(0, 0, baseW, 1, 0xFFFFD700);
        g.fill(0, baseH - 1, baseW, baseH, 0xFFFFD700);
        g.drawString(f, "AUCTION", 6, 5, 0xFFFFD700);
        String st = AuctionState.active ? "LIVE" : "ENDED";
        g.drawString(f, st, baseW - 6 - f.width(st), 5, AuctionState.active ? 0xFF55FF55 : 0xFFFF5555);
        long remaining = AuctionState.secondsRemaining();
        if (remaining >= 0) {
            String timer = String.format(Locale.ROOT, "%02d:%02d:%02d", remaining / 3600, (remaining % 3600) / 60, remaining % 60);
            g.drawString(f, timer, (baseW - f.width(timer)) / 2, 37, 0xFFFFFFFF);
            if (remaining == 0 && AuctionState.active) {
                AuctionState.active = false;
                AuctionState.stopTimer();
            }
        }
        g.renderItem(new ItemStack(AuctionState.item), 6, 17);
        String name = BlockList.name(AuctionState.item);
        while (f.width(name) > baseW - 34 && name.length() > 3) name = name.substring(0, name.length() - 2);
        g.drawString(f, name, 28, 22, 0xFFFFFFFF);
        g.fill(6, 53, baseW - 6, 54, 0x55FFFFFF);
        if (rank.isEmpty()) {
            g.drawString(f, "No bids yet", 6, 62, 0xFFAAAAAA);
            g.pose().popMatrix();
            return;
        }
        Map.Entry<String, Double> top = rank.get(0);
        g.drawString(f, "TOP BIDDER", 6, 58, 0xFF888888);
        g.drawString(f, top.getKey(), 6, 68, 0xFFFFFFFF);
        String amt = AuctionState.money(top.getValue());
        g.drawString(f, amt, baseW - 6 - f.width(amt), 68, 0xFF55FF55);
        g.drawString(f, "total paid", baseW - 6 - f.width("total paid"), 58, 0xFF888888);
        for (int i = 1; i <= rows; i++) {
            Map.Entry<String, Double> e = rank.get(i);
            g.drawString(f, (i + 1) + ". " + e.getKey(), 6, 72 + i * 10, 0xFFAAAAAA);
            String a = AuctionState.money(e.getValue());
            g.drawString(f, a, baseW - 6 - f.width(a), 72 + i * 10, 0xFFAAAAAA);
        }
        g.pose().popMatrix();
    }
}
