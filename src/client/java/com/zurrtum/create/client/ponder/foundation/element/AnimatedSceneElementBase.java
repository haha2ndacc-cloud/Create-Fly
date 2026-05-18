package com.zurrtum.create.client.ponder.foundation.element;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.client.ponder.api.element.AnimatedSceneElement;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public abstract class AnimatedSceneElementBase extends PonderElementBase implements AnimatedSceneElement {

    protected @Nullable Vec3 fadeVec;
    protected LerpedFloat fade;

    public AnimatedSceneElementBase() {
        fade = LerpedFloat.linear().startWithValue(0);
    }

    @Override
    public void forceApplyFade(float fade) {
        this.fade.startWithValue(fade);
    }

    @Override
    public void setFade(float fade) {
        this.fade.setValue(fade);
    }

    @Override
    public void setFadeVec(@Nullable Vec3 fadeVec) {
        this.fadeVec = fadeVec;
    }

    @Override
    public final void renderFirst(
        BlockEntityRenderDispatcher blockEntityRenderDispatcher,
        ModelManager modelManager,
        PonderLevel world,
        SubmitNodeCollector queue,
        CameraRenderState cameraRenderState,
        PoseStack poseStack,
        float pt
    ) {
        poseStack.pushPose();
        float currentFade = applyFade(poseStack, pt);
        renderFirst(
            blockEntityRenderDispatcher,
            modelManager,
            world,
            queue,
            cameraRenderState,
            poseStack,
            currentFade,
            pt
        );
        poseStack.popPose();
    }

    @Override
    public final void renderLast(
        EntityRenderDispatcher entityRenderManager,
        ItemModelResolver itemModelManager,
        PonderLevel world,
        SubmitNodeCollector queue,
        CameraRenderState cameraRenderState,
        PoseStack poseStack,
        float pt
    ) {
        poseStack.pushPose();
        float currentFade = applyFade(poseStack, pt);
        renderLast(entityRenderManager, itemModelManager, world, queue, cameraRenderState, poseStack, currentFade, pt);
        poseStack.popPose();
    }

    protected float applyFade(PoseStack ms, float pt) {
        float currentFade = fade.getValue(pt);
        if (fadeVec != null) {
            Vec3 scaled = fadeVec.scale(-1 + currentFade);
            ms.translate(scaled.x, scaled.y, scaled.z);
        }

        return currentFade;
    }

    protected void renderFirst(
        BlockEntityRenderDispatcher blockEntityRenderDispatcher,
        ModelManager modelManager,
        PonderLevel world,
        SubmitNodeCollector queue,
        CameraRenderState cameraRenderState,
        PoseStack ms,
        float fade,
        float pt
    ) {
    }

    protected void renderLast(
        EntityRenderDispatcher entityRenderManager,
        ItemModelResolver itemModelManager,
        PonderLevel world,
        SubmitNodeCollector queue,
        CameraRenderState cameraRenderState,
        PoseStack ms,
        float fade,
        float pt
    ) {
    }

    protected int lightCoordsFromFade(float fade) {
        int light = LightCoordsUtil.FULL_BRIGHT;
        if (fade != 1) {
            light = (int) Mth.lerp(fade, 5, 0xF);
            light = LightCoordsUtil.pack(light, light);
        }
        return light;
    }

}
