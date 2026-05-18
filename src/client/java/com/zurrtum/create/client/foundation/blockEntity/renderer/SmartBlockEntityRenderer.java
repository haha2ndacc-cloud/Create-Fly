package com.zurrtum.create.client.foundation.blockEntity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.content.redstone.link.LinkRenderer;
import com.zurrtum.create.client.content.redstone.link.LinkRenderer.LinkRenderState;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringRenderer;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringRenderer.FilterRenderState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer.SmartRenderState;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class SmartBlockEntityRenderer<T extends SmartBlockEntity, S extends SmartRenderState> implements BlockEntityRenderer<T, S> {
    protected final ItemModelResolver itemModelManager;

    public SmartBlockEntityRenderer(Context context) {
        itemModelManager = context.itemModelResolver();
    }

    @Override
    @SuppressWarnings("unchecked")
    public S createRenderState() {
        return (S) new SmartRenderState();
    }

    @Nullable
    public static Level extractBase(
        BlockEntity be,
        BlockEntityRenderState state,
        @Nullable CrumblingOverlay breakProgress
    ) {
        state.blockPos = be.getBlockPos();
        state.blockState = be.getBlockState();
        state.blockEntityType = be.getType();
        Level level = be.getLevel();
        state.lightCoords = getLightCoords(level, state.blockPos);
        state.breakProgress = breakProgress;
        return level;
    }

    public static void extractBase(
        @Nullable Level level,
        BlockEntity be,
        BlockEntityRenderState state,
        @Nullable CrumblingOverlay breakProgress
    ) {
        state.blockPos = be.getBlockPos();
        state.blockState = be.getBlockState();
        state.blockEntityType = be.getType();
        state.lightCoords = getLightCoords(level, state.blockPos);
        state.breakProgress = breakProgress;
    }

    @Override
    public void extractRenderState(
        T be,
        S state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        double distance = be.isVirtual() ? -1 : cameraPos.distanceToSqr(VecHelper.getCenterOf(state.blockPos));
        state.filter = FilteringRenderer.getFilterRenderState(be, state.blockState, itemModelManager, distance);
        state.link = LinkRenderer.getLinkRenderState(be, itemModelManager, distance);
    }

    @Override
    public void submit(S state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        if (state.filter != null) {
            state.filter.submit(state.blockState, queue, matrices, state.lightCoords);
        }
        if (state.link != null) {
            state.link.render(state.blockState, queue, matrices, state.lightCoords);
        }
    }

    public static int getLightCoords(@Nullable Level level, BlockPos pos) {
        return level != null ? LightCoordsUtil.getLightCoords(level, pos) : LightCoordsUtil.FULL_BRIGHT;
    }

    @Nullable
    public static CardinalLighting getCardinalLighting(@Nullable Level level) {
        return level instanceof BlockAndTintGetter getter ? getter.cardinalLighting() : null;
    }

    public static Vec3 createNudge(int seed) {
        long randomBits = seed * 31L * 493286711L;
        randomBits = randomBits * randomBits * 4392167121L + randomBits * 98761L;
        float xNudge = (((randomBits >> 16 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        float yNudge = (((randomBits >> 20 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        float zNudge = (((randomBits >> 24 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        return new Vec3(xNudge, yNudge, zNudge);
    }

    @Nullable
    public static NameplateRenderState getNameplateRenderState(
        SmartBlockEntity blockEntity,
        BlockPos pos,
        Vec3 cameraPos,
        Component tag,
        float yOffset,
        int light
    ) {
        if (blockEntity.isVirtual()) {
            return null;
        }
        double distance = cameraPos.distanceToSqr(Vec3.atCenterOf(pos));
        if (distance > 4096.0f) {
            return null;
        }
        HitResult hitResult = Minecraft.getInstance().hitResult;
        if (!(hitResult instanceof BlockHitResult bhr) || bhr.getType() == Type.MISS || !bhr.getBlockPos()
            .equals(pos)) {
            return null;
        }
        Vec3 labelPos = new Vec3(0.5, yOffset - 0.25, 0.5);
        return new NameplateRenderState(labelPos, tag, light);
    }

    public static class SmartRenderState extends BlockEntityRenderState {
        public @Nullable FilterRenderState filter;
        public @Nullable LinkRenderState link;
    }

    public record NameplateRenderState(Vec3 pos, Component label, int light) {
        public void submit(PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
            queue.submitNameTag(matrices, pos, 0, label, true, light, cameraState);
        }
    }
}
