package com.zurrtum.create.client.mixin;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.foundation.model.FabricBakedModelHelper;
import com.zurrtum.create.client.infrastructure.model.CopycatModel;
import com.zurrtum.create.client.infrastructure.model.CopycatStepModel;
import com.zurrtum.create.content.decoration.copycat.CopycatBlock;
import com.zurrtum.create.content.decoration.copycat.CopycatBlockEntity;
import com.zurrtum.create.content.decoration.copycat.CopycatStepBlock;
import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableMesh;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.model.FabricBlockStateModel;
import net.fabricmc.fabric.api.client.renderer.v1.sprite.FabricTextureAtlas;
import net.fabricmc.fabric.api.client.renderer.v1.sprite.SpriteFinder;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.AtlasIds;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Predicate;

@Mixin(CopycatStepModel.class)
public abstract class CopycatStepModelMixin extends CopycatModel implements FabricBlockStateModel {
    @Shadow
    @Final
    protected static Vec3 VEC_Y_3;

    @Shadow
    @Final
    protected static Vec3 VEC_Y_2;

    @Shadow
    @Final
    protected static Vec3 VEC_Y_N2;

    private CopycatStepModelMixin(BlockState state, UnbakedRoot unbaked) {
        super(state, unbaked);
    }

    @Override
    public void emitQuads(
        @NonNull QuadEmitter emitter,
        @NonNull BlockAndTintGetter level,
        @NonNull BlockPos pos,
        BlockState state,
        @NonNull RandomSource random,
        @NonNull Predicate<@Nullable Direction> cullTest
    ) {
        if (!(state.getBlock() instanceof CopycatBlock block)) {
            return;
        }
        CopycatBlockEntity copycat = (CopycatBlockEntity) level.getBlockEntity(pos);
        BlockState material = copycat == null ? AllBlocks.COPYCAT_BASE.defaultBlockState() : copycat.getMaterial();
        TriState defaultAo = TriState.of(material.getLightEmission() == 0);
        boolean emissive = material.emissiveRendering();
        Direction facing = state.getValueOrElse(CopycatStepBlock.FACING, Direction.SOUTH);
        Direction opposite = facing.getOpposite();
        boolean upperHalf = state.getValueOrElse(CopycatStepBlock.HALF, Half.BOTTOM) == Half.TOP;
        Vec3 normal = Vec3.atLowerCornerOf(facing.getUnitVec3i());
        Vec3 normalScaled2 = normal.scale(0.5);
        Vec3 normalScaledN3 = normal.scale(-0.75);
        AABB bottomBack = CUBE_AABB.contract(-normal.x * 0.75, 0.75, -normal.z * 0.75);
        AABB bottomFront = bottomBack.move(normalScaledN3);
        AABB topBack = bottomBack.move(VEC_Y_3);
        AABB topFront = bottomFront.move(VEC_Y_3);
        Vec3 topFrontOffset, bottomFrontOffset, topBackOffset, bottomBackOffset;
        if (upperHalf) {
            topFrontOffset = normalScaled2;
            bottomFrontOffset = normalScaled2.add(VEC_Y_2);
            topBackOffset = Vec3.ZERO;
            bottomBackOffset = VEC_Y_2;
        } else {
            topFrontOffset = normalScaled2.add(VEC_Y_N2);
            bottomFrontOffset = normalScaled2;
            topBackOffset = VEC_Y_N2;
            bottomBackOffset = Vec3.ZERO;
        }
        MutableMesh mutableMesh = Renderer.get().mutableMesh();
        QuadEmitter meshEmitter = mutableMesh.emitter();
        SpriteFinder spriteFinder = ((FabricTextureAtlas) Minecraft.getInstance().getAtlasManager()
            .getAtlasOrThrow(AtlasIds.BLOCKS)).spriteFinder();
        int[] crop = new int[4];
        DirectionData directionData = gatherDirectionData(block, state);
        emitter.pushTransform(quad -> {
            Direction direction = quad.cullFace();
            if (direction != null && directionData.isUncull(direction)) {
                quad.cullFace(null);
            }
            int count = 0;
            if (direction != facing) {
                if (direction != Direction.DOWN) {
                    crop[count++] = 0;
                }
                if (direction != Direction.UP) {
                    crop[count++] = 1;
                }
            }
            if (direction != opposite) {
                if (direction != Direction.DOWN) {
                    crop[count++] = 2;
                }
                if (direction != Direction.UP) {
                    crop[count++] = 3;
                }
            }
            if (count == 0) {
                return false;
            }
            if (quad.ambientOcclusion() == TriState.DEFAULT) {
                quad.ambientOcclusion(defaultAo);
            }
            if (emissive) {
                quad.emissive(true);
            }
            TextureAtlasSprite sprite = spriteFinder.find(quad);
            int end = count - 1;
            for (int i = 0; i < end; i++) {
                meshEmitter.copyFrom(quad);
                switch (crop[i]) {
                    case 0 -> FabricBakedModelHelper.cropAndMove(meshEmitter, sprite, topFront, topFrontOffset);
                    case 1 -> FabricBakedModelHelper.cropAndMove(meshEmitter, sprite, bottomFront, bottomFrontOffset);
                    case 2 -> FabricBakedModelHelper.cropAndMove(meshEmitter, sprite, topBack, topBackOffset);
                    case 3 -> FabricBakedModelHelper.cropAndMove(meshEmitter, sprite, bottomBack, bottomBackOffset);
                }
                meshEmitter.emit();
            }
            switch (crop[end]) {
                case 0 -> FabricBakedModelHelper.cropAndMove(quad, sprite, topFront, topFrontOffset);
                case 1 -> FabricBakedModelHelper.cropAndMove(quad, sprite, bottomFront, bottomFrontOffset);
                case 2 -> FabricBakedModelHelper.cropAndMove(quad, sprite, topBack, topBackOffset);
                case 3 -> FabricBakedModelHelper.cropAndMove(quad, sprite, bottomBack, bottomBackOffset);
            }
            return true;
        });
        ((FabricBlockStateModel) getModelOf(material)).emitQuads(
            emitter,
            level,
            pos,
            material,
            random,
            direction -> cullTest.test(direction) && directionData.isCull(direction)
        );
        emitter.popTransform();
        mutableMesh.outputTo(emitter);
    }
}
