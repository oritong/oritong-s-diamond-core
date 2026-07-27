package com.oritong.oritongsdiamondcore.mixin.gtmthings;

import com.gregtechceu.gtceu.api.capability.compat.FeCompat;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import net.minecraftforge.energy.IEnergyStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "com.hepdd.gtmthings.common.block.machine.electric.WirelessEnergyInterface", remap = false)
public abstract class WirelessEnergyInterfaceMixin implements IEnergyStorage {

    @Shadow(remap = false)
    @Final
    public NotifiableEnergyContainer energyContainer;

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (maxReceive <= 0) {
            return 0;
        }

        int ratio = Math.max(1, FeCompat.ratio(true));
        int alignedReceive = maxReceive - maxReceive % ratio;
        if (alignedReceive <= 0) {
            return 0;
        }

        long euToInsert = Math.min(FeCompat.toEu(alignedReceive, ratio), energyContainer.getEnergyCanBeInserted());
        if (euToInsert <= 0) {
            return 0;
        }

        if (!simulate) {
            euToInsert = energyContainer.changeEnergy(euToInsert);
        }
        return oritongsdiamondcore$toFeSaturated(euToInsert, ratio, alignedReceive);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return oritongsdiamondcore$toFeSaturated(energyContainer.getEnergyStored(), Math.max(1, FeCompat.ratio(true)),
                Integer.MAX_VALUE);
    }

    @Override
    public int getMaxEnergyStored() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return true;
    }

    private int oritongsdiamondcore$toFeSaturated(long eu, int ratio, int maxFe) {
        if (eu <= 0L || ratio <= 0) {
            return 0;
        }
        long limitEu = (long) maxFe / ratio;
        if (eu >= limitEu) {
            return maxFe;
        }
        return (int) (eu * ratio);
    }
}
