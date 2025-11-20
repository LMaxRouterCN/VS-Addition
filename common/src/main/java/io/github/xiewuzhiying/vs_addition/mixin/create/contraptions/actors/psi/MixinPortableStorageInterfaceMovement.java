package io.github.xiewuzhiying.vs_addition.mixin.create.contraptions.actors.psi;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.contraptions.actors.psi.PortableStorageInterfaceBlockEntity;
import com.simibubi.create.content.contraptions.actors.psi.PortableStorageInterfaceMovement;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import io.github.xiewuzhiying.vs_addition.mixinducks.create.portable_interface.IPSIWithShipBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PortableStorageInterfaceMovement.class)
public abstract class MixinPortableStorageInterfaceMovement implements MovementBehaviour {

    @WrapOperation(
            method = "findInterface",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/contraptions/actors/psi/PortableStorageInterfaceBlockEntity;isPowered()Z"
            )
    )
    public boolean findStationaryInterface(PortableStorageInterfaceBlockEntity instance, Operation<Boolean> original, @Local(ordinal = 0, argsOnly = true) MovementContext context) {
        if (instance instanceof IPSIWithShipBehavior behavior && behavior.getWorkingMode().get() == IPSIWithShipBehavior.WorkigMode.WITH_SHIP) {
            return false;
        } else {
            return original.call(instance);
        }
    }

}
