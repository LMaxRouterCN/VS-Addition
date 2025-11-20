package io.github.xiewuzhiying.vs_addition.compats.vs_clockwork.behaviour.flap_bearing

import com.mojang.blaze3d.vertex.PoseStack
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock
import net.minecraft.core.Direction
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.phys.Vec3
import net.createmod.catnip.math.VecHelper
import dev.engine_room.flywheel.lib.transform.TransformStack

class FlapBearingLinkFrequencySlotNegative(first: Boolean) : FlapBearingLinkFrequencySlot(first) {
    override fun getLocalOffset(level: LevelAccessor, pos: BlockPos, state: BlockState): Vec3 {
        val facing = state.getValue(DirectionalKineticBlock.FACING)
        var location = VecHelper.voxelSpace(-0.01, 6.0, 5.5)

        if (facing.axis
                .isHorizontal
        ) {
            location = VecHelper.voxelSpace(-0.01, 5.5, 6.0)
            if (isFirst) location = location.add(0.0, (5 / 16f).toDouble(), 0.0)
            return rotateHorizontally(state, location)
        }

        if (isFirst) location = location.add(0.0, 0.0, (5 / 16f).toDouble())
        location =
            VecHelper.rotateCentered(location, (if (facing == Direction.DOWN) 180 else 0).toDouble(), Direction.Axis.X)
        return location
    }

    override fun rotate(level: LevelAccessor, pos: BlockPos, state: BlockState, ms: PoseStack) {
        super.rotate(level, pos, state, ms)
        TransformStack.of(ms).rotateY(-180.0f)
    }
}
