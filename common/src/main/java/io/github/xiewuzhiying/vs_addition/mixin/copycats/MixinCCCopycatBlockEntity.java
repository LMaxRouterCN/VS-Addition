package io.github.xiewuzhiying.vs_addition.mixin.copycats;

import com.copycatsplus.copycats.foundation.copycat.CCCopycatBlockEntity;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import io.github.xiewuzhiying.vs_addition.compats.create.content.decoration.copycat.CopycatMassHandler;
import io.github.xiewuzhiying.vs_addition.mixinducks.create.copycat.CopycatBlockEntityMixinDuck;
import io.github.xiewuzhiying.vs_addition.context.conditiontester.CopycatConditionTester;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.List;

@Mixin(CCCopycatBlockEntity.class)
@Restriction(
        require = @Condition(type = Condition.Type.TESTER, tester = CopycatConditionTester.class)
)
public abstract class MixinCCCopycatBlockEntity extends SmartBlockEntity implements CopycatBlockEntityMixinDuck, IHaveGoggleInformation {

    @Shadow
    public abstract BlockState getMaterial();

    @Shadow public abstract BlockPos getBlockPos();

    @Shadow public abstract Level getLevel();

    @Shadow public abstract BlockState getBlockState();

    @Unique
    private CopycatMassHandler vs_addition$handler = null;

    @Unique boolean vs_addition$checked = false;

    public MixinCCCopycatBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(
            method = "setMaterialInternal",
            at = @At("HEAD"),
            remap = false
    )
    private void beforeSetMaterial(BlockState material, CallbackInfo ci, @Share("handler") LocalRef<CopycatMassHandler> handler) {
        handler.set(vs_addition$getOrCreateHandler());
        if (handler.get() != null) {
            handler.get().beforeSetMaterial();
        }
    }

    @Inject(
            method = "setMaterialInternal",
            at = @At("HEAD"),
            remap = false
    )
    private void afterSetMaterial(BlockState material, CallbackInfo ci, @Share("handler") LocalRef<CopycatMassHandler> handler) {
        handler.set(vs_addition$getOrCreateHandler());
        if (handler.get() != null) {
            handler.get().afterSetMaterial();
        }
    }

    @Override
    public CopycatMassHandler vs_addition$getCopycatMassHandler() {
        return vs_addition$handler;
    }

    @Unique
    private CopycatMassHandler vs_addition$getOrCreateHandler() {
        if (this.vs_addition$checked) {
            return this.vs_addition$handler;
        } else {
            this.vs_addition$checked = true;
            if (this.getLevel() != null && !this.getLevel().isClientSide()) {
                final ServerLevel serverLevel = (ServerLevel) this.getLevel();
                final ServerShip ship = VSGameUtilsKt.getShipManagingPos(serverLevel, this.getBlockPos());
                if (ship != null) {
                    this.vs_addition$handler = new CopycatMassHandler(serverLevel, this.getBlockPos(), ship, this::getMaterial, this::getBlockState);
                    return this.vs_addition$handler;
                }
            }
            return null;
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (isPlayerSneaking) {
            final CopycatMassHandler handler = this.vs_addition$getOrCreateHandler();
            CopycatMassHandler.getFormattedMassText(tooltip, handler != null ? handler.getAddedMass() : null);
        }
        return true;
    }
}
