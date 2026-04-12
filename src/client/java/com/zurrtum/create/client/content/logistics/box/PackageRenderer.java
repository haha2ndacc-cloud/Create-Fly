package com.zurrtum.create.client.content.logistics.box;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.logistics.box.PackageRenderer.PackageState;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.logistics.box.PackageEntity;
import com.zurrtum.create.content.logistics.box.PackageItem;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.getUpRotateAngle;

public class PackageRenderer extends EntityRenderer<PackageEntity, PackageState> {
    public PackageRenderer(Context pContext) {
        super(pContext);
        shadowRadius = 0.5f;
    }

    @Override
    public PackageState createRenderState() {
        return new PackageState();
    }

    @Override
    public void extractRenderState(PackageEntity entity, PackageState state, float tickProgress) {
        super.extractRenderState(entity, state, tickProgress);
        if (VisualizationManager.supportsVisualization(entity.level())) {
            return;
        }
        ItemStack box = entity.box;
        if (box.isEmpty() || !PackageItem.isPackage(box)) {
            box = AllItems.CARDBOARD_BLOCK.getDefaultInstance();
        }
        PartialModel model = AllPartialModels.PACKAGES.get(BuiltInRegistries.ITEM.getKey(box.getItem()));
        if (model == null) {
            return;
        }
        int id = entity.getId();
        float yaw = entity.getYRot(tickProgress);
        state.box = getBoxRenderState(id, yaw, state.lightCoords, model);
    }

    @Override
    public void submit(PackageState state, PoseStack ms, SubmitNodeCollector queue, CameraRenderState cameraState) {
        if (state.box != null) {
            state.box.submit(ms, queue);
        }
        super.submit(state, ms, queue, cameraState);
    }

    public static BoxRenderState getBoxRenderState(int id, float yaw, int light, PartialModel model) {
        BoxRenderState state = new BoxRenderState();
        state.model = CachedBuffers.partial(model, Blocks.AIR.defaultBlockState()).light(light).extractRenderState();
        state.angle = getUpRotateAngle(-90 - yaw);
        state.nudge = SmartBlockEntityRenderer.createNudge(id);
        return state;
    }

    public static class PackageState extends EntityRenderState {
        public @Nullable BoxRenderState box;
    }

    public static class BoxRenderState {
        public @UnknownNullability SuperByteBufferRenderState model;
        public @Nullable Quaternionf angle;
        public @UnknownNullability Vec3 nudge;

        public void submit(PoseStack ms, SubmitNodeCollector queue) {
            ms.pushPose();
            ms.translate(-0.5f, 0, -0.5f);
            if (angle != null) {
                ms.rotateAround(angle, 0.5f, 0.5f, 0.5f);
            }
            ms.translate(nudge);
            model.submit(ms, queue);
            ms.popPose();
        }
    }
}
