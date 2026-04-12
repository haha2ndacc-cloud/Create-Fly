package com.zurrtum.create.client.content.trains.track;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;

public interface TrackBlockRenderState {
    void submit(PoseStack matrices, SubmitNodeCollector queue);
}
