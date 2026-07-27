package com.oritong.oritongsdiamondcore.mixin.ae2;

import appeng.api.stacks.GenericStack;
import appeng.client.gui.me.items.SetProcessingPatternAmountScreen;
import org.spongepowered.asm.mixin.*;

@Mixin(SetProcessingPatternAmountScreen.class)
public class MixinSetProcessingPatternAmountScreen {

    @Shadow(remap = false)
    @Final
    private GenericStack currentStack;

    @Overwrite(remap = false)
    private long getMaxAmount() {
        return (long) Integer.MAX_VALUE * this.currentStack.what().getAmountPerUnit();
    }
}
