package io.github.xiewuzhiying.vs_addition.forge.compats.create.content.contraptions.actors.psi

import com.simibubi.create.content.contraptions.actors.psi.PortableItemInterfaceBlockEntity
import io.github.xiewuzhiying.vs_addition.VSAdditionConfig
import io.github.xiewuzhiying.vs_addition.compats.create.content.contraptions.actors.psi.PortableStorageInterfaceWithShipController
import io.github.xiewuzhiying.vs_addition.mixinducks.create.portable_interface.IPSIWithShipBehavior
import net.minecraft.world.entity.item.ItemEntity
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.items.IItemHandlerModifiable
import net.minecraftforge.items.ItemStackHandler

open class PortableItemInterfaceWithShipController(be: PortableItemInterfaceBlockEntity) : ForgePortableStorageInterfaceWithShipController(be) {
    var capability: LazyOptional<IItemHandlerModifiable>? = null

    fun createEmptyHandler(behavior: IPSIWithShipBehavior): LazyOptional<IItemHandlerModifiable> {
        return LazyOptional.of { InterfaceItemHandler(ItemStackHandler(0), behavior) }
    }

    override fun startTransferringTo(otherController: PortableStorageInterfaceWithShipController) {
        if (otherController !is PortableItemInterfaceWithShipController || this == otherController || this.other == otherController) {
            return
        }
        val oldCap0 = capability
        val oldCap1 = otherController.capability
        capability = LazyOptional.of { InterfaceItemHandler(ItemStackHandler(VSAdditionConfig.SERVER.create.psi.itemTemp), be as IPSIWithShipBehavior) }
        otherController.capability = capability
        oldCap0?.invalidate()
        oldCap1?.invalidate()

        super.startTransferringTo(otherController)
    }

    override fun stopTransferring() {
        if (!this.isPassive) {
            val level = this.be.level
            val capOptional = this.capability

            // 安全处理 capability，避免 NoSuchElement 异常
            if (capOptional != null && level != null) {
                // 检查 capability 是否仍然有效
                val storage = capOptional.resolve().orElse(null)
                if (storage != null) {
                    val center = getConnectionCenter()
                    for (i in 0 until storage.slots) {
                        val entity = ItemEntity(level, center.x, center.y, center.z, storage.getStackInSlot(i))
                        level.addFreshEntity(entity)
                    }
                }
            }

            // 安全处理 capability 无效化
            var oldCap = capability
            this.capability = createEmptyHandler(be as IPSIWithShipBehavior)
            oldCap?.invalidate()

            // 安全处理 other controller
            val otherController = other as? PortableItemInterfaceWithShipController
            if (otherController != null) {
                oldCap = otherController.capability
                otherController.capability = createEmptyHandler(be as IPSIWithShipBehavior)
                oldCap?.invalidate()
                otherController.isPassive = false
            }
        }
        super.stopTransferring()
    }

    override fun invalidateCapability() {
        capability?.invalidate()
    }
}