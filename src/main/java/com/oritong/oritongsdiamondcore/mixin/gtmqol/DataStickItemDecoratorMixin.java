package com.oritong.oritongsdiamondcore.mixin.gtmqol;

import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.utils.ResearchManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.List;

@Mixin(targets = "cn.elytra.mod.gtmqol.client.item_decorator.DataStickItemDecorator", remap = false)
public abstract class DataStickItemDecoratorMixin {

    @Inject(method = "getItemToRender", at = @At("HEAD"), cancellable = true, remap = false)
    private void oritongsdiamondcore$getItemToRender(ItemStack itemStack, CallbackInfoReturnable<ItemStack> cir) {
        try {
            ResearchManager.ResearchItem researchItem = ResearchManager.readResearchId(itemStack);
            if (researchItem == null) {
                cir.setReturnValue(null);
                return;
            }

            Collection<GTRecipe> recipes = researchItem.recipeType().getDataStickEntry(researchItem.researchId());
            if (recipes == null || recipes.isEmpty()) {
                cir.setReturnValue(null);
                return;
            }

            List<Content> outputs = recipes.iterator().next().getOutputContents(ItemRecipeCapability.CAP);
            if (outputs.isEmpty()) {
                cir.setReturnValue(null);
                return;
            }

            Ingredient ingredient = ItemRecipeCapability.CAP.of(outputs.get(0).content);
            ItemStack[] stacks = ingredient.getItems();
            cir.setReturnValue(stacks.length > 0 ? stacks[0] : null);
        } catch (RuntimeException | LinkageError ignored) {
            cir.setReturnValue(null);
        }
    }
}
