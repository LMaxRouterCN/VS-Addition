package io.github.xiewuzhiying.vs_addition.forge.mixin.xaeros_minimap.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4d;
import org.joml.Matrix4dc;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import xaero.common.minimap.render.MinimapRenderer;

@Pseudo
@Restriction(
        require = @Condition("xaerominimap")
)
@Mixin(MinimapRenderer.class)
public abstract class MixinMinimapRenderer {

    @Shadow(remap = false) protected Minecraft mc;

    @Unique
    private Matrix4dc vs_addition$prevShipTransform = new Matrix4d();

    @Unique
    private double vs_addition$shipYawOffset = 0;

    @WrapOperation(
            method = "renderMinimap",
            at = @At(
                    value = "INVOKE",
                    target = "Lxaero/common/minimap/render/MinimapRenderer;getRenderAngle(Z)D"
            ),
            require = 0,
            remap = false
    )
    public double modifyAngle1(MinimapRenderer instance, boolean lockedNorth, Operation<Double> original) {
        if (lockedNorth) {
            vs_addition$resetShipData();
            return original.call(instance, true);
        } else {
            ClientShip ship = vs_addition$getMountedShip();
            if (ship != null) {
                vs_addition$updateShipData(ship);
                return original.call(instance, false) + vs_addition$shipYawOffset;
            }
            vs_addition$resetShipData();
            return original.call(instance, false);
        }
    }

    @ModifyExpressionValue(
            method = "renderMinimap",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getViewYRot(F)F"
            ),
            require = 0
    )
    private float modifyAngle2(float original) {
        ClientShip ship = vs_addition$getMountedShip();
        if (ship != null) {
            vs_addition$updateShipData(ship);
            return (float) (original - vs_addition$shipYawOffset);
        }
        vs_addition$resetShipData();
        return original;
    }

    @Unique
    private ClientShip vs_addition$getMountedShip() {
        return (ClientShip) VSGameUtilsKt.getShipMountedTo(mc.gameRenderer.getMainCamera().getEntity());
    }

    @Unique
    private void vs_addition$updateShipData(ClientShip ship) {
        Matrix4d currentTransform = new Matrix4d(ship.getRenderTransform().getShipToWorld());

        if (vs_addition$prevShipTransform != null) {
            // 计算相对变换：prev⁻¹ × current
            Matrix4d relativeTransform = new Matrix4d(vs_addition$prevShipTransform).invert().mul(currentTransform);

            // 从变换矩阵中提取偏航角（Yaw）
            // 使用矩阵的m20和m22元素来计算偏航角
            double yawChange = Math.atan2(-relativeTransform.m20(), relativeTransform.m22());
            vs_addition$shipYawOffset += Math.toDegrees(yawChange);
        }

        vs_addition$prevShipTransform = currentTransform;
    }

    @Unique
    private void vs_addition$resetShipData() {
        vs_addition$prevShipTransform = new Matrix4d();
        vs_addition$shipYawOffset = 0;
    }
}