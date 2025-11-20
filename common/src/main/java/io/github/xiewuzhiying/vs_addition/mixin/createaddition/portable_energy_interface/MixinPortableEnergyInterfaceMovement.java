package io.github.xiewuzhiying.vs_addition.mixin.createaddition.portable_energy_interface;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mrh0.createaddition.blocks.portable_energy_interface.PortableEnergyInterfaceBlockEntity;
import com.simibubi.create.content.contraptions.actors.psi.PortableStorageInterfaceBlockEntity;
import com.simibubi.create.content.contraptions.actors.psi.PortableStorageInterfaceMovement;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import io.github.xiewuzhiying.vs_addition.mixinducks.create.portable_interface.IPSIWithShipBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 修复说明：
 * 1. 将 @Mixin 目标从 PortableEnergyInterfaceMovement 改为 PortableStorageInterfaceMovement
 *    （因为 findInterface 方法实际定义在父类中）
 * 2. 将 target 中的类型改为父类 PortableStorageInterfaceBlockEntity
 *    （变量 psi 在字节码中的声明类型是父类）
 * 3. 保持功能：仅在实例为 PortableEnergyInterfaceBlockEntity 且处于飞船模式时跳过通电检测
 *
 * 关键假设：
 * - IPSIWithShipBehavior 接口由 vs_addition 提供，且被 PortableEnergyInterfaceBlockEntity 实现
 * - WorkigMode.WITH_SHIP 枚举值存在（注意拼写 Workig 而非 Working，与原始代码保持一致）
 */
@Pseudo
@Mixin(PortableStorageInterfaceMovement.class)
public abstract class MixinPortableEnergyInterfaceMovement {

    @WrapOperation(
            method = "findInterface", // 目标方法名
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/contraptions/actors/psi/PortableStorageInterfaceBlockEntity;isPowered()Z" // 字节码中实际的调用类型
            )
    )
    private boolean vs_addition$findStationaryInterface(
            PortableStorageInterfaceBlockEntity instance,
            Operation<Boolean> original,
            @Local(ordinal = 0, argsOnly = true) MovementContext context
    ) {
        // 仅对 CreateAdditions 的能量接口应用飞船模式逻辑
        if (instance instanceof PortableEnergyInterfaceBlockEntity energyInterface) {
            // 检查是否启用了飞船行为的特殊模式
            if (energyInterface instanceof IPSIWithShipBehavior behavior
                    && behavior.getWorkingMode().get() == IPSIWithShipBehavior.WorkigMode.WITH_SHIP) {
                // 在飞船模式下跳过 isPowered() 检查，允许接口工作
                return false;
            }
        }
        // 其他情况保持原始行为
        return original.call(instance);
    }
}