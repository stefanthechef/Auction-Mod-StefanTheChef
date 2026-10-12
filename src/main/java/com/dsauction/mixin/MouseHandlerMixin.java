package com.dsauction.mixin;

import com.dsauction.AuctionHud;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @Inject(method = "onPress", at = @At("HEAD"), cancellable = true)
    private void dsauction$startHudDrag(long window, int button, int action, int mods, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (button == 0 && action == 1 && AuctionHud.isHudHovered(mc)) {
            ci.cancel();
        }
    }

    @Inject(method = "onScroll", at = @At("HEAD"))
    private void dsauction$resizeHud(long window, double horizontal, double vertical, CallbackInfo ci) {
        AuctionHud.onScroll(vertical);
    }
}
