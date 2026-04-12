package com.zurrtum.create.client.foundation.blockEntity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.content.redstone.link.LinkRenderer;
import com.zurrtum.create.client.content.redstone.link.LinkRenderer.LinkRenderState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.LinkBlockEntityRenderer.LinkBlockEntityRenderState;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

public class LinkBlockEntityRenderer implements BlockEntityRenderer<SmartBlockEntity, LinkBlockEntityRenderState> {
    protected final ItemModelResolver itemModelManager;

    public LinkBlockEntityRenderer(Context context) {
        itemModelManager = context.itemModelResolver();
    }

    @Override
    public LinkBlockEntityRenderState createRenderState() {
        return new LinkBlockEntityRenderState();
    }

    @Override
    public void extractRenderState(
        SmartBlockEntity be,
        LinkBlockEntityRenderState state,
        float partialTicks,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay breakProgress
    ) {
        BlockPos blockPos = be.getBlockPos();
        state.link = LinkRenderer.getLinkRenderState(
            be,
            itemModelManager,
            be.isVirtual() ? -1 : cameraPos.distanceToSqr(VecHelper.getCenterOf(blockPos))
        );
        if (state.link != null) {
            state.blockPos = blockPos;
            state.blockState = be.getBlockState();
            state.lightCoords = SmartBlockEntityRenderer.getLightCoords(be.getLevel(), blockPos);
            state.blockEntityType = be.getType();
        }
    }

    @Override
    public void submit(
        LinkBlockEntityRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState camera
    ) {
        state.link.render(state.blockState, queue, matrices, state.lightCoords);
    }

    public static class LinkBlockEntityRenderState extends BlockEntityRenderState {
        public @UnknownNullability LinkRenderState link;
    }
}
