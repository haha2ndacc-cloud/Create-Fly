package com.zurrtum.create.client.mixin;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem.AutoStorageIndexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.zurrtum.create.client.catnip.gui.render.*;
import com.zurrtum.create.client.foundation.gui.render.*;
import com.zurrtum.create.client.ponder.foundation.render.SceneRenderState;
import com.zurrtum.create.client.ponder.foundation.render.SceneRenderer;
import com.zurrtum.create.client.ponder.foundation.render.TitleTextRenderState;
import com.zurrtum.create.client.ponder.foundation.render.TitleTextRenderer;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GuiRenderer.class)
public class GuiRendererMixin {
    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap;builder()Lcom/google/common/collect/ImmutableMap$Builder;", remap = false))
    private ImmutableMap.Builder<Class<? extends PictureInPictureRenderState>, PictureInPictureRenderer<?>> addRenderer(
        Operation<ImmutableMap.Builder<Class<? extends PictureInPictureRenderState>, PictureInPictureRenderer<?>>> original,
        @Local(argsOnly = true) BufferSource vertexConsumers
    ) {
        ImmutableMap.Builder<Class<? extends PictureInPictureRenderState>, PictureInPictureRenderer<?>> builder = original.call();
        builder.put(ItemTransformRenderState.class, new ItemTransformElementRenderer(vertexConsumers));
        builder.put(BlockTransformRenderState.class, new BlockTransformElementRenderer(vertexConsumers));
        builder.put(EntityBlockRenderState.class, new EntityBlockRenderer(vertexConsumers));
        builder.put(PartialRenderState.class, new PartialElementRenderer(vertexConsumers));
        builder.put(BlazeBurnerRenderState.class, new BlazeBurnerElementRenderer(vertexConsumers));
        builder.put(PressBasinRenderState.class, new PressBasinRenderer(vertexConsumers));
        builder.put(PressRenderState.class, new PressRenderer(vertexConsumers));
        builder.put(MixingBasinRenderState.class, new MixingBasinRenderer(vertexConsumers));
        builder.put(BasinBlazeBurnerRenderState.class, new BasinBlazeBurnerRenderer(vertexConsumers));
        builder.put(MillstoneRenderState.class, new MillstoneRenderer(vertexConsumers));
        builder.put(SawRenderState.class, new SawRenderer(vertexConsumers));
        builder.put(CrushWheelRenderState.class, new CrushWheelRenderer(vertexConsumers));
        builder.put(DeployerRenderState.class, new DeployerRenderer(vertexConsumers));
        builder.put(ManualBlockRenderState.class, new ManualBlockRenderer(vertexConsumers));
        builder.put(SpoutRenderState.class, new SpoutRenderer(vertexConsumers));
        builder.put(CrafterRenderState.class, new CrafterRenderer(vertexConsumers));
        builder.put(DrainRenderState.class, new DrainRenderer(vertexConsumers));
        builder.put(SandPaperRenderState.class, new SandPaperRenderer(vertexConsumers));
        builder.put(TitleTextRenderState.class, new TitleTextRenderer(vertexConsumers));
        builder.put(SceneRenderState.class, new SceneRenderer());
        builder.put(FanRenderState.class, new FanRenderer(vertexConsumers));
        return builder;
    }
}
