package io.github.xiewuzhiying.vs_addition.mixin.createaddition.portable_energy_interface;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mrh0.createaddition.blocks.portable_energy_interface.PortableEnergyInterfaceBlockEntity;
import com.mrh0.createaddition.blocks.portable_energy_interface.PortableEnergyInterfaceMovement;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import io.github.xiewuzhiying.vs_addition.mixinducks.create.portable_interface.IPSIWithShipBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(PortableEnergyInterfaceMovement.class)
public abstract class MixinPortableEnergyInterfaceMovement implements MovementBehaviour {
    @WrapOperation(
            method = "findInterface",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mrh0/createaddition/blocks/portable_energy_interface/PortableEnergyInterfaceBlockEntity;isPowered()Z"
            )
    )
    public boolean findStationaryInterface(PortableEnergyInterfaceBlockEntity instance, Operation<Boolean> original, @Local(ordinal = 0, argsOnly = true) MovementContext context) {
        if (instance instanceof IPSIWithShipBehavior behavior && behavior.getWorkingMode().get() == IPSIWithShipBehavior.WorkigMode.WITH_SHIP) {
            return false;
        } else {
            return original.call(instance);
        }
    }
}
