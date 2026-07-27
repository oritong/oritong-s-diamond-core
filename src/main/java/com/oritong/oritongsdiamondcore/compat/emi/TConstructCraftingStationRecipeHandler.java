package com.oritong.oritongsdiamondcore.compat.emi;

import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.platform.EmiClient;
import dev.emi.emi.registry.EmiRecipeFiller;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.common.crafting.IShapedRecipe;
import slimeknights.tconstruct.tables.menu.CraftingStationContainerMenu;

import java.util.ArrayList;
import java.util.List;

public enum TConstructCraftingStationRecipeHandler implements StandardRecipeHandler<CraftingStationContainerMenu> {
    INSTANCE;

    private static final int CRAFTING_GRID_SIZE = 9;
    private static final int RESULT_SLOT_INDEX = 9;
    private static final int PLAYER_INVENTORY_SIZE = 36;

    @SuppressWarnings("unchecked")
    public static <T extends net.minecraft.world.inventory.AbstractContainerMenu> EmiRecipeHandler<T> cast() {
        return (EmiRecipeHandler<T>) INSTANCE;
    }

    @Override
    public List<Slot> getInputSources(CraftingStationContainerMenu menu) {
        List<Slot> slots = new ArrayList<>();
        int totalSize = menu.slots.size();
        int playerInventoryStart = Math.max(RESULT_SLOT_INDEX + 1, totalSize - PLAYER_INVENTORY_SIZE);

        for (int i = playerInventoryStart; i < totalSize; i++) {
            slots.add(menu.getSlot(i));
        }

        Player player = Minecraft.getInstance().player;
        for (int i = RESULT_SLOT_INDEX + 1; i < playerInventoryStart; i++) {
            Slot slot = menu.getSlot(i);
            if (slot.hasItem() && (player == null || slot.allowModification(player))) {
                slots.add(slot);
            }
        }

        for (int i = 0; i < CRAFTING_GRID_SIZE && i < totalSize; i++) {
            slots.add(menu.getSlot(i));
        }

        return slots;
    }

    @Override
    public List<Slot> getCraftingSlots(CraftingStationContainerMenu menu) {
        List<Slot> slots = new ArrayList<>();
        for (int i = 0; i < CRAFTING_GRID_SIZE && i < menu.slots.size(); i++) {
            slots.add(menu.getSlot(i));
        }
        return slots;
    }

    @Override
    public Slot getOutputSlot(CraftingStationContainerMenu menu) {
        if (menu.slots.size() > RESULT_SLOT_INDEX) {
            return menu.getSlot(RESULT_SLOT_INDEX);
        }
        return null;
    }

    @Override
    public EmiPlayerInventory getInventory(AbstractContainerScreen<CraftingStationContainerMenu> screen) {
        return new EmiPlayerInventory(getInputSources(screen.getMenu()).stream()
                .map(Slot::getItem)
                .map(EmiStack::of)
                .toList());
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        if (recipe.getCategory() != VanillaEmiRecipeCategories.CRAFTING || !recipe.supportsRecipeTree()) {
            return false;
        }
        Recipe<?> backingRecipe = recipe.getBackingRecipe();
        if (backingRecipe instanceof IShapedRecipe<?> shapedRecipe) {
            return shapedRecipe.getRecipeWidth() <= 3 && shapedRecipe.getRecipeHeight() <= 3;
        }
        return recipe.getInputs().size() <= CRAFTING_GRID_SIZE;
    }

    @Override
    public boolean canCraft(EmiRecipe recipe, EmiCraftContext<CraftingStationContainerMenu> context) {
        return supportsRecipe(recipe) && context.getInventory().canCraft(recipe);
    }

    @Override
    public boolean craft(EmiRecipe recipe, EmiCraftContext<CraftingStationContainerMenu> context) {
        List<ItemStack> stacks = getReusableAwareStacks(recipe, context.getScreen(), Math.max(1, context.getAmount()));
        if (stacks == null) {
            return false;
        }

        Minecraft.getInstance().setScreen(context.getScreen());
        if (!EmiClient.onServer) {
            return EmiRecipeFiller.clientFill(this, recipe, context.getScreen(), stacks, context.getDestination());
        }

        EmiClient.sendFillRecipe(this, context.getScreen(), context.getScreenHandler().containerId,
                getDestinationMode(context.getDestination()), stacks, recipe);
        return true;
    }

