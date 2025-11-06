package io.github.xiewuzhiying.vs_addition.compats.create.content.decoration.copycat

import com.simibubi.create.AllBlocks
import io.github.xiewuzhiying.vs_addition.VSAdditionMod
import io.github.xiewuzhiying.vs_addition.util.addMass
import io.github.xiewuzhiying.vs_addition.util.getMass
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.VoxelShape
import net.minecraft.world.phys.shapes.CollisionContext
import net.createmod.catnip.lang.LangBuilder
import net.createmod.catnip.lang.Lang
import net.createmod.catnip.lang.LangNumberFormat
import org.valkyrienskies.core.api.ships.ServerShip
import org.valkyrienskies.core.api.ships.Ship

open class CopycatMassHandler(val level: Level, val blockPos: BlockPos, val ship: Ship,
                              val getMaterial: () -> BlockState,
                              val getBlockState: () -> BlockState
    ) {

    private var addedMass = 0.0
    private var oldMaterial : BlockState = AllBlocks.COPYCAT_BASE.defaultState
    private var oldBlockState : BlockState = this.getBlockState()

    fun beforeSetMaterial() {
        oldMaterial = this.getMaterial()
        oldBlockState = this.getBlockState()
    }

    fun afterSetMaterial() {
        if (this.getMaterial() !== this.oldMaterial || this.getBlockState() !== this.oldBlockState) {
            val oldMul = getMultiplier(this.oldBlockState.block.getShape(
                this.getBlockState(),
                this.level,
                this.blockPos, CollisionContext.empty()))

            val mul = getMultiplier(this.getBlockState().block.getShape(
                this.getBlockState(),
                this.level,
                this.blockPos, CollisionContext.empty()))
            this.addedMass =  (getMass(getMaterial()) * mul - getMass(this.oldMaterial) * oldMul)
            if (!this.level.isClientSide()) {
                (this.ship as ServerShip).addMass(this.addedMass, this.blockPos)
            }
        }
    }

    fun onRemove() {
        if (!this.level.isClientSide()) {
            (this.ship as ServerShip).addMass(-this.addedMass, this.blockPos)
        }
    }

    private fun getMultiplier(shape: VoxelShape): Double {
        var mul = 0.0
        for (aabb in shape.toAabbs()) {
            mul += aabb.xsize * aabb.ysize * aabb.zsize
        }
        return mul
    }

    private fun getMass(material: BlockState?): Double {
        if (material == null || material == AllBlocks.COPYCAT_BASE.defaultState) {
            return 0.0
        }
        return material.getMass()
    }

    fun getAddedMass(): Double {
        return addedMass
    }

    companion object {
        @JvmStatic
        @JvmOverloads
        fun getFormattedMassText(tooltip: MutableList<Component>, mass: Double? = null): LangBuilder {
            Lang.builder(VSAdditionMod.MOD_ID).text("附加质量").style(ChatFormatting.WHITE).forGoggles(tooltip)
            val builder = Lang.builder(VSAdditionMod.MOD_ID)
            if (mass == null) {
                builder.text("无附加质量").style(ChatFormatting.DARK_GRAY).forGoggles(tooltip)
            } else {
                builder.text(LangNumberFormat.format(mass)).space().text("kg").style(ChatFormatting.GREEN).forGoggles(tooltip)
            }

            return builder
        }
    }
}