package com.zurrtum.create.client.catnip.outliner;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.catnip.render.SuperRenderTypeBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.SubmitNodeStorage.ItemSubmit;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState.FoilType;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class ItemOutline extends Outline {
    private static final QuadInstance quadInstance = new QuadInstance();
    protected Vec3 pos;
    protected ItemStack stack;
    protected ItemStackRenderState itemRenderState;
    protected SubmitNodeStorage queue;
    protected PoseStack matrices;

    public ItemOutline(Vec3 pos, ItemStack stack) {
        this.pos = pos;
        this.stack = stack;
        this.itemRenderState = new ItemStackRenderState();
        this.matrices = new PoseStack();
        this.queue = new SubmitNodeStorage();
    }

    @Override
    public void render(Minecraft mc, PoseStack ms, SuperRenderTypeBuffer buffer, Vec3 camera, float pt) {
        ms.pushPose();

        ms.translate(pos.x - camera.x, pos.y - camera.y, pos.z - camera.z);
        ms.scale(params.alpha, params.alpha, params.alpha);

        mc.getItemModelResolver()
            .updateForTopItem(this.itemRenderState, stack, ItemDisplayContext.FIXED, null, null, 0);
        itemRenderState.submit(ms, queue, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
        render(buffer, false);
        render(buffer, true);
        queue.clear();
        ms.popPose();
    }

    private void render(SuperRenderTypeBuffer buffer, boolean translucent) {
        for (SubmitNodeCollection batchingRenderCommandQueue : queue.getSubmitsPerOrder().values()) {
            for (ItemSubmit submit : batchingRenderCommandQueue.getItemSubmits()) {
                if (ItemFeatureRenderer.hasTranslucency(submit) == translucent) {
                    Pose pose = submit.pose();
                    FoilType foilType = submit.foilType();
                    Pose foilDecalPose = foilType == FoilType.SPECIAL ? ItemFeatureRenderer.computeFoilDecalPose(
                        submit.displayContext(),
                        pose
                    ) : null;
                    quadInstance.setLightCoords(submit.lightCoords());
                    quadInstance.setOverlayCoords(submit.overlayCoords());
                    for (BakedQuad quad : submit.quads()) {
                        BakedQuad.MaterialInfo material = quad.materialInfo();
                        RenderType renderType = material.itemRenderType();
                        quadInstance.setColor(ItemFeatureRenderer.getLayerColorSafe(submit.tintLayers(), material));
                        if (foilType != FoilType.NONE) {
                            VertexConsumer foilBuffer = ItemFeatureRenderer.getFoilBuffer(
                                buffer,
                                renderType,
                                foilDecalPose
                            );
                            foilBuffer.putBakedQuad(pose, quad, quadInstance);
                        }
                        buffer.getBuffer(renderType).putBakedQuad(pose, quad, quadInstance);
                    }
                }
            }
        }
    }
}
