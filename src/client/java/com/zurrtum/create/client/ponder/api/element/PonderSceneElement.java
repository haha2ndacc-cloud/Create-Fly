package com.zurrtum.create.client.ponder.api.element;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.resources.model.ModelManager;

public interface PonderSceneElement extends PonderElement {
    void renderFirst(
        BlockEntityRenderDispatcher blockEntityRenderDispatcher,
        ModelManager modelManager,
        PonderLevel world,
        SubmitNodeCollector queue,
        CameraRenderState cameraRenderState,
        PoseStack ms,
        float pt
    );

    void renderLast(
        EntityRenderDispatcher entityRenderManager,
        ItemModelResolver itemModelManager,
        PonderLevel world,
        SubmitNodeCollector queue,
        CameraRenderState cameraRenderState,
        PoseStack ms,
        float pt
    );
}
