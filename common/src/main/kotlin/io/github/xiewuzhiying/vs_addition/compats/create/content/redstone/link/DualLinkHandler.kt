package io.github.xiewuzhiying.vs_addition.compats.create.content.redstone.link

import com.simibubi.create.AllItems
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour
import com.simibubi.create.foundation.utility.RaycastHelper
import dev.architectury.event.EventResult
import io.github.xiewuzhiying.vs_addition.PlatformUtils
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3

object DualLinkHandler {
    @JvmStatic
    fun handler(player: Player, hand: InteractionHand, pos: BlockPos, face: Direction) : EventResult {
        val world = player.level()

        if (player.isShiftKeyDown || player.isSpectator) return EventResult.pass()

        val behaviour = BlockEntityBehaviour.get(world, pos, DualLinkBehaviour.TYPE)
            ?: return EventResult.pass()

        val heldItem = player.getItemInHand(hand)
        val ray = RaycastHelper.rayTraceRange(world, player, 10.0) ?: return EventResult.pass()
        if (AllItems.LINKED_CONTROLLER.isIn(heldItem)) return EventResult.pass()
        if (AllItems.WRENCH.isIn(heldItem)) return EventResult.pass()

        val fakePlayer = PlatformUtils.isFakePlayer(player)
        var fakePlayerChoice = false

        if (fakePlayer) {
            val blockState = world.getBlockState(pos)
            val localHit = ray.location
                .subtract(Vec3.atLowerCornerOf(pos))
                .add(Vec3.atLowerCornerOf(ray.direction.normal).scale(.25))
            fakePlayerChoice = localHit.distanceToSqr(behaviour.firstSlot.getLocalOffset(world, pos, blockState)) > localHit
                .distanceToSqr(behaviour.secondSlot.getLocalOffset(world, pos, blockState))
        }

        for (first in mutableListOf(false, true)) {
            if (behaviour.testHit(first, ray.location) || fakePlayer && fakePlayerChoice == first) {
                if (!world.isClientSide()) behaviour.setFrequency(first, heldItem)
                world.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, .25f, .1f)
                return EventResult.interruptTrue()
            }
        }

        return EventResult.pass()
    }
}