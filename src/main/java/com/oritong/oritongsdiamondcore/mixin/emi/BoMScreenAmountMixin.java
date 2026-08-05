package com.oritong.oritongsdiamondcore.mixin.emi;

import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.BoMScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EmiRenderHelper.class, priority = 100, remap = false)
public abstract class BoMScreenAmountMixin {
    @Inject(method = "renderAmount", at = @At("HEAD"), cancellable = true, remap = false)
    private static void oritongsdiamondcore$renderBomAmount(
            EmiDrawContext context, int x, int y, Component amount, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!(minecraft.screen instanceof BoMScreen)) {
            return;
        }

        context.push();
        try {
            context.matrices().translate(0.0F, 0.0F, 200.0F);
            int textX = x + 17 - Math.min(14, minecraft.font.width(amount));
            context.drawTextWithShadow(amount, textX, y + 9, -1);
        } finally {
            context.pop();
        }
        ci.cancel();
    }
}