    private List<ItemStack> getReusableAwareStacks(EmiRecipe recipe,
                                                   AbstractContainerScreen<CraftingStationContainerMenu> screen,
                                                   int requestedAmount) {
        CraftingStationContainerMenu menu = screen.getMenu();
        List<EmiIngredient> inputs = recipe.getInputs();
        List<Slot> craftingSlots = getCraftingSlots(recipe, menu);
        if (inputs.isEmpty() || inputs.size() > craftingSlots.size()) {
            return null;
        }

        List<Slot> inputSources = getInputSources(menu);
        List<SelectedInput> selectedInputs = new ArrayList<>();
        for (EmiIngredient ingredient : inputs) {
            if (ingredient.isEmpty()) {
                selectedInputs.add(SelectedInput.empty());
                continue;
            }

            SelectedInput selectedInput = selectInput(ingredient, inputSources, selectedInputs);
            if (selectedInput == null) {
                return null;
            }
            selectedInputs.add(selectedInput);
        }

        int targetBatches = saturatedAdd(requestedAmount,
                EmiRecipeFiller.batchesAlreadyPresent(recipe, this, screen));
        int craftableBatches = getCraftableBatches(selectedInputs, inputSources, targetBatches);
        if (craftableBatches <= 0) {
            return null;
        }

        List<ItemStack> stacks = new ArrayList<>();
        for (SelectedInput selectedInput : selectedInputs) {
            if (selectedInput.isEmpty()) {
                stacks.add(ItemStack.EMPTY);
                continue;
            }

            ItemStack stack = selectedInput.stack().copy();
            int count = selectedInput.reusableTool()
                    ? selectedInput.consumedPerCraft()
                    : selectedInput.consumedPerCraft() * craftableBatches;
            stack.setCount(count);
            stacks.add(stack);
        }
        return stacks;
    }

    private SelectedInput selectInput(EmiIngredient ingredient, List<Slot> inputSources,
                                      List<SelectedInput> selectedInputs) {
        int consumed = getIngredientAmount(ingredient);
        SelectedInput best = null;
        int bestBatches = -1;

        for (EmiStack variant : ingredient.getEmiStacks()) {
            ItemStack matchingStack = findMatchingStack(variant, inputSources);
            if (matchingStack.isEmpty()) {
                continue;
            }

            boolean reusableTool = isReusableTool(matchingStack);
            if (reusableTool && consumed > matchingStack.getMaxStackSize()) {
                continue;
            }
            int existingRequired = getRequiredPerCraft(selectedInputs, matchingStack, reusableTool);
            int available = countAvailable(inputSources, matchingStack);
            int totalRequired = existingRequired + consumed;
            if (totalRequired <= 0 || available < totalRequired) {
                continue;
            }

            int batches = reusableTool ? Integer.MAX_VALUE : available / totalRequired;
            if (batches > bestBatches) {
                bestBatches = batches;
                best = new SelectedInput(matchingStack.copy(), consumed, reusableTool);
            }
        }

        return best;
    }

    private int getCraftableBatches(List<SelectedInput> selectedInputs, List<Slot> inputSources, int requestedAmount) {
        int batches = Math.max(1, requestedAmount);

        for (SelectedInput selectedInput : selectedInputs) {
            if (selectedInput.isEmpty()) {
                continue;
            }

            int requiredPerCraft = getRequiredPerCraft(selectedInputs, selectedInput.stack(),
                    selectedInput.reusableTool());
            int available = countAvailable(inputSources, selectedInput.stack());
            if (available < requiredPerCraft) {
                return 0;
            }

            if (selectedInput.reusableTool()) {
                continue;
            }

            batches = Math.min(batches, available / requiredPerCraft);
            batches = Math.min(batches,
                    selectedInput.stack().getMaxStackSize() / Math.max(1, selectedInput.consumedPerCraft()));
        }

        return batches;
    }

    private int getRequiredPerCraft(List<SelectedInput> selectedInputs, ItemStack stack, boolean reusableTool) {
        int required = 0;
        for (SelectedInput selectedInput : selectedInputs) {
            if (!selectedInput.isEmpty() && selectedInput.reusableTool() == reusableTool
                    && ItemStack.isSameItemSameTags(selectedInput.stack(), stack)) {
                required += selectedInput.consumedPerCraft();
            }
        }
        return required;
    }

    private ItemStack findMatchingStack(EmiStack variant, List<Slot> inputSources) {
        for (Slot slot : inputSources) {
            ItemStack stack = slot.getItem();
            if (!stack.isEmpty() && EmiStack.of(stack).isEqual(variant)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private int countAvailable(List<Slot> inputSources, ItemStack targetStack) {
        int available = 0;
        for (Slot slot : inputSources) {
            ItemStack stack = slot.getItem();
            if (ItemStack.isSameItemSameTags(stack, targetStack)) {
                available += stack.getCount();
            }
        }
        return available;
    }

    private int getIngredientAmount(EmiIngredient ingredient) {
        long amount = ingredient.getAmount();
        if (amount <= 0L) {
            return 1;
        }
        return (int) Math.min(Integer.MAX_VALUE, amount);
    }

    private int saturatedAdd(int left, int right) {
        long result = (long) left + right;
        return result >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) result;
    }

    private boolean isReusableTool(ItemStack stack) {
        return !stack.isEmpty() && stack.getMaxStackSize() == 1 && stack.isDamageableItem();
    }

    private int getDestinationMode(EmiCraftContext.Destination destination) {
        return switch (destination) {
            case NONE -> 0;
            case CURSOR -> 1;
            case INVENTORY -> 2;
        };
    }

    private record SelectedInput(ItemStack stack, int consumedPerCraft, boolean reusableTool) {
        private static SelectedInput empty() {
            return new SelectedInput(ItemStack.EMPTY, 0, false);
        }

        private boolean isEmpty() {
            return stack.isEmpty();
        }
    }
}
