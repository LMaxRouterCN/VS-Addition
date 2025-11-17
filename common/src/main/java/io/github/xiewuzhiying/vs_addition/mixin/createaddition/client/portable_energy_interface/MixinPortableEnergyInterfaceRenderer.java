package io.github.xiewuzhiying.vs_addition.mixin.createaddition.client.portable_energy_interface;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mrh0.createaddition.blocks.portable_energy_interface.PortableEnergyInterfaceBlockEntity;
import com.mrh0.createaddition.blocks.portable_energy_interface.PortableEnergyInterfaceRenderer;
import io.github.xiewuzhiying.vs_addition.compats.create.content.contraptions.actors.psi.PortableStorageInterfaceWithShipController;
import io.github.xiewuzhiying.vs_addition.mixinducks.create.portable_interface.IPSIWithShipBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 修复说明：
 * 1. 移除完整方法签名，仅保留方法名 "renderSafe"
 *    - 避免因重映射或参数类型变化导致的匹配失败
 * 2. 添加 require = 0
 *    - 使注入失败时不阻止游戏启动（关键！）
 * 3. 保持所有功能逻辑不变
 *
 * 关键假设：
 * - PortableEnergyInterfaceRenderer 或其父类中存在 renderSafe 方法
 * - 该方法在调用 instance.isConnected()
 * - IPSIWithShipBehavior 接口被正确实现
 */
@Pseudo
@Mixin(PortableEnergyInterfaceRenderer.class)
public abstract class MixinPortableEnergyInterfaceRenderer {

    @WrapOperation(
            method = "renderSafe", // 仅使用方法名，不指定完整签名
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mrh0/createaddition/blocks/portable_energy_interface/PortableEnergyInterfaceBlockEntity;isConnected()Z",
                    remap = false
            ),
            require = 0 // 关键：使注入可选，失败时不崩溃
    )
    private boolean vs_addition$wrapIsConnected(
            PortableEnergyInterfaceBlockEntity instance,
            Operation<Boolean> original
    ) {
        // 飞船模式特殊逻辑
        if (instance instanceof IPSIWithShipBehavior behavior
                && behavior.getWorkingMode().get() == IPSIWithShipBehavior.WorkigMode.WITH_SHIP) {
            final PortableStorageInterfaceWithShipController controller = behavior.getController();
            if (controller != null) {
                return controller.isConnected();
            }
        }

        // 保持原始行为
        return original.call(instance);
    }
}