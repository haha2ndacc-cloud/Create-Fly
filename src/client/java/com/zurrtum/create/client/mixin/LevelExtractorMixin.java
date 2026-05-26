package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.zurrtum.create.client.catnip.render.EntityBlockLayer;
import com.zurrtum.create.client.catnip.render.EntityBlockLightLayer;
import com.zurrtum.create.client.catnip.render.EntityBlockMultipleLayer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.backend.engine.uniform.LevelInfoHolder;
import com.zurrtum.create.client.flywheel.impl.FlwImplXplat;
import com.zurrtum.create.client.flywheel.impl.event.RenderContextHolder;
import com.zurrtum.create.client.flywheel.lib.visualization.VisualizationHelper;
import com.zurrtum.create.client.infrastructure.render.BreakingRenderStateInfo;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.state.level.BlockBreakingRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.Set;

import static com.zurrtum.create.client.infrastructure.model.WrapperBlockStateModel.getBlockDestroyModel;

@Mixin(LevelExtractor.class)
public class LevelExtractorMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    private @Nullable ClientLevel level;

    @Shadow
    @Final
    private LevelRenderer levelRenderer;

    @Shadow
    @Final
    private LevelRenderState levelRenderState;

    @Inject(method = "extract(Lnet/minecraft/client/DeltaTracker;Lnet/minecraft/client/Camera;F)V", at = @At("TAIL"))
    private void flywheel$updateContext(
        DeltaTracker deltaTracker,
        Camera camera,
        float deltaPartialTick,
        CallbackInfo ci
    ) {
        ((RenderContextHolder) levelRenderer).flywheel$updateRenderContext(level, deltaPartialTick);
        ((LevelInfoHolder) levelRenderState).flywheel$update(level, deltaPartialTick);
    }

    /**
     * This gets called when a block is marked for rerender by vanilla.
     */
    @Inject(method = "setBlockDirty(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;)V", at = @At("TAIL"))
    private void flywheel$checkUpdate(BlockPos pos, BlockState oldState, BlockState newState, CallbackInfo ci) {
        VisualizationManager manager = VisualizationManager.get(level);
        if (manager == null) {
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return;
        }

        var blockEntities = manager.blockEntities();
        if (oldState != newState) {
            blockEntities.queueRemove(blockEntity);
            blockEntities.queueAdd(blockEntity);
        } else {
            // I don't think this is possible to reach in vanilla
            blockEntities.queueUpdate(blockEntity);
        }
    }

    @Inject(method = "allChanged()V", at = @At("RETURN"))
    private void flywheel$reload(CallbackInfo ci) {
        if (level != null) {
            FlwImplXplat.INSTANCE.dispatchReloadLevelRendererEvent(level);
            EntityBlockLightLayer.clear();
            EntityBlockLayer.clear();
            EntityBlockMultipleLayer.clear();
        }
    }

    @WrapOperation(method = "extractVisibleEntities(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/culling/Frustum;Lnet/minecraft/client/DeltaTracker;Lnet/minecraft/client/renderer/state/level/LevelRenderState;)V", at = @At(value = "INVOKE", target = "Ljava/lang/Iterable;iterator()Ljava/util/Iterator;", remap = false))
    private Iterator<Entity> flywheel$decideNotToRenderEntity(
        Iterable<Entity> instance,
        Operation<Iterator<Entity>> original
    ) {
        return VisualizationHelper.skipEntityVanillaRender(level, original.call(instance));
    }

    @WrapOperation(method = "extractVisibleBlockEntities(Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/state/level/LevelRenderState;)V", at = @At(value = "INVOKE", target = "Ljava/util/Set;iterator()Ljava/util/Iterator;"))
    private Iterator<BlockEntity> flywheel$decideNotToRenderBlockEntity(
        Set<BlockEntity> instance,
        Operation<Iterator<BlockEntity>> original
    ) {
        return VisualizationHelper.skipBlockEntityVanillaRender(level, original.call(instance));
    }

    @Inject(method = "extractBlockDestroyAnimation(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/state/level/LevelRenderState;)V", at = @At("HEAD"))
    private void init(
        Camera camera,
        LevelRenderState levelRenderState,
        CallbackInfo ci,
        @Share("models") LocalRef<BlockStateModelSet> ref
    ) {
        ref.set(minecraft.getModelManager().getBlockStateModelSet());
    }

    @ModifyArg(method = "extractBlockDestroyAnimation(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/state/level/LevelRenderState;)V", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
    private <E> E addInfo(E e, @Share("models") LocalRef<BlockStateModelSet> ref) {
        BlockBreakingRenderState state = (BlockBreakingRenderState) e;
        BlockState blockState = state.blockState();
        ((BreakingRenderStateInfo) e).create$setRenderModel(getBlockDestroyModel(
            ref.get().get(blockState),
            level,
            state.blockPos(),
            blockState
        ));
        return e;
    }
}
