package com.oritong.oritongsdiamondcore.mixin.extendedae;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKey;
import com.glodblock.github.extendedae.common.items.InfinityCell;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(InfinityCell.class)
public class MixinInfinityCell {

    @Overwrite(remap = false)
    public static long getAsIntMax(AEKey key) {
        if (key instanceof AEFluidKey) {
            return Long.MAX_VALUE / AEFluidKey.AMOUNT_BUCKET * AEFluidKey.AMOUNT_BUCKET;
        }
        return Long.MAX_VALUE;
    }
}
