package io.github.xiewuzhiying.vs_addition.mixin.create.contraptions.actors.psi;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.simibubi.create.content.contraptions.DirectionalExtenderScrollOptionSlot;
import com.simibubi.create.content.contraptions.actors.psi.PortableStorageInterfaceBlock;
import com.simibubi.create.content.contraptions.actors.psi.PortableStorageInterfaceBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import io.github.xiewuzhiying.vs_addition.compats.create.content.contraptions.actors.psi.PortableStorageInterfaceConstraintManager;
import io.github.xiewuzhiying.vs_addition.compats.create.content.contraptions.actors.psi.PortableStorageInterfaceWithShipController;
import io.github.xiewuzhiying.vs_addition.mixinducks.create.portable_interface.IPSIWithShipBehavior;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PortableStorageInterfaceBlockEntity.class)
public abstract class MixinPortableStorageInterfaceBlockEntity implements IPSIWithShipBehavior {

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/foundation/blockEntity/SmartBlockEntity;tick()V",
                    shift = At.Shift.AFTER
            ),
            cancellable = true,
            remap = false
    )
    private void onTick(CallbackInfo ci) {
        final PortableStorageInterfaceWithShipController controller = this.getController();
        if (controller == null) { return; }
        controller.tick(ci);
    }

    @WrapMethod(
            method = "getExtensionDistance",
            remap = false
    )
    private float replace(float partialTicks, Operation<Float> original) {
        if (this.getWorkingMode().get() == WorkigMode.WITH_SHIP) {
            final PortableStorageInterfaceWithShipController controller = this.getController();
            if (controller != null) {
                return controller.getExtensionDistance(partialTicks);
            }
        }
        return original.call(partialTicks);
    }

    @Inject(
            method = "write",
            at = @At("TAIL"),
            remap = false
    )
    private void write(CompoundTag tag, boolean clientPacket, CallbackInfo ci) {
        final PortableStorageInterfaceWithShipController controller = this.getController();
        if (controller == null) { return; }
        final PortableStorageInterfaceConstraintManager manager = this.getController().getConstraintManager();
        if (manager == null) { return; }
        manager.writeCompoundTag(tag);
    }

    @Inject(
            method = "read",
            at = @At("TAIL"),
            remap = false
    )
    private void read(CompoundTag tag, boolean clientPacket, CallbackInfo ci) {
        final PortableStorageInterfaceWithShipController controller = this.getController();
        if (controller == null) { return; }
        final PortableStorageInterfaceConstraintManager manager = this.getController().getConstraintManager();
        if (manager == null) { return; }
        manager.readCompoundTag(tag);
    }

    @Unique
    private ScrollOptionBehaviour<WorkigMode> vs_addition$workingMode;

    @Inject(
            method = "addBehaviours",
            at = @At("RETURN"),
            remap = false
    )
    public void behaviour(List<BlockEntityBehaviour> behaviours, CallbackInfo ci) {
        this.vs_addition$workingMode = new ScrollOptionBehaviour<>(IPSIWithShipBehavior.WorkigMode.class, CreateLang.translateDirect("vs_addition.working_mode"), (PortableStorageInterfaceBlockEntity)(Object) this, vs_addition$getMovementModeSlot());
        behaviours.add(this.vs_addition$workingMode);
    }

    @Unique
    private ValueBoxTransform vs_addition$getMovementModeSlot() {
        return new DirectionalExtenderScrollOptionSlot((state, d) -> {
            Direction.Axis axis = d.getAxis();
            Direction.Axis bearingAxis = state.getValue(PortableStorageInterfaceBlock.FACING)
                    .getAxis();
            return bearingAxis != axis;
        });
    }

    @Override
    public ScrollOptionBehaviour<IPSIWithShipBehavior.WorkigMode> getWorkingMode() {
        return vs_addition$workingMode;
    }

}
