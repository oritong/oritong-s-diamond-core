package com.oritong.oritongsdiamondcore.mixin.industrial;

import com.buuz135.industrial.module.ModuleCore;
import com.buuz135.industrial.module.ModuleTool;
import com.buuz135.industrial.plugin.jei.JEICustomPlugin;

import net.minecraft.world.item.Item;

import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraftforge.registries.RegistryObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Mixin(value = JEICustomPlugin.class, remap = false)
public abstract class JEICustomPluginMixin {

    @Redirect(method = "registerItemSubtypes",
            at = @At(value = "INVOKE",
                    target = "Lmezz/jei/api/registration/ISubtypeRegistration;useNbtForSubtypes([Lnet/minecraft/world/item/Item;)V"),
            remap = false)
    private void oritongsdiamondcore$normalizeInitialSubtypeRegistration(ISubtypeRegistration registration,
                                                                         Item[] items) {
        oritongsdiamondcore$normalizeIndustrialForegoingRecipeLookup(registration, items);
    }

    @Redirect(method = "lambda$registerItemSubtypes$0",
            at = @At(value = "INVOKE",
                    target = "Lmezz/jei/api/registration/ISubtypeRegistration;useNbtForSubtypes([Lnet/minecraft/world/item/Item;)V"),
            remap = false)
    private static void oritongsdiamondcore$normalizeRangeAddonSubtypeRegistration(ISubtypeRegistration registration,
                                                                                   Item[] items) {
        oritongsdiamondcore$normalizeIndustrialForegoingRecipeLookup(registration, items);
    }

    @Unique
    private static void oritongsdiamondcore$normalizeIndustrialForegoingRecipeLookup(ISubtypeRegistration registration,
                                                                                     Item[] items) {
        Set<Item> normalizedItems = oritongsdiamondcore$normalizedRecipeLookupItems();
        List<Item> normalizedThisCall = Arrays.stream(items)
                .filter(normalizedItems::contains)
                .toList();
        Item[] nbtItems = Arrays.stream(items)
                .filter(item -> !normalizedItems.contains(item))
                .toArray(Item[]::new);
        if (nbtItems.length > 0) {
            registration.useNbtForSubtypes(nbtItems);
        }
        normalizedThisCall.forEach(item -> registration.registerSubtypeInterpreter(item,
                (stack, context) -> IIngredientSubtypeInterpreter.NONE));
    }

    @Unique
    private static Set<Item> oritongsdiamondcore$normalizedRecipeLookupItems() {
        List<Item> items = new ArrayList<>();
        items.add(ModuleTool.INFINITY_DRILL.get());
        items.add(ModuleTool.INFINITY_SAW.get());
        items.add(ModuleTool.INFINITY_HAMMER.get());
        items.add(ModuleTool.INFINITY_TRIDENT.get());
        items.add(ModuleTool.INFINITY_BACKPACK.get());
        items.add(ModuleTool.INFINITY_LAUNCHER.get());
        items.add(ModuleTool.INFINITY_NUKE.get());
        items.add(ModuleCore.SPEED_ADDON_1.get());
        items.add(ModuleCore.SPEED_ADDON_2.get());
        items.add(ModuleCore.EFFICIENCY_ADDON_1.get());
        items.add(ModuleCore.EFFICIENCY_ADDON_2.get());
        items.add(ModuleCore.PROCESSING_ADDON_1.get());
        items.add(ModuleCore.PROCESSING_ADDON_2.get());
        Arrays.stream(ModuleCore.RANGE_ADDONS)
                .map(RegistryObject::get)
                .forEach(items::add);
        return Set.copyOf(items);
    }
}
