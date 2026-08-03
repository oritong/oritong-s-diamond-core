package com.oritong.oritongsdiamondcore.mixin.bettergtae;

import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import top.ialdaiaxiariyay.bettergtae.common.data.machine.BGTAEMultiblockA;

import java.util.Arrays;

@Mixin(value = BGTAEMultiblockA.class, remap = false)
public abstract class BGTAEMultiblockAMixin {

    @Redirect(method = "lambda$static$3",
            at = @At(value = "INVOKE",
                    target = "Lcom/gregtechceu/gtceu/api/pattern/TraceabilityPredicate;or(Lcom/gregtechceu/gtceu/api/pattern/TraceabilityPredicate;)Lcom/gregtechceu/gtceu/api/pattern/TraceabilityPredicate;",
                    ordinal = 3),
            remap = false)
    private static TraceabilityPredicate oritongsdiamondcore$removeLargeMolecularAssemblerParallelHatch(
            TraceabilityPredicate current,
            TraceabilityPredicate parallelHatchPredicate) {
        return current;
    }

    @ModifyArg(method = "<clinit>",
            at = @At(value = "INVOKE",
                    target = "Lcom/gregtechceu/gtceu/api/registry/registrate/MultiblockMachineBuilder;recipeModifiers([Lcom/gregtechceu/gtceu/api/recipe/modifier/RecipeModifier;)Lcom/gregtechceu/gtceu/api/registry/registrate/MachineBuilder;",
                    ordinal = 1),
            index = 0,
            remap = false)
    private static RecipeModifier[] oritongsdiamondcore$removeLargeMolecularAssemblerParallelModifier(
            RecipeModifier[] modifiers) {
        return Arrays.stream(modifiers)
                .filter(modifier -> modifier != GTRecipeModifiers.PARALLEL_HATCH)
                .toArray(RecipeModifier[]::new);
    }
}
