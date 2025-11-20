package io.github.xiewuzhiying.vs_addition.mixin.create.redstone.displayLink;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.redstone.displayLink.ClickToLinkBlockItem;
import io.github.xiewuzhiying.vs_addition.util.ConversionUtilsKt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(ClickToLinkBlockItem.class)
public abstract class MixinClickToLinkBlockItem {

    @WrapOperation(
            method = "useOn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/BlockPos;closerThan(Lnet/minecraft/core/Vec3i;D)Z"
            )
    )
    public boolean closerThan(BlockPos instance, Vec3i vec3i, double v, Operation<Boolean> original, @Local Level level) {
        return ConversionUtilsKt.squaredDistanceBetweenInclShips(level, instance, vec3i) < Mth.square(v);
    }
}
