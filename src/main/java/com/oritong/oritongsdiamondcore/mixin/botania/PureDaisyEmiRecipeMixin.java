package com.oritong.oritongsdiamondcore.mixin.botania;

import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.botania.api.recipe.PureDaisyRecipe;
import vazkii.botania.client.integration.emi.PureDaisyEmiRecipe;

import java.util.List;

@Mixin(value = PureDaisyEmiRecipe.class, remap = false)
public abstract class PureDaisyEmiRecipeMixin {
    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void oritongsdiamondcore$useFluidOutput(PureDaisyRecipe recipe, CallbackInfo ci) {
        BotaniaEmiRecipeAccessor accessor = (BotaniaEmiRecipeAccessor) this;
        if (!hasEmptyItemOutput(accessor.oritongsdiamondcore$getOutput())) {
            return;
        }

        BlockState outputState = recipe.getOutputState();
        FluidState fluidState = outputState.getFluidState();
        if (!fluidState.isEmpty()) {
            accessor.oritongsdiamondcore$setOutput(List.of(EmiStack.of(fluidState.getType())));
        }
    }

    private boolean hasEmptyItemOutput(List<EmiStack> output) {
        return output.isEmpty() || output.get(0).isEmpty();
    }
}
