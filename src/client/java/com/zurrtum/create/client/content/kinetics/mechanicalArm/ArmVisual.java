package com.zurrtum.create.client.content.kinetics.mechanicalArm;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.content.kinetics.base.SingleAxisRotatingVisual;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.visual.DynamicVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.AbstractInstance;
import com.zurrtum.create.client.flywheel.lib.instance.FlatLit;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmBlock;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmBlockEntity.Phase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.function.Consumer;

public class ArmVisual extends SingleAxisRotatingVisual<ArmBlockEntity> implements SimpleDynamicVisual {

    final TransformedInstance base;
    final TransformedInstance lowerBody;
    final TransformedInstance upperBody;
    final TransformedInstance claw;

    private final ArrayList<TransformedInstance> clawGrips;
    private final ArrayList<TransformedInstance> models;
    private final boolean ceiling;

    private final PoseStack poseStack = new PoseStack();
    private final ItemStackRenderState itemRenderState = new ItemStackRenderState();

    private boolean wasDancing;
    private float baseAngle = Float.NaN;
    private float lowerArmAngle = Float.NaN;
    private float upperArmAngle = Float.NaN;
    private float headAngle = Float.NaN;

    public ArmVisual(VisualizationContext context, ArmBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick, Models.chunkPartial(AllPartialModels.ARM_COG));

