package io.github.xiewuzhiying.vs_addition.compats.vs_clockwork.behaviour.flap_bearing

import com.mojang.blaze3d.vertex.PoseStack
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform
import net.minecraft.core.Direction
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.phys.Vec3
import net.createmod.catnip.math.VecHelper
import net.createmod.catnip.math.AngleHelper
import dev.engine_room.flywheel.lib.transform.TransformStack

open class FlapBearingLinkFrequencySlot(first: Boolean) : ValueBoxTransform.Dual(first) {
    override fun getLocalOffset(level: LevelAccessor, pos: BlockPos, state: BlockState): Vec3 {
        val facing = state.getValue(DirectionalKineticBlock.FACING)
        var location = VecHelper.voxelSpace(16.01, 6.0, 5.5)

        if (facing.axis
                .isHorizontal
        ) {
            location = VecHelper.voxelSpace(16.01, 5.5, 6.0)
            if (isFirst) location = location.add(0.0, (5 / 16f).toDouble(), 0.0)
            return rotateHorizontally(state, location)
        }

        if (isFirst) location = location.add(0.0, 0.0, (5 / 16f).toDouble())
        location =
            VecHelper.rotateCentered(location, (if (facing == Direction.DOWN) 180 else 0).toDouble(), Direction.Axis.X)
        return location
    }

    override fun rotate(level: LevelAccessor, pos: BlockPos, state: BlockState, ms: PoseStack) {
        val facing = state.getValue(DirectionalKineticBlock.FACING)
        val xRot: Float
        val yRot: Float
        if (facing.axis.isVertical) {
            yRot = 270f
            xRot = AngleHelper.verticalAngle(facing)
        } else {
            yRot = AngleHelper.horizontalAngle(facing) + 270
            xRot = 0f
        }
        TransformStack.of(ms)
            .rotateX(xRot.toFloat())
            .rotateY(yRot.toFloat())
    }

    override fun getScale(): Float {
        return .4975f
    }
}