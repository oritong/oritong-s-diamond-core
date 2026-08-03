package com.oritong.oritongsdiamondcore.mixin.bettergtae;

import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.google.common.collect.BiMap;

import net.minecraft.world.item.ItemStack;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.crafting.pattern.EncodedPatternItem;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.ialdaiaxiariyay.bettergtae.common.machine.multiblock.part.CraftingPatternPartMachine;

import java.util.function.Function;
import java.util.function.Predicate;

@Mixin(value = CraftingPatternPartMachine.class, remap = false)
public abstract class CraftingPatternPartMachineMixin {

    @Shadow
    @Final
    private BiMap<IPatternDetails, CraftingPatternPartMachine.InternalSlot> detailsSlotMap;

    @Redirect(method = "<init>",
            at = @At(value = "INVOKE",
                    target = "Lcom/gregtechceu/gtceu/api/transfer/item/CustomItemStackHandler;setFilter(Ljava/util/function/Predicate;)V"),
            remap = false)
    private void oritongsdiamondcore$allowEncodedPatternItems(CustomItemStackHandler inventory,
                                                              Predicate<ItemStack> originalFilter) {
        inventory.setFilter(stack -> stack.getItem() instanceof EncodedPatternItem);
    }

    @ModifyArg(method = "createUIWidget",
            at = @At(value = "INVOKE",
                    target = "Lcom/gregtechceu/gtceu/integration/ae2/gui/widget/slot/AEPatternViewSlotWidget;setItemHook(Ljava/util/function/Function;)Lcom/gregtechceu/gtceu/api/gui/widget/SlotWidget;"),
            index = 0,
            remap = false)
    private Function<ItemStack, ItemStack> oritongsdiamondcore$showEncodedPatternOutputs(
            Function<ItemStack, ItemStack> originalHook) {
        return stack -> {
            if (!stack.isEmpty() && stack.getItem() instanceof EncodedPatternItem encodedPattern) {
                ItemStack output = encodedPattern.getOutput(stack);
                if (!output.isEmpty()) {
                    return output;
                }
            }
            return originalHook.apply(stack);
        };
    }

    @Inject(method = "pushPattern", at = @At("HEAD"), cancellable = true, remap = false)
    private void oritongsdiamondcore$pushPatternWithRealMultiplier(IPatternDetails patternDetails,
                                                                   KeyCounter[] inputHolder,
                                                                   CallbackInfoReturnable<Boolean> cir) {
        CraftingPatternPartMachine machine = (CraftingPatternPartMachine) (Object) this;
        if (!machine.isFormed() ||
                !machine.getMainNode().isActive() ||
                !this.detailsSlotMap.containsKey(patternDetails) ||
                !oritongsdiamondcore$checkInput(inputHolder)) {
            cir.setReturnValue(false);
            return;
        }

        CraftingPatternPartMachine.InternalSlot slot = this.detailsSlotMap.get(patternDetails);
        GenericStack output = oritongsdiamondcore$getPrimaryOutput(patternDetails);
        long multiplier = oritongsdiamondcore$calculateMultiplier(patternDetails, inputHolder);
        if (slot == null || output == null || multiplier <= 0) {
            cir.setReturnValue(false);
            return;
        }

        slot.pushPattern(patternDetails, inputHolder);
        machine.outputItems.addTo(output, multiplier);
        cir.setReturnValue(true);
    }

    private static GenericStack oritongsdiamondcore$getPrimaryOutput(IPatternDetails patternDetails) {
        GenericStack[] outputs = patternDetails.getOutputs();
        return outputs.length > 0 ? outputs[0] : null;
    }

    private static boolean oritongsdiamondcore$checkInput(KeyCounter[] inputHolder) {
        if (inputHolder == null) {
            return false;
        }
        for (KeyCounter input : inputHolder) {
            if (input == null) {
                return false;
            }
            for (Object2LongMap.Entry<AEKey> entry : input) {
                AEKey key = entry.getKey();
                if (!(key instanceof AEItemKey || key instanceof AEFluidKey)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static long oritongsdiamondcore$calculateMultiplier(IPatternDetails patternDetails,
                                                               KeyCounter[] inputHolder) {
        IPatternDetails.IInput[] inputs = patternDetails.getInputs();
        long multiplier = Long.MAX_VALUE;
        boolean foundInput = false;

        for (int i = 0; i < inputs.length; i++) {
            IPatternDetails.IInput input = inputs[i];
            if (input == null) {
                continue;
            }
            if (i >= inputHolder.length) {
                return 0;
            }

            long inputMultiplier = oritongsdiamondcore$calculateInputMultiplier(input, inputHolder[i]);
            foundInput = true;
            if (inputMultiplier <= 0) {
                return 0;
            }
            multiplier = Math.min(multiplier, inputMultiplier);
        }

        return foundInput && multiplier != Long.MAX_VALUE ? multiplier : 0;
    }

    private static long oritongsdiamondcore$calculateInputMultiplier(IPatternDetails.IInput input,
                                                                    KeyCounter inputHolder) {
        long requiredMultiplier = Math.max(1L, input.getMultiplier());
        long normalizedAmount = 0;
        long rawAmount = 0;
        boolean matchedTemplate = false;

        for (Object2LongMap.Entry<AEKey> entry : inputHolder) {
            long amount = entry.getLongValue();
            if (amount <= 0) {
                continue;
            }

            rawAmount += amount;
            long templateAmount = oritongsdiamondcore$getTemplateAmount(input, entry.getKey());
            if (templateAmount > 0) {
                matchedTemplate = true;
                normalizedAmount += amount / templateAmount;
            }
        }

        if (rawAmount <= 0) {
            return 0;
        }
        if (!matchedTemplate) {
            return rawAmount / requiredMultiplier;
        }
        return normalizedAmount / requiredMultiplier;
    }

    private static long oritongsdiamondcore$getTemplateAmount(IPatternDetails.IInput input, AEKey key) {
        GenericStack[] possibleInputs = input.getPossibleInputs();
        if (possibleInputs.length == 0) {
            return 1;
        }

        for (GenericStack possibleInput : possibleInputs) {
            if (possibleInput != null && key.matches(possibleInput)) {
                return Math.max(1L, possibleInput.amount());
            }
        }
        return 0;
    }
}