        base = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.chunkPartial(AllPartialModels.ARM_BASE))
            .createInstance();
        lowerBody = instancerProvider().instancer(
            InstanceTypes.TRANSFORMED,
            Models.chunkPartial(AllPartialModels.ARM_LOWER_BODY)
        ).createInstance();
        upperBody = instancerProvider().instancer(
            InstanceTypes.TRANSFORMED,
            Models.chunkPartial(AllPartialModels.ARM_UPPER_BODY)
        ).createInstance();
        claw = instancerProvider().instancer(
            InstanceTypes.TRANSFORMED,
            Models.chunkPartial(
                blockEntity.goggles ? AllPartialModels.ARM_CLAW_BASE_GOGGLES : AllPartialModels.ARM_CLAW_BASE)
        ).createInstance();

        TransformedInstance clawGrip1 = instancerProvider().instancer(
            InstanceTypes.TRANSFORMED,
            Models.chunkPartial(AllPartialModels.ARM_CLAW_GRIP_UPPER)
        ).createInstance();
        TransformedInstance clawGrip2 = instancerProvider().instancer(
            InstanceTypes.TRANSFORMED,
            Models.chunkPartial(AllPartialModels.ARM_CLAW_GRIP_LOWER)
        ).createInstance();

        clawGrips = Lists.newArrayList(clawGrip1, clawGrip2);
        models = Lists.newArrayList(base, lowerBody, upperBody, claw, clawGrip1, clawGrip2);
        ceiling = blockState.getValue(ArmBlock.CEILING);

        var msr = TransformStack.of(poseStack);
        msr.translate(getVisualPosition());
        msr.center();

        if (ceiling) {
            msr.rotateXDegrees(180);
        }

        animate(partialTick);
    }

    @Override
    public void setSectionCollector(SectionCollector sectionCollector) {
        setSectionCollector(sectionCollector, -2, -2, -2, 2, 3, 2);
    }

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {
        animate(ctx.partialTick());
    }

    private void animate(float pt) {
        if (blockEntity.phase == Phase.DANCING && blockEntity.getSpeed() != 0) {
            animateRave(pt);
            wasDancing = true;
            return;
        }

        float baseAngleNow = blockEntity.baseAngle.getValue(pt);
        float lowerArmAngleNow = blockEntity.lowerArmAngle.getValue(pt);
        float upperArmAngleNow = blockEntity.upperArmAngle.getValue(pt);
        float headAngleNow = blockEntity.headAngle.getValue(pt);

        boolean settled = Mth.equal(baseAngle, baseAngleNow) && Mth.equal(lowerArmAngle, lowerArmAngleNow) && Mth.equal(upperArmAngle,
            upperArmAngleNow
        ) && Mth.equal(headAngle, headAngleNow);

        baseAngle = baseAngleNow;
        lowerArmAngle = lowerArmAngleNow;
        upperArmAngle = upperArmAngleNow;
        headAngle = headAngleNow;

        // Need to reset the animation if the arm is dancing. We'd very likely be settled
        if (!settled || wasDancing) {
            animateArm();
        }

        wasDancing = false;
    }

    private void animateRave(float partialTick) {
        var ticks = AnimationTickHolder.getTicks(blockEntity.getLevel());
        float renderTick = ticks + partialTick + blockEntity.hashCode() % 64;

        float baseAngle = renderTick * 10 % 360;
        float lowerArmAngle = Mth.lerp((Mth.sin(renderTick / 4) + 1) / 2, -45, 15);
        float upperArmAngle = Mth.lerp((Mth.sin(renderTick / 8) + 1) / 4, -45, 95);
        float headAngle = -lowerArmAngle;
        int color = Color.rainbowColor(ticks * 100).getRGB();
        updateAngles(baseAngle, lowerArmAngle, upperArmAngle, headAngle, color);
    }

    private void animateArm() {
        updateAngles(baseAngle, lowerArmAngle - 135, upperArmAngle - 90, headAngle, 0xFFFFFF);
    }

    private void updateAngles(float baseAngle, float lowerArmAngle, float upperArmAngle, float headAngle, int color) {
        poseStack.pushPose();

        var msr = TransformStack.of(poseStack);

        transformBase(msr, baseAngle);
        base.setTransform(poseStack).setChanged();

        transformLowerArm(msr, lowerArmAngle);
        lowerBody.setTransform(poseStack).colorRgb(color).setChanged();

        transformUpperArm(msr, upperArmAngle);
        upperBody.setTransform(poseStack).colorRgb(color).setChanged();

        transformHead(msr, headAngle);

        if (ceiling && blockEntity.goggles) {
            msr.rotateZDegrees(180);
        }

        claw.setTransform(poseStack).setChanged();

        if (ceiling && blockEntity.goggles) {
            msr.rotateZDegrees(180);
        }

        ItemStack item = blockEntity.heldItem;
        boolean hasItem = !item.isEmpty();
        boolean isBlockItem;
        if (hasItem && item.getItem() instanceof BlockItem) {
            Minecraft mc = Minecraft.getInstance();
            mc.getItemModelResolver()
                .updateForTopItem(itemRenderState, item, ItemDisplayContext.FIXED, mc.level, null, 0);
            isBlockItem = itemRenderState.usesBlockLight();
        } else {
            isBlockItem = false;
        }

        for (int index : Iterate.zeroAndOne) {
            poseStack.pushPose();
            int flip = index * 2 - 1;
            transformClawHalf(msr, hasItem, isBlockItem, flip);
            clawGrips.get(index).setTransform(poseStack).setChanged();
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    @Override
    public void update(float pt) {
        super.update(pt);
        instancerProvider().instancer(
            InstanceTypes.TRANSFORMED,
            Models.chunkPartial(
                blockEntity.goggles ? AllPartialModels.ARM_CLAW_BASE_GOGGLES : AllPartialModels.ARM_CLAW_BASE)
        ).stealInstance(claw);
    }

    @Override
    public void updateLight(float partialTick) {
        super.updateLight(partialTick);

        relight(models.toArray(FlatLit[]::new));
    }

    @Override
    protected void _delete() {
        super._delete();
        models.forEach(AbstractInstance::delete);
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        super.collectCrumblingInstances(consumer);
        models.forEach(consumer);
    }

    public static void transformClawHalf(TransformStack<?> msr, boolean hasItem, boolean isBlockItem, int flip) {
        msr.translate(0, -flip * (hasItem ? isBlockItem ? 3 / 16.0f : 5 / 64.0f : 1 / 16.0f), -6 / 16.0d);
    }

    public static void transformHead(TransformStack<?> msr, float headAngle) {
        msr.translate(0, 0, -15 / 16.0d);
        msr.rotateXDegrees(headAngle - 45.0f);
    }

    public static void transformUpperArm(TransformStack<?> msr, float upperArmAngle) {
        msr.translate(0, 0, -14 / 16.0d);
        msr.rotateXDegrees(upperArmAngle - 90);
    }

    public static void transformLowerArm(TransformStack<?> msr, float lowerArmAngle) {
        msr.translate(0, 2 / 16.0d, 0);
        msr.rotateXDegrees(lowerArmAngle + 135);
    }

    public static void transformBase(TransformStack<?> msr, float baseAngle) {
        msr.translate(0, 4 / 16.0d, 0);
        msr.rotateYDegrees(baseAngle);
    }
}
