package com.zurrtum.create.client.content.kinetics.crafter;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.Pointing;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.AllSpriteShifts;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.crafter.MechanicalCrafterRenderer.MechanicalCrafterRenderState;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.crafter.MechanicalCrafterBlock;
import com.zurrtum.create.content.kinetics.crafter.MechanicalCrafterBlockEntity;
import com.zurrtum.create.content.kinetics.crafter.MechanicalCrafterBlockEntity.Phase;
import com.zurrtum.create.content.kinetics.crafter.RecipeGridHandler.GroupedItems;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.*;
import static com.zurrtum.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING;

public class MechanicalCrafterRenderer implements BlockEntityRenderer<MechanicalCrafterBlockEntity, MechanicalCrafterRenderState> {
    protected final ItemModelResolver itemModelManager;

    public MechanicalCrafterRenderer(Context context) {
        itemModelManager = context.itemModelResolver();
    }

    @Override
    public MechanicalCrafterRenderState createRenderState() {
        return new MechanicalCrafterRenderState();
    }

    @Override
    public void extractRenderState(
        MechanicalCrafterBlockEntity be,
        MechanicalCrafterRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
        Phase phase = be.phase;
        state.item = createItemState(itemModelManager, be, level, state.blockState, phase, tickProgress);
        Direction facing = state.blockState.getValue(HORIZONTAL_FACING);
        float yRot = AngleHelper.horizontalAngle(facing);
        if (state.item != null) {
            Vec3 vec = Vec3.atLowerCornerOf(facing.getUnitVec3i()).scale(0.58).add(0.5, 0.5, 0.5);
            if (phase == Phase.EXPORTING) {
                Direction targetDirection = MechanicalCrafterBlock.getTargetDirection(state.blockState);
                float progress = Mth.clamp(
                    (1000 - be.countDown + be.getCountDownSpeed() * tickProgress) / 1000.0f,
                    0,
                    1
                );
                vec = vec.add(Vec3.atLowerCornerOf(targetDirection.getUnitVec3i()).scale(progress * 0.75f));
            }
            state.offset = vec;
            state.yRot = getYRotateAngle(yRot);
        }
        if (!VisualizationManager.supportsVisualization(level)) {
            state.cogwheel = CogwheelRenderState.create(be, level, state, cardinalLighting, facing);
        }
        state.upRot = getUpRotateAngle(yRot + 90);
        state.eastRot = getEastRotateAngle(state.blockState.getValue(MechanicalCrafterBlock.POINTING).getXRotation());
        if ((be.covered || phase != Phase.IDLE) && phase != Phase.CRAFTING && phase != Phase.INSERTING) {
            state.lid = CachedBuffers.partial(AllPartialModels.MECHANICAL_CRAFTER_LID, state.blockState)
                .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        }
        Direction targetDirection = MechanicalCrafterBlock.getTargetDirection(state.blockState);
        if (MechanicalCrafterBlock.isValidTarget(level, state.blockPos.relative(targetDirection), state.blockState)) {
            SuperByteBuffer belt = CachedBuffers.partial(AllPartialModels.MECHANICAL_CRAFTER_BELT, state.blockState);
            if (phase == Phase.EXPORTING) {
                int textureIndex = (int) (be.getCountDownSpeed() / 128.0f * AnimationTickHolder.getTicks());
                belt.shiftUVtoSheet(AllSpriteShifts.CRAFTER_THINGIES, (textureIndex % 4) / 4.0f, 0, 1);
            }
            state.belt = belt.cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
            state.frame = CachedBuffers.partial(AllPartialModels.MECHANICAL_CRAFTER_BELT_FRAME, state.blockState)
                .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        } else {
            state.arrow = CachedBuffers.partial(AllPartialModels.MECHANICAL_CRAFTER_ARROW, state.blockState)
                .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        }
    }

    @Nullable
    public static MechanicalCrafterItemRenderState createItemState(
        ItemModelResolver itemModelManager,
        MechanicalCrafterBlockEntity be,
        @Nullable Level world,
        BlockState blockState,
        Phase phase,
        float tickProgress
    ) {
        if (phase == Phase.IDLE) {
            return MechanicalCrafterSingleItemRenderState.create(itemModelManager, be, world);
        }
        if (phase == Phase.CRAFTING) {
            return MechanicalCrafterCraftingItemRenderState.create(itemModelManager, be, world, tickProgress);
        }
        return MechanicalCrafterPhaseItemRenderState.create(itemModelManager, be, world, blockState, phase);
    }

    @Override
    public void submit(
        MechanicalCrafterRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        if (state.item != null) {
            matrices.pushPose();
            matrices.translate(state.offset);
            matrices.scale(0.5f, 0.5f, 0.5f);
            if (state.yRot != null) {
                matrices.mulPose(state.yRot);
            }
            state.item.submit(queue, matrices, state.lightCoords);
            matrices.popPose();
        }
        if (state.cogwheel != null) {
            state.cogwheel.submit(matrices, queue);
        }
        if (state.upRot != null) {
            matrices.rotateAround(state.upRot, 0.5f, 0.5f, 0.5f);
        }
        if (state.eastRot != null) {
            matrices.rotateAround(state.eastRot, 0.5f, 0.5f, 0.5f);
        }
        if (state.lid != null) {
            state.lid.submit(matrices, queue);
        }
        if (state.belt != null) {
            state.belt.submit(matrices, queue);
            state.frame.submit(matrices, queue);
        } else {
            state.arrow.submit(matrices, queue);
        }
    }

    public static class MechanicalCrafterRenderState extends BlockEntityRenderState {
        public @UnknownNullability Vec3 offset;
        public @Nullable Quaternionf yRot;
        public @Nullable MechanicalCrafterItemRenderState item;
        public @Nullable CogwheelRenderState cogwheel;
        public @Nullable Quaternionfc upRot;
        public @Nullable Quaternionfc eastRot;
        public @Nullable SuperByteBufferRenderState lid;
        public @Nullable SuperByteBufferRenderState belt;
        public @UnknownNullability SuperByteBufferRenderState frame;
        public @UnknownNullability SuperByteBufferRenderState arrow;
    }

    public record CogwheelRenderState(SuperByteBufferRenderState cogwheel, @Nullable Quaternionf angle,
                                      @Nullable Quaternionf upAngle) {
        private static final Quaternionf EAST_ANGLE = new Quaternionf().setAngleAxis(Mth.HALF_PI, 1, 0, 0);

        public static CogwheelRenderState create(
            MechanicalCrafterBlockEntity be,
            @Nullable Level level,
            MechanicalCrafterRenderState state,
            @Nullable CardinalLighting cardinalLighting,
            Direction facing
        ) {
            SuperByteBufferRenderState model = CachedBuffers.partial(
                AllPartialModels.SHAFTLESS_COGWHEEL,
                state.blockState
            ).cardinalLighting(cardinalLighting).light(state.lightCoords).color(getTintColor(be)).extractRenderState();
            Direction.Axis axis = facing.getAxis();
            Quaternionf angle = getRotateAngleWithoutBeOffset(axis, be, state, level);
            Quaternionf upAngle = axis == Direction.Axis.X ? new Quaternionf().setAngleAxis(
                Mth.HALF_PI,
                0,
                1,
                0
            ) : null;
            return new CogwheelRenderState(model, angle, upAngle);
        }

        public void submit(PoseStack matrices, SubmitNodeCollector queue) {
            matrices.pushPose();
            if (angle != null) {
                matrices.rotateAround(angle, 0.5f, 0.5f, 0.5f);
            }
            if (upAngle != null) {
                matrices.rotateAround(upAngle, 0.5f, 0.5f, 0.5f);
            }
            matrices.rotateAround(EAST_ANGLE, 0.5f, 0.5f, 0.5f);
            cogwheel.submit(matrices, queue);
            matrices.popPose();
        }
    }

    public interface MechanicalCrafterItemRenderState {
        void submit(SubmitNodeCollector queue, PoseStack ms, int light);
    }

