package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.zurrtum.create.client.flywheel.lib.model.SimpleModel;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.model.BakedQuad;
import org.jetbrains.annotations.UnknownNullability;

public class VanillinMeshEmitterManager extends MeshEmitterManager<MeshEmitter> implements BlockQuadOutput {
    private boolean useAo;
    @UnknownNullability
    private PoseStack poseStack;

    VanillinMeshEmitterManager() {
        super(MeshEmitter::new);
    }

    public void prepare(BlockMaterialFunction blockMaterialFunction, PoseStack poseStack) {
        super.prepare(blockMaterialFunction);
        this.poseStack = poseStack;
    }

    public void prepareForModelLayer(boolean useAo) {
        this.useAo = useAo;
    }

    @Override
    public void put(float x, float y, float z, BakedQuad quad, QuadInstance instance) {
        BufferBuilder buffer = getBuffer(quad.spriteInfo().layer(), quad.shade(), useAo);
        if (buffer != null) {
            if (x != 0F || y != 0F || z != 0F) {
                poseStack.pushPose();
                poseStack.translate(x, y, z);
                buffer.putBakedQuad(poseStack.last(), quad, instance);
                poseStack.popPose();
            } else {
                buffer.putBakedQuad(poseStack.last(), quad, instance);
            }
        }
    }

    @Override
    public SimpleModel end() {
        poseStack = null;
        return super.end();
    }
}
