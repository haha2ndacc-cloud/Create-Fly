package com.zurrtum.create.client.mixin;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.client.catnip.gui.render.*;
import com.zurrtum.create.client.foundation.gui.render.*;
import com.zurrtum.create.client.ponder.foundation.render.SceneRenderState;
import com.zurrtum.create.client.ponder.foundation.render.SceneRenderer;
import com.zurrtum.create.client.ponder.foundation.render.TitleTextRenderState;
import com.zurrtum.create.client.ponder.foundation.render.TitleTextRenderer;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GuiRenderer.class)
public class GuiRendererMixin {
    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap;builder()Lcom/google/common/collect/ImmutableMap$Builder;", remap = false))
    private ImmutableMap.Builder<Class<? extends PictureInPictureRenderState>, PictureInPictureRenderer<?>> addRenderer(
        Operation<ImmutableMap.Builder<Class<? extends PictureInPictureRenderState>, PictureInPictureRenderer<?>>> original
    ) {
        ImmutableMap.Builder<Class<? extends PictureInPictureRenderState>, PictureInPictureRenderer<?>> builder = original.call();
        builder.put(ItemTransformRenderState.class, new ItemTransformElementRenderer());
        builder.put(BlockTransformRenderState.class, new BlockTransformElementRenderer());
        builder.put(EntityBlockRenderState.class, new EntityBlockRenderer());
        builder.put(PartialRenderState.class, new PartialElementRenderer());
        builder.put(BlazeBurnerRenderState.class, new BlazeBurnerElementRenderer());
        builder.put(PressBasinRenderState.class, new PressBasinRenderer());
        builder.put(PressRenderState.class, new PressRenderer());
        builder.put(MixingBasinRenderState.class, new MixingBasinRenderer());
        builder.put(BasinBlazeBurnerRenderState.class, new BasinBlazeBurnerRenderer());
        builder.put(MillstoneRenderState.class, new MillstoneRenderer());
        builder.put(SawRenderState.class, new SawRenderer());
        builder.put(CrushWheelRenderState.class, new CrushWheelRenderer());
        builder.put(DeployerRenderState.class, new DeployerRenderer());
        builder.put(ManualBlockRenderState.class, new ManualBlockRenderer());
        builder.put(SpoutRenderState.class, new SpoutRenderer());
        builder.put(CrafterRenderState.class, new CrafterRenderer());
        builder.put(DrainRenderState.class, new DrainRenderer());
        builder.put(SandPaperRenderState.class, new SandPaperRenderer());
        builder.put(TitleTextRenderState.class, new TitleTextRenderer());
        builder.put(SceneRenderState.class, new SceneRenderer());
        builder.put(FanRenderState.class, new FanRenderer());
        return builder;
    }
}
