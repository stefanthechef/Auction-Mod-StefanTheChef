package com.dsauction.mixin;

import com.dsauction.AuctionHud;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @Inject(method = "onScroll", at = @At("HEAD"))
    private void dsauction$resizeHud(long window, double horizontal, double vertical, CallbackInfo ci) {
        AuctionHud.onScroll(vertical);
    }
}
