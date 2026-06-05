package com.zurrtum.create.client.content.schematics.client.tools;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.outliner.LineOutline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class RotateTool extends PlacementToolBase {

    private final LineOutline line = new LineOutline();

    @Override
    public boolean handleMouseWheel(double delta) {
        schematicHandler.getTransformation().rotate90(delta > 0);
        schematicHandler.markDirty();
        return true;
    }

    @Override
    public void renderOnSchematic(Minecraft mc, PoseStack ms, SubmitNodeCollector queue) {
        AABB bounds = schematicHandler.getBounds();
        double lengthY = bounds.getYsize();
        double height = lengthY + Math.max(20, lengthY);
        Vec3 center = bounds.getCenter().add(schematicHandler.getTransformation().getRotationOffset(false));
        Vec3 start = center.subtract(0, height / 2, 0);
        Vec3 end = center.add(0, height / 2, 0);

        line.getParams().disableCull().disableLineNormals().colored(0xdddddd).lineWidth(1 / 16.0f);
        line.set(start, end).submit(mc, ms, queue, Vec3.ZERO, AnimationTickHolder.getPartialTicks());

        super.renderOnSchematic(mc, ms, queue);
    }

}
