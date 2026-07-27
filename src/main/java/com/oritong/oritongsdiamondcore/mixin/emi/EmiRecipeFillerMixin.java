package com.oritong.oritongsdiamondcore.mixin.emi;

import com.oritong.oritongsdiamondcore.compat.emi.TConstructCraftingStationRecipeHandler;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.registry.EmiRecipeFiller;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.tconstruct.tables.menu.CraftingStationContainerMenu;

import java.util.List;

@Mixin(value = EmiRecipeFiller.class, remap = false)
public abstract class EmiRecipeFillerMixin {

    @Inject(method = "getAllHandlers", at = @At("HEAD"), cancellable = true, remap = false)
    private static <T extends AbstractContainerMenu> void oritongsdiamondcore$useCraftingStationHandler(
            AbstractContainerScreen<T> screen, CallbackInfoReturnable<List<EmiRecipeHandler<T>>> cir) {
        if (screen != null && screen.getMenu() instanceof CraftingStationContainerMenu) {
            cir.setReturnValue(List.of(TConstructCraftingStationRecipeHandler.cast()));
        }
    }
}
