// 你可能在找common/src/main/java/io/github/xiewuzhiying/vs_addition/mixin/xaeros_minimap/client/MixinMinimapRenderer.java
//
//package io.github.xiewuzhiying.vs_addition.forge.mixin.xaeros_minimap.client;
//
//import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
//import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
//import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
//import me.fallenbreath.conditionalmixin.api.annotation.Condition;
//import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
//import net.minecraft.client.Minecraft;
//import org.jetbrains.annotations.Nullable;
//import org.joml.Matrix4d;
//import org.joml.Matrix4dc;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Pseudo;
//import org.spongepowered.asm.mixin.Shadow;
//import org.spongepowered.asm.mixin.Unique;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//import org.valkyrienskies.core.api.ships.ClientShip;
//import org.valkyrienskies.mod.common.VSGameUtilsKt;
//import xaero.common.minimap.render.MinimapRenderer;
//
//@Pseudo
//@Restriction(require = @Condition("xaerominimap"))
//@Mixin(MinimapRenderer.class)
//public abstract class MixinMinimapRenderer {
//
//    @Shadow
//    protected Minecraft mc;
//
//    /** null 表示尚未初始化 prev transform（首次只记录，不计算偏移） */
//    @Unique
//    private @Nullable Matrix4dc vs_addition$prevShipTransform = null;
//
//    /** 以度为单位的累计偏航补偿（累积相对变换带来的偏航） */
//    @Unique
//    private double vs_addition$shipYawOffset = 0.0;
//
//    /**
//     * 修正 getRenderAngle 方法 - 用于?底图渲染
//     */
//    @WrapOperation(
//            method = "renderMinimap",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lxaero/common/minimap/render/MinimapRenderer;getRenderAngle(Z)D"
//            ),
//            require = 0,
//            remap = false
//    )
//    private double vs_addition$modifyRenderAngle(MinimapRenderer instance, boolean lockedNorth, Operation<Double> original) {
//        ClientShip ship = vs_addition$getMountedShip();
//        if (lockedNorth) {
//            vs_addition$resetShipData();
//            return original.call(instance, true);
//        }
//        if (ship != null) {
//            vs_addition$updateShipData(ship);
//            // 底图修正角度
//            double angle = original.call(instance, false) - vs_addition$shipYawOffset;
//            double normalizedAngle = vs_addition$normalizeAngle(angle);
//
//            // 调试输出：打印角度
//            //System.out.println("底图角度修正: 原始角度: " + angle + ", 修正后角度: " + normalizedAngle);
//            //return normalizedAngle;
//        }
//        return original.call(instance, false);
//    }
//
//    /**
//     * 修正 getActualAngle 方法 - 用于?实体和方向指示器渲染
//     */
//    @Inject(
//            method = "getActualAngle",
//            at = @At("RETURN"),
//            cancellable = true,
//            remap = false
//    )
//    private void vs_addition$modifyActualAngle(CallbackInfoReturnable<Double> cir) {
//        ClientShip ship = vs_addition$getMountedShip();
//        if (ship != null) {
//            double originalAngle = cir.getReturnValueD();
//            // 实体和方向指示器也需要相同的角度修正，但防止重复累积
//            double newAngle = originalAngle - vs_addition$shipYawOffset;
//            double normalizedAngle = vs_addition$normalizeAngle(newAngle);
//            cir.setReturnValue(normalizedAngle);
//
//            // 调试输出：打印角度
//            //System.out.println("实体角度修正: 原始角度: " + originalAngle + ", 修正后角度: " + normalizedAngle);
//        }
//    }
//
//    /**
//     * 修正玩家箭头角度计算
//     */
//    @ModifyExpressionValue(
//            method = "renderMinimap",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lnet/minecraft/world/entity/Entity;getViewYRot(F)F"
//            ),
//            remap = true
//    )
//    private float vs_addition$modifyArrowAngle(float originalAngle) {
//        ClientShip ship = vs_addition$getMountedShip();
//        if (ship != null) {
//            float newAngle = (float) (originalAngle - vs_addition$shipYawOffset);
//            float normalizedAngle = (float) vs_addition$normalizeAngle(newAngle);
//
//            // 调试输出：打印角度
//            //System.out.println("箭头角度修正: 原始角度: " + originalAngle + ", 修正后角度: " + normalizedAngle);
//            //return normalizedAngle;
//        }
//        return originalAngle;
//    }
//
//    @Unique
//    private ClientShip vs_addition$getMountedShip() {
//        return (ClientShip) VSGameUtilsKt.getShipMountedTo(mc.gameRenderer.getMainCamera().getEntity());
//    }
//
//    @Unique
//    private void vs_addition$updateShipData(ClientShip ship) {
//        Matrix4d currentTransform = new Matrix4d(ship.getRenderTransform().getShipToWorld());
//        // 首帧：未初始化 prev => 仅记录，不计算偏移
//        if (vs_addition$prevShipTransform == null) {
//            vs_addition$prevShipTransform = new Matrix4d(currentTransform);
//            return;
//        }
//
//        // 计算相对变换：prev⁻¹ × current
//        Matrix4d relativeTransform = new Matrix4d(vs_addition$prevShipTransform).invert().mul(currentTransform);
//
//        // 从相对变换矩阵提取偏航增量（Yaw）
//        double yawChangeRad = Math.atan2(-relativeTransform.m20(), relativeTransform.m22());
//        double yawChangeDeg = Math.toDegrees(yawChangeRad);
//
//        // 累积相对偏航（度）
//        vs_addition$shipYawOffset += yawChangeDeg;
//
//        // 防止数值溢出导致漂移
//        vs_addition$shipYawOffset = vs_addition$normalizeAngle(vs_addition$shipYawOffset);
//
//        // 更新 prev 矩阵
//        vs_addition$prevShipTransform = new Matrix4d(currentTransform);
//
//        // 调试输出：打印旋转增量
//        //System.out.println("增量偏航: " + yawChangeDeg + "°");
//    }
//
//    @Unique
//    private void vs_addition$resetShipData() {
//        vs_addition$prevShipTransform = null;
//        vs_addition$shipYawOffset = 0.0;
//    }
//
//    /**
//     * 归一化角度，保证在 (-180, 180] 区间
//     */
//    @Unique
//    private double vs_addition$normalizeAngle(double angle) {
//        angle %= 360.0;
//        if (angle > 180.0) angle -= 360.0;
//        if (angle <= -180.0) angle += 360.0;
//        return angle;
//    }
//}
