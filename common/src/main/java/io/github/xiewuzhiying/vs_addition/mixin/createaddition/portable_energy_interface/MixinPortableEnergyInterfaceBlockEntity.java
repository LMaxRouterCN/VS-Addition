package io.github.xiewuzhiying.vs_addition.mixin.createaddition.portable_energy_interface;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.xiewuzhiying.vs_addition.compats.create.content.contraptions.actors.psi.PortableStorageInterfaceWithShipController;
import io.github.xiewuzhiying.vs_addition.mixinducks.create.portable_interface.IPSIWithShipBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

// 1. 移除了对 PortableEnergyInterfaceBlockEntity 的直接 import
// 2. 使用 @Mixin 的 "targets" 属性通过字符串指定目标类，并添加 remap = false
@Pseudo
@Mixin(
        value = {}, // 当使用 targets 时，value 通常为空数组
        targets = "com.mrh0.createaddition.blocks.portable_energy_interface.PortableEnergyInterfaceBlockEntity",
        remap = false // 重要：因为目标类来自另一个模组，不是原版 Minecraft
)
public abstract class MixinPortableEnergyInterfaceBlockEntity {

    @WrapMethod(
            method = "getExtensionDistance",
            remap = false
    )
    private float replace(float partialTicks, Operation<Float> original) {
        // 这里的逻辑是安全的，因为只有当 CCA 存在且 Mixin 成功应用时，这段代码才会执行
        if (this instanceof IPSIWithShipBehavior behavior && behavior.getWorkingMode().get() == IPSIWithShipBehavior.WorkigMode.WITH_SHIP) {
            final PortableStorageInterfaceWithShipController controller = behavior.getController();
            if (controller != null) {
                return controller.getExtensionDistance(partialTicks);
            }
        }
        return original.call(partialTicks);
    }
}
