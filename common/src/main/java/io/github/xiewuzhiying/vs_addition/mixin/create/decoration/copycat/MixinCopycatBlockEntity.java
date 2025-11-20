package io.github.xiewuzhiying.vs_addition.mixin.create.decoration.copycat;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.decoration.copycat.CopycatBlockEntity;
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
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.List;

@Pseudo
@Restriction(
        require = @Condition(type = Condition.Type.TESTER, tester = CopycatConditionTester.class)
)
@Mixin(CopycatBlockEntity.class)
public abstract class MixinCopycatBlockEntity extends SmartBlockEntity implements CopycatBlockEntityMixinDuck, IHaveGoggleInformation {

    @Shadow public abstract BlockState getMaterial();

    @Unique
    private CopycatMassHandler vs_addition$handler = null;

    @Unique
    private boolean vs_addition$checked = false;

    @Unique
    private BlockState vs_addition$oldMaterial = AllBlocks.COPYCAT_BASE.getDefaultState();

    public MixinCopycatBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(
            method = "setMaterial",
            at = @At("HEAD"),
            remap = false
    )
    private void beforeSetMaterial(BlockState state, CallbackInfo ci, @Share("handler") LocalRef<CopycatMassHandler> handler) {
        handler.set(vs_addition$getOrCreateHandler());
        if (handler.get() != null) {
            handler.get().beforeSetMaterial();
        }
    }

    @Inject(
            method = "setMaterial",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/decoration/copycat/CopycatBlockEntity;notifyUpdate()V"
            )
    )
    private void afterSetMaterial(BlockState blockState, CallbackInfo ci, @Share("handler") LocalRef<CopycatMassHandler> handler) {
        if (handler.get() != null) {
            handler.get().afterSetMaterial();
            sendData();
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
