package com.oritong.oritongsdiamondcore.mixin.botania;

import dev.emi.emi.api.stack.EmiStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import vazkii.botania.client.integration.emi.BotaniaEmiRecipe;

import java.util.List;

@Mixin(value = BotaniaEmiRecipe.class, remap = false)
public interface BotaniaEmiRecipeAccessor {
    @Accessor(value = "output", remap = false)
    List<EmiStack> oritongsdiamondcore$getOutput();

    @Accessor(value = "output", remap = false)
    void oritongsdiamondcore$setOutput(List<EmiStack> output);
}