    public record MechanicalCrafterSingleItemRenderState(float offset, Quaternionf yRot,
                                                         ItemStackRenderState state) implements MechanicalCrafterItemRenderState {
        @Nullable
        public static MechanicalCrafterSingleItemRenderState create(
            ItemModelResolver itemModelManager,
            MechanicalCrafterBlockEntity be,
            @Nullable Level world
        ) {
            ItemStack stack = be.getInventory().getStack();
            if (stack.isEmpty()) {
                return null;
            }
            float offset = -1 / 256.0f;
            Quaternionf yRot = Axis.YP.rotation(RAD_180);
            ItemStackRenderState state = new ItemStackRenderState();
            state.displayContext = ItemDisplayContext.FIXED;
            itemModelManager.appendItemLayers(state, stack, state.displayContext, world, null, 0);
            return new MechanicalCrafterSingleItemRenderState(offset, yRot, state);
        }

        @Override
        public void submit(SubmitNodeCollector queue, PoseStack ms, int light) {
            ms.pushPose();
            ms.translate(0, 0, offset);
            ms.mulPose(yRot);
            state.submit(ms, queue, light, OverlayTexture.NO_OVERLAY, 0);
            ms.popPose();
        }
    }

    public record MechanicalCrafterCraftingItemRenderState(float scale, @Nullable Vec3 centering,
                                                           @Nullable List<GridItemRenderState> before, Quaternionf yRot,
                                                           @Nullable Quaternionf zRot, float upScaling,
                                                           float downScaling,
                                                           @Nullable List<ItemStackRenderState> states) implements MechanicalCrafterItemRenderState {
        @Nullable
        public static MechanicalCrafterCraftingItemRenderState create(
            ItemModelResolver itemModelManager,
            MechanicalCrafterBlockEntity be,
            @Nullable Level world,
            float tickProgress
        ) {
            GroupedItems items = be.groupedItemsBeforeCraft;
            boolean beforeEmpty = items.grid.isEmpty();
            boolean itemsEmpty = be.groupedItems.grid.isEmpty();
            if (beforeEmpty && itemsEmpty) {
                return null;
            }
            Quaternionf yRot = Axis.YP.rotation(RAD_180);
            float value = be.countDown - be.getCountDownSpeed() * tickProgress;
            float scale;
            Vec3 centering;
            List<GridItemRenderState> before;
            if (beforeEmpty) {
                scale = 0;
                centering = null;
                before = null;
            } else {
                items.calcStats();
                float progress = Mth.clamp((2000 - value) / 1000.0f, 0, 1);
                float earlyProgress = Mth.clamp(progress * 2, 0, 1);
                scale = 1 - Mth.clamp(progress * 2 - 1, 0, 1);
                centering = new Vec3(
                    -items.minX + (-items.width + 1) / 2.0f,
                    -items.minY + (-items.height + 1) / 2.0f,
                    0
                ).scale(earlyProgress).multiply(0.5, 0.5, 1);
                float distance = 0.5f + (-4 * (progress - 0.5f) * (progress - 0.5f) + 1) * 0.25f;
                boolean onlyRenderFirst = be.countDown < 1000;
                before = new ArrayList<>(items.grid.size());
                items.grid.forEach((pair, stack) -> {
                    if (onlyRenderFirst && (pair.getFirst() != 0 || pair.getSecond() != 0)) {
                        return;
                    }
                    int x = pair.getFirst();
                    int y = pair.getSecond();
                    float offsetX = x * distance;
                    float offsetY = y * distance;
                    float offsetZ = (x + y * 3) / 1024.0f;
                    ItemStackRenderState state = new ItemStackRenderState();
                    state.displayContext = ItemDisplayContext.FIXED;
                    itemModelManager.appendItemLayers(state, stack, state.displayContext, world, null, 0);
                    before.add(new GridItemRenderState(state, offsetX, offsetY, offsetZ));
                });
            }
            Quaternionf zRot;
            float upScaling;
            float downScaling;
            List<ItemStackRenderState> states;
            if (itemsEmpty) {
                zRot = null;
                upScaling = downScaling = 0;
                states = null;
            } else {
                float progress = Mth.clamp((1000 - value) / 1000.0f, 0, 1);
                float earlyProgress = Mth.clamp(progress * 2, 0, 1);
                zRot = getZRotateAngle(earlyProgress * 720);
                upScaling = earlyProgress * 1.125f;
                downScaling = 1 + (1 - Mth.clamp(progress * 2 - 1, 0, 1)) * 0.125f;
                items = be.groupedItems;
                states = new ArrayList<>(items.grid.size());
                items.grid.forEach((pair, stack) -> {
                    if (pair.getFirst() != 0 || pair.getSecond() != 0) {
                        return;
                    }
                    ItemStackRenderState state = new ItemStackRenderState();
                    state.displayContext = ItemDisplayContext.FIXED;
                    itemModelManager.appendItemLayers(state, stack, state.displayContext, world, null, 0);
                    states.add(state);
                });
            }
            return new MechanicalCrafterCraftingItemRenderState(
                scale,
                centering,
                before,
                yRot,
                zRot,
                upScaling,
                downScaling,
                states
            );
        }

        @Override
        public void submit(SubmitNodeCollector queue, PoseStack ms, int light) {
            if (before != null) {
                ms.pushPose();
                ms.scale(scale, scale, scale);
                ms.translate(centering);
                for (GridItemRenderState state : before) {
                    state.submit(queue, ms, yRot, light);
                }
                ms.popPose();
            }
            if (states != null) {
                if (zRot != null) {
                    ms.mulPose(zRot);
                }
                ms.scale(upScaling, upScaling, upScaling);
                ms.scale(downScaling, downScaling, downScaling);
                ms.mulPose(yRot);
                for (ItemStackRenderState state : states) {
                    state.submit(ms, queue, light, OverlayTexture.NO_OVERLAY, 0);
                }
            }
        }
    }

