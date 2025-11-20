package io.github.xiewuzhiying.vs_addition.compats.create.content.contraptions.actors.psi

import com.simibubi.create.content.contraptions.actors.psi.PortableStorageInterfaceBlock
import com.simibubi.create.content.contraptions.actors.psi.PortableStorageInterfaceBlockEntity
import com.simibubi.create.foundation.advancement.AllAdvancements
import com.simibubi.create.infrastructure.config.AllConfigs
import io.github.xiewuzhiying.vs_addition.mixinducks.create.portable_interface.IPSIWithShipBehavior
import io.github.xiewuzhiying.vs_addition.util.centerJOMLD
import io.github.xiewuzhiying.vs_addition.util.toVector3d
import net.createmod.catnip.animation.LerpedFloat
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.Mth
import net.minecraft.world.level.Level
import org.joml.Vector3d
import org.joml.primitives.AABBd
import org.joml.primitives.AABBdc
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.valkyrienskies.core.api.ships.ClientShip
import org.valkyrienskies.core.util.toAABBi
import org.valkyrienskies.mod.common.getShipManagingPos
import org.valkyrienskies.mod.common.getShipsIntersecting
import org.valkyrienskies.mod.common.shipObjectWorld
import org.valkyrienskies.mod.common.transformAabbToWorld
import kotlin.math.pow

open class PortableStorageInterfaceWithShipController(open val be: PortableStorageInterfaceBlockEntity) {
    open var other: PortableStorageInterfaceWithShipController? = null
    open var isPassive = false
    open val isConnected
        get() = !be.isRemoved && !be.isPowered && (be as IPSIWithShipBehavior).workingMode.get() == IPSIWithShipBehavior.WorkigMode.WITH_SHIP && other != null && !other!!.be.isRemoved && !other!!.be.isPowered && (other!!.be as IPSIWithShipBehavior).workingMode.get() == IPSIWithShipBehavior.WorkigMode.WITH_SHIP
    open var manager: PortableStorageInterfaceConstraintManager? = null
    open var connectionAnimation: LerpedFloat = LerpedFloat.linear().startWithValue(0.0);
    open var transferTimer = 0
    open var latestOtherPos = Vector3d()
    open var latestDistance = 0.0

    open fun startTransferringTo(otherController: PortableStorageInterfaceWithShipController) {
        if (this.other == otherController) { return }
        otherController.isPassive = true

        this.other = otherController
        otherController.other = this

        startConnecting()
        otherController.startConnecting()

        getConstraintManager()?.createPSIConstraint()

        this.be.notifyUpdate()
    }

    open fun stopTransferring() {
        getConstraintManager()?.removeAllConstraintGroups()
        this.other?.getConstraintManager()?.removeAllConstraintGroups()
        this.other = null
        this.be.level?.updateNeighborsAt(be.blockPos, be.blockState.block)
    }

    open fun invalidateCapability() {

    }

    open fun canTransfer() : Boolean {
        if (!isConnected) {
            stopTransferring();
        }
        return this.other!= null && this.isConnected && this.other!!.isConnected
    }

    open fun tick(ci: CallbackInfo) {
        val be = this.be
        if ((be as IPSIWithShipBehavior).workingMode.get() == IPSIWithShipBehavior.WorkigMode.WITH_SHIP) {
            val wasConnected = isConnected
            if (!isPassive) {
                if (!be.isPowered && this.other == null) {
                    findOtherController()
                }
            }
            if (this.other != null && !this.other!!.isConnected) {
                stopTransferring()
            }
            updateDistance()
            val isConnected = isConnected
            if (isConnected != wasConnected) {
                be.setChanged()
            }
            ci.cancel()
        } else {
            if (this.other != null) {
                stopTransferring()
            }
        }
    }

    open fun getExtensionDistance(partialTicks: Float) : Float {
        val level = be.level ?: return 0f
        val other = this.other

        if (other != null) {
            val pos0 = getConnectionPos(level, this.be)
            val pos1 = getConnectionPos(level, other.be)
            latestDistance = pos0.distance(pos1)
        }

        return (connectionAnimation.getValue(partialTicks).pow(2.0f) * latestDistance / 2).toFloat()
    }

