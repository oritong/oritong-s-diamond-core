package com.oritong.oritongsdiamondcore.mixin.pipez;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.compat.FeCompat;
import de.maxhenkel.pipez.blocks.tileentity.PipeLogicTileEntity;
import de.maxhenkel.pipez.blocks.tileentity.PipeTileEntity;
import de.maxhenkel.pipez.blocks.tileentity.types.PipeType;
import de.maxhenkel.pipez.corelib.energy.EnergyUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraftforge.energy.IEnergyStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.ArrayList;
import java.util.List;

@Mixin(targets = "de.maxhenkel.pipez.blocks.tileentity.types.EnergyPipeType", remap = false)
public abstract class EnergyPipeTypeMixin {

    @Overwrite(remap = false)
    protected void insertEqually(PipeLogicTileEntity tile, Direction dir,
                                 List<PipeTileEntity.Connection> connections, IEnergyStorage source) {
        if (connections.isEmpty()) {
            return;
        }

        int rate = oritongsdiamondcore$getRate(tile, dir);
        if (rate <= 0) {
            return;
        }

        int remaining = rate;
        int roundRobinIndex = tile.getRoundRobinIndex(dir, oritongsdiamondcore$self()) % connections.size();
        List<EnergyTarget> targets = oritongsdiamondcore$collectTargets(tile, connections, roundRobinIndex, rate);

        for (EnergyTarget target : targets) {
            if (remaining <= 0) {
                break;
            }

            int fairShare = Math.min(Math.max(rate / targets.size(), 1), remaining);
            int pushed = 0;

            if (target.forgeEnergy() != null) {
                int extractable = source.extractEnergy(fairShare, true);
                if (extractable > 0) {
                    pushed = EnergyUtils.pushEnergy(source, target.forgeEnergy(), extractable);
                }
            }

            if (pushed <= 0 && target.gtEnergy() != null) {
                int gtBudget = Math.min(Math.max(fairShare, oritongsdiamondcore$getMinimumGtPacketFe(target.gtEnergy())),
                        remaining);
                pushed = oritongsdiamondcore$pushForgeEnergyToGt(source, target.gtEnergy(),
                        target.connection().getDirection(), gtBudget);
            }

            if (pushed > 0) {
                remaining -= pushed;
            }
            roundRobinIndex = (target.index() + 1) % connections.size();
        }

        tile.setRoundRobinIndex(dir, oritongsdiamondcore$self(), roundRobinIndex);
    }

    @Overwrite(remap = false)
    protected int receiveEqually(PipeLogicTileEntity tile, Direction dir,
                                 List<PipeTileEntity.Connection> connections, int amount, boolean simulate) {
        if (connections.isEmpty() || amount <= 0 || tile.pushRecursion()) {
            return 0;
        }

        int received = 0;
        int remaining = amount;
        int roundRobinIndex = tile.getRoundRobinIndex(dir, oritongsdiamondcore$self()) % connections.size();
        List<EnergyTarget> targets = oritongsdiamondcore$collectTargets(tile, connections, roundRobinIndex, amount);

        for (EnergyTarget target : targets) {
            if (remaining <= 0) {
                break;
            }

            int fairShare = Math.min(Math.max(amount / targets.size(), 1), remaining);
            int inserted = 0;

            if (target.forgeEnergy() != null) {
                inserted = target.forgeEnergy().receiveEnergy(Math.min(fairShare, amount), simulate);
            }

            if (inserted <= 0 && target.gtEnergy() != null) {
                int gtBudget = Math.min(Math.max(fairShare, oritongsdiamondcore$getMinimumGtPacketFe(target.gtEnergy())),
                        remaining);
                inserted = oritongsdiamondcore$receiveForgeEnergyIntoGt(target.gtEnergy(),
                        target.connection().getDirection(), gtBudget, simulate);
            }

            if (inserted > 0) {
                remaining -= inserted;
                received += inserted;
            }
            roundRobinIndex = (target.index() + 1) % connections.size();
        }

        if (!simulate) {
            tile.setRoundRobinIndex(dir, oritongsdiamondcore$self(), roundRobinIndex);
        }
        tile.popRecursion();
        return received;
    }

    private List<EnergyTarget> oritongsdiamondcore$collectTargets(PipeLogicTileEntity tile,
                                                                  List<PipeTileEntity.Connection> connections,
                                                                  int startIndex, int budgetFe) {
        Level level = tile.getLevel();
        List<EnergyTarget> targets = new ArrayList<>(connections.size());

        for (int i = 0; i < connections.size(); i++) {
            int index = (startIndex + i) % connections.size();
            PipeTileEntity.Connection connection = connections.get(index);
            IEnergyStorage forgeEnergy = connection.getEnergyHandler(level).orElse(null);
            IEnergyContainer gtEnergy = GTCapabilityHelper.getEnergyContainer(level, connection.getPos(),
                    connection.getDirection());

            boolean forgeReceives = oritongsdiamondcore$canReceiveForgeEnergy(forgeEnergy, budgetFe);
            boolean gtReceives = oritongsdiamondcore$canReceiveGtEnergy(gtEnergy, connection.getDirection(), budgetFe);
            if (forgeReceives || gtReceives) {
                targets.add(new EnergyTarget(index, connection, forgeReceives ? forgeEnergy : null,
                        gtReceives ? gtEnergy : null));
            }
        }

        return targets;
    }

    private boolean oritongsdiamondcore$canReceiveForgeEnergy(IEnergyStorage energyStorage, int budgetFe) {
        if (energyStorage == null || !energyStorage.canReceive() || budgetFe <= 0) {
            return false;
        }
        int probe = Math.max(1, Math.min(budgetFe, oritongsdiamondcore$getMinimumFeProbe()));
        return energyStorage.receiveEnergy(probe, true) > 0 || energyStorage.receiveEnergy(budgetFe, true) > 0;
    }

    private boolean oritongsdiamondcore$canReceiveGtEnergy(IEnergyContainer energyContainer, Direction side,
                                                           int budgetFe) {
        if (energyContainer == null || !energyContainer.inputsEnergy(side)) {
            return false;
        }
        long inputVoltage = energyContainer.getInputVoltage();
        return inputVoltage > 0 && energyContainer.getInputAmperage() > 0 &&
                energyContainer.getEnergyCanBeInserted() >= inputVoltage &&
                FeCompat.toEu(budgetFe, oritongsdiamondcore$getFeToEuRatio()) >= inputVoltage;
    }

    private int oritongsdiamondcore$pushForgeEnergyToGt(IEnergyStorage source, IEnergyContainer target,
                                                        Direction side, int maxFe) {
        int extractable = source.extractEnergy(maxFe, true);
        int transferable = oritongsdiamondcore$getGtTransferFe(target, side, extractable);
        if (transferable <= 0) {
            return 0;
        }

        int extracted = source.extractEnergy(transferable, false);
        return oritongsdiamondcore$receiveForgeEnergyIntoGt(target, side, extracted, false);
    }

    private int oritongsdiamondcore$receiveForgeEnergyIntoGt(IEnergyContainer target, Direction side, int maxFe,
                                                             boolean simulate) {
        int acceptedFe = oritongsdiamondcore$getGtTransferFe(target, side, maxFe);
        if (acceptedFe <= 0) {
            return 0;
        }

        if (simulate) {
            return acceptedFe;
        }

        int ratio = oritongsdiamondcore$getFeToEuRatio();
        long voltage = target.getInputVoltage();
        long amperage = FeCompat.toEu(acceptedFe, ratio) / voltage;
        long acceptedAmperage = target.acceptEnergyFromNetwork(side, voltage, amperage);
        return FeCompat.toFeBounded(voltage * acceptedAmperage, ratio, acceptedFe);
    }

    private int oritongsdiamondcore$getGtTransferFe(IEnergyContainer target, Direction side, int maxFe) {
        if (maxFe <= 0 || target == null || !target.inputsEnergy(side)) {
            return 0;
        }

        int ratio = oritongsdiamondcore$getFeToEuRatio();
        int alignedFe = maxFe - maxFe % ratio;
        if (alignedFe <= 0) {
            return 0;
        }

        long voltage = target.getInputVoltage();
        long maxAmperage = target.getInputAmperage();
        if (voltage <= 0 || maxAmperage <= 0) {
            return 0;
        }

        long availableEu = FeCompat.toEu(alignedFe, ratio);
        long amperage = Math.min(maxAmperage,
                Math.min(availableEu / voltage, target.getEnergyCanBeInserted() / voltage));
        if (amperage <= 0) {
            return 0;
        }

        return FeCompat.toFeBounded(voltage * amperage, ratio, alignedFe);
    }

    private int oritongsdiamondcore$getMinimumGtPacketFe(IEnergyContainer energyContainer) {
        return FeCompat.toFeBounded(energyContainer.getInputVoltage(), oritongsdiamondcore$getFeToEuRatio(),
                Integer.MAX_VALUE);
    }

    private int oritongsdiamondcore$getMinimumFeProbe() {
        return Math.max(1, oritongsdiamondcore$getFeToEuRatio());
    }

    private int oritongsdiamondcore$getFeToEuRatio() {
        return Math.max(1, FeCompat.ratio(true));
    }

    private int oritongsdiamondcore$getRate(PipeLogicTileEntity tile, Direction dir) {
        return oritongsdiamondcore$self().getRate(tile, dir);
    }

    private PipeType<?> oritongsdiamondcore$self() {
        return (PipeType<?>) (Object) this;
    }

    private record EnergyTarget(int index, PipeTileEntity.Connection connection, IEnergyStorage forgeEnergy,
                                IEnergyContainer gtEnergy) {
    }
}