    public record MechanicalCrafterPhaseItemRenderState(List<GridItemRenderState> states,
                                                        Quaternionf yRot) implements MechanicalCrafterItemRenderState {
        @Nullable
        public static MechanicalCrafterPhaseItemRenderState create(
            ItemModelResolver itemModelManager,
            MechanicalCrafterBlockEntity be,
            @Nullable Level world,
            BlockState blockState,
            Phase phase
        ) {
            Map<Pair<Integer, Integer>, ItemStack> grid = be.groupedItems.grid;
            if (grid.isEmpty()) {
                return null;
            }
            float distance = 0.5f;
            boolean onlyRenderFirst = phase == Phase.INSERTING;
            boolean isExporting = phase == Phase.EXPORTING && blockState.hasProperty(MechanicalCrafterBlock.POINTING);
            Pointing pointing = isExporting ? blockState.getValue(MechanicalCrafterBlock.POINTING) : null;
            Quaternionf yRot = Axis.YP.rotation(RAD_180);
            List<GridItemRenderState> states = new ArrayList<>(grid.size());
            grid.forEach((pair, stack) -> {
                if (onlyRenderFirst && (pair.getFirst() != 0 || pair.getSecond() != 0)) {
                    return;
                }
                int x = pair.getFirst();
                int y = pair.getSecond();
                float offsetX = x * distance;
                float offsetY = y * distance;
                int value = x + y * 3;
                if (pointing != null) {
                    switch (pointing) {
                        case UP -> value -= 9;
                        case LEFT -> value += 18;
                        case RIGHT -> value -= 18;
                        case DOWN -> value += 9;
                    }
                }
                float offsetZ = value / 1024.0f;
                ItemStackRenderState state = new ItemStackRenderState();
                state.displayContext = ItemDisplayContext.FIXED;
                itemModelManager.appendItemLayers(state, stack, state.displayContext, world, null, 0);
                states.add(new GridItemRenderState(state, offsetX, offsetY, offsetZ));
            });
            return new MechanicalCrafterPhaseItemRenderState(states, yRot);
        }

        @Override
        public void submit(SubmitNodeCollector queue, PoseStack ms, int light) {
            for (GridItemRenderState state : states) {
                state.submit(queue, ms, yRot, light);
            }
        }
    }

    public record GridItemRenderState(ItemStackRenderState state, float offsetX, float offsetY, float offsetZ) {
        public void submit(SubmitNodeCollector queue, PoseStack ms, Quaternionf yRot, int light) {
            ms.pushPose();
            ms.translate(offsetX, offsetY, 0);
            ms.mulPose(yRot);
            ms.translate(0, 0, offsetZ);
            state.submit(ms, queue, light, OverlayTexture.NO_OVERLAY, 0);
            ms.popPose();
        }
    }
}
