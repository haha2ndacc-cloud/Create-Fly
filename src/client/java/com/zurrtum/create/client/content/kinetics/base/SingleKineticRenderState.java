package com.zurrtum.create.client.content.kinetics.base;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

public class SingleKineticRenderState extends BlockEntityRenderState {
    public @UnknownNullability SuperByteBufferRenderState model;
    public @Nullable Quaternionf angle;

    public void submit(PoseStack matrices, SubmitNodeCollector queue) {
        if (angle != null) {
            matrices.rotateAround(angle, 0.5f, 0.5f, 0.5f);
        }
        model.submit(matrices, queue);
    }
}
