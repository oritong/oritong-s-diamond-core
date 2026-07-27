package com.oritong.oritongsdiamondcore.mixin.pipez;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import de.maxhenkel.pipez.blocks.tileentity.PipeLogicTileEntity;
import de.maxhenkel.pipez.blocks.tileentity.PipeTileEntity;
import de.maxhenkel.pipez.blocks.tileentity.types.EnergyPipeType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "de.maxhenkel.pipez.blocks.tileentity.PipeLogicTileEntity", remap = false)
public abstract class PipeLogicTileEntityMixin {

    @Inject(method = "canInsert", at = @At("RETURN"), cancellable = true, remap = false)
    private void oritongsdiamondcore$allowGtEnergyInputs(Level level, PipeTileEntity.Connection connection,
                                                         CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) {
            return;
        }

        PipeLogicTileEntity tile = (PipeLogicTileEntity) (Object) this;
        if (!tile.hasType(EnergyPipeType.INSTANCE)) {
            return;
        }

        IEnergyContainer energyContainer = GTCapabilityHelper.getEnergyContainer(level, connection.getPos(),
                connection.getDirection());
        if (energyContainer != null && energyContainer.inputsEnergy(connection.getDirection()) &&
                energyContainer.getInputVoltage() > 0 && energyContainer.getInputAmperage() > 0) {
            cir.setReturnValue(true);
        }
    }
}
