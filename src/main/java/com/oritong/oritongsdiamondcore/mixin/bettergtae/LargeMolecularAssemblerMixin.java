package com.oritong.oritongsdiamondcore.mixin.bettergtae;

import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraft.world.item.ItemStack;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.ialdaiaxiariyay.bettergtae.common.machine.multiblock.electric.LargeMolecularAssembler;
import top.ialdaiaxiariyay.bettergtae.common.machine.multiblock.part.CraftingPatternPartMachine;

import java.util.List;

@Mixin(value = LargeMolecularAssembler.class, remap = false)
public abstract class LargeMolecularAssemblerMixin {

    private static final int ORITONGSDIAMONDCORE_MAX_PARALLEL = Integer.MAX_VALUE;

    @Shadow
    private CraftingPatternPartMachine craftingPatternPartMachine;

    @Inject(method = "getGTRecipe", at = @At("HEAD"), cancellable = true, remap = false)
    private void oritongsdiamondcore$getMaxParallelPatternRecipe(CallbackInfoReturnable<GTRecipe> cir) {
        if (this.craftingPatternPartMachine == null) {
            cir.setReturnValue(null);
            return;
        }

        GTRecipe recipe = GTRecipeBuilder.ofRaw().buildRawRecipe();
        List<Content> outputList = recipe.outputs.computeIfAbsent(ItemRecipeCapability.CAP,
                capability -> new ObjectArrayList<>());
        long remainingParallel = ORITONGSDIAMONDCORE_MAX_PARALLEL;
        int totalRuns = 0;

        for (var it = Object2LongMaps.fastIterator(this.craftingPatternPartMachine.outputItems);
             it.hasNext() && remainingParallel > 0;) {
            var entry = it.next();
            GenericStack output = entry.getKey();
            if (!(output.what() instanceof AEItemKey outputKey) || output.amount() <= 0) {
                it.remove();
                continue;
            }

            long queuedRuns = entry.getLongValue();
            long maxRunsForOneContent = Math.max(1L, Integer.MAX_VALUE / output.amount());
            long extractedRuns = Math.min(Math.min(queuedRuns, remainingParallel), maxRunsForOneContent);
            int totalItems = (int) Math.min(Integer.MAX_VALUE, extractedRuns * output.amount());
            if (extractedRuns <= 0 || totalItems <= 0) {
                it.remove();
                continue;
            }

            ItemStack outputStack = outputKey.toStack();
            outputStack.setCount(totalItems);
            outputList.add(new Content(
                    SizedIngredient.create(outputStack),
                    ChanceLogic.getMaxChancedValue(),
                    ChanceLogic.getMaxChancedValue(),
                    0));

            remainingParallel -= extractedRuns;
            totalRuns = oritongsdiamondcore$saturatedAdd(totalRuns, extractedRuns);
            queuedRuns -= extractedRuns;
            if (queuedRuns == 0) {
                it.remove();
            } else {
                entry.setValue(queuedRuns);
            }
        }

        if (outputList.isEmpty()) {
            cir.setReturnValue(null);
            return;
        }

        recipe.duration = Math.max(1, ((WorkableElectricMultiblockMachine) (Object) this).getTier() * 10);
        recipe.parallels = Math.max(1, totalRuns);
        recipe.subtickParallels = 1;
        recipe.batchParallels = 1;
        cir.setReturnValue(recipe);
    }

    private static int oritongsdiamondcore$saturatedAdd(int left, long right) {
        if (right <= 0) {
            return left;
        }
        long sum = left + right;
        return sum > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) sum;
    }
}