    open fun getConnectionCenter() : Vector3d {
        val level = be.level ?: return Vector3d()
        val other = this.other

        val pos0 = getConnectionPos(level, this.be)
        if (other != null) {
            latestOtherPos.set(getConnectionPos(level, other.be))
        }

        return pos0.lerp(latestOtherPos, 0.5)
    }

    open fun updateDistance() {
        var progress = 0.0
        val timeUnit = getTransferTimeout()
        val animation = PortableStorageInterfaceBlockEntity.ANIMATION
        if (isConnected) {
            progress = 1.0
        } else if (transferTimer >= timeUnit + animation) {
            progress = Mth.lerp((transferTimer - timeUnit - animation) / animation.toFloat(), 1f, 0f).toDouble()
        } else if (transferTimer < animation) {
            progress = Mth.lerp(transferTimer / animation.toFloat(), 0f, 1f).toDouble()
        }
        this.connectionAnimation.setValue(progress)
    }

    open fun findOtherController() {
        val level = this.be.level ?: return

        val blockPos = this.be.blockPos
        val blockState = this.be.blockState
        val ship = level.getShipManagingPos(blockPos)


        val center = blockPos.centerJOMLD
        val aabb = AABBd(1.5, 1.5, 1.5, -1.5, -1.5, -1.5)
        aabb.translate(blockState.getValue(PortableStorageInterfaceBlock.FACING).normal.toVector3d.mul(2.0).add(center))

        val worldAabb = level.transformAabbToWorld(AABBd(aabb))
        val matrices = level.getShipsIntersecting(worldAabb).filterNot { it == ship }.map { it.transform.worldToShip }

        fun findPSI(aabb: AABBdc) : Boolean {
            val aabbi = aabb.toAABBi()
            val mutableBlockPos = BlockPos.MutableBlockPos()
            for (x in aabbi.minX .. aabbi.maxX) {
                for (y in aabbi.minY .. aabbi.maxY) {
                    for (z in aabbi.minZ .. aabbi.maxZ) {
                        mutableBlockPos.set(x, y, z)
                        val entity = level.getBlockEntity(mutableBlockPos)
                        if (entity is PortableStorageInterfaceBlockEntity && !entity.isPowered && entity is IPSIWithShipBehavior && entity.workingMode.get() == IPSIWithShipBehavior.WorkigMode.WITH_SHIP && entity.controller != null && entity.controller::class == this::class) {
                            startTransferringTo(entity.controller)
                            return true
                        }
                    }
                }
            }
            return false
        }

        if (matrices.isNotEmpty()) {
            val tmpAabb = AABBd()
            matrices.forEach { matrix ->
                tmpAabb.set(worldAabb)
                tmpAabb.transform(matrix)
                if (findPSI(tmpAabb)) return
            }
        }

        findPSI(worldAabb)
    }

    open fun getConstraintManager() : PortableStorageInterfaceConstraintManager? {
        if (this.manager == null) {
            this.manager = PortableStorageInterfaceConstraintManager(this, (be.level as? ServerLevel ?: return null).shipObjectWorld)
        }
        return this.manager
    }

    open fun startConnecting() {
        transferTimer = 6 + PortableStorageInterfaceBlockEntity.ANIMATION * 2
    }

    open fun onContentTransferred() {
        val timeUnit = getTransferTimeout()
        transferTimer = timeUnit + PortableStorageInterfaceBlockEntity.ANIMATION
        be.award(AllAdvancements.PSI)
        be.sendData()
    }

    open fun getTransferTimeout() : Int {
        return AllConfigs.server().logistics.psiTimeout.get();
    }

    companion object {
        fun getConnectionPos(level: Level, be: PortableStorageInterfaceBlockEntity) : Vector3d {
            val center = be.blockPos.centerJOMLD
            val ship = level.getShipManagingPos(center)
            val blockState = be.blockState
            val facing = blockState.getValue(PortableStorageInterfaceBlock.FACING)

            val pos = be.blockPos.centerJOMLD.add(toVector3d(facing.step()).mul(0.5))

            (ship as? ClientShip)?.renderTransform?.shipToWorld?.transformPosition(pos)
                ?: ship?.transform?.shipToWorld?.transformPosition(pos)

            return pos
        }
    }
}