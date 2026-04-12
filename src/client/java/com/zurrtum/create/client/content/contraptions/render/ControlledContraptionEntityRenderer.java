package com.zurrtum.create.client.content.contraptions.render;

import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.contraptions.render.ContraptionEntityRenderer.AbstractContraptionState;
import com.zurrtum.create.content.contraptions.ControlledContraptionEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;

public class ControlledContraptionEntityRenderer extends ContraptionEntityRenderer<ControlledContraptionEntity, AbstractContraptionState> {
    public ControlledContraptionEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public Pose createTransform(ControlledContraptionEntity entity, float tickProgress) {
        Pose pose = new Pose();
        SuperByteBuffer.nudge(pose, entity.getId());
        Axis axis = entity.getRotationAxis();
        if (axis != null) {
            float angle = entity.getAngle(tickProgress);
            if (angle != 0) {
                Direction direction = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE);
                pose.translate(0.5f, 0.5f, 0.5f);
                pose.rotate(new Quaternionf().setAngleAxis(
                    Mth.DEG_TO_RAD * angle,
                    direction.getStepX(),
                    direction.getStepY(),
                    direction.getStepZ()
                ));
                pose.translate(-0.5f, -0.5f, -0.5f);
            }
        }
        return pose;
    }
}
