package com.zurrtum.create.client.ponder.foundation.render;

import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.Nullable;

public record SceneRenderState(int id, PonderScene scene, int width, int height, double slide, boolean userViewMode,
                               LerpedFloat finishingFlash, float partialTicks, Matrix3x2f pose,
                               ScreenRectangle bounds) implements PictureInPictureRenderState {
    public SceneRenderState(
        int id,
        PonderScene scene,
        int width,
        int height,
        double slide,
        boolean userViewMode,
        LerpedFloat finishingFlash,
        float partialTicks,
        Matrix3x2f pose
    ) {
        this(
            id,
            scene,
            width,
            height,
            slide,
            userViewMode,
            finishingFlash,
            partialTicks,
            pose,
            new ScreenRectangle(0, 0, width, height).transformMaxBounds(pose)
        );
    }

    @Override
    public int x0() {
        return 0;
    }

    @Override
    public int x1() {
        return width;
    }

    @Override
    public int y0() {
        return 0;
    }

    @Override
    public int y1() {
        return height;
    }

    @Override
    public float scale() {
        return 1;
    }

    @Override
    public @Nullable ScreenRectangle scissorArea() {
        return null;
    }
}