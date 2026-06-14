package com.zurrtum.create.client.mixin;

import com.google.common.base.Predicates;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.foundation.model.FabricBakedModelHelper;
import com.zurrtum.create.client.infrastructure.model.CopycatModel;
import com.zurrtum.create.client.infrastructure.model.CopycatPanelModel;
import com.zurrtum.create.content.decoration.copycat.CopycatBlock;
import com.zurrtum.create.content.decoration.copycat.CopycatBlockEntity;
import com.zurrtum.create.content.decoration.copycat.CopycatPanelBlock;
import com.zurrtum.create.content.decoration.copycat.CopycatSpecialCases;
import com.zurrtum.create.foundation.block.WrenchableDirectionalBlock;
import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableMesh;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.model.FabricBlockStateModel;
import net.fabricmc.fabric.api.client.renderer.v1.sprite.FabricTextureAtlas;
import net.fabricmc.fabric.api.client.renderer.v1.sprite.SpriteFinder;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.AtlasIds;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Predicate;

@Mixin(CopycatPanelModel.class)
public abstract class CopycatPanelModelMixin extends CopycatModel implements FabricBlockStateModel {
    private CopycatPanelModelMixin(BlockState state, UnbakedRoot unbaked) {
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
        if (CopycatSpecialCases.isTrapdoorMaterial(material)) {
            emitter.pushTransform(quad -> {
                if (quad.ambientOcclusion() == TriState.DEFAULT) {
                    quad.ambientOcclusion(defaultAo);
                }
                if (emissive) {
                    quad.emissive(true);
                }
                return true;
            });
            ((FabricBlockStateModel) getModelOf(material)).emitQuads(emitter, level, pos, material, random, cullTest);
            emitter.popTransform();
            return;
        }
        Direction facing = state.getValueOrElse(CopycatPanelBlock.FACING, Direction.UP);
        SpriteFinder spriteFinder = ((FabricTextureAtlas) Minecraft.getInstance().getAtlasManager()
            .getAtlasOrThrow(AtlasIds.BLOCKS)).spriteFinder();
        DirectionData directionData = gatherDirectionData(block, state);
        if (CopycatSpecialCases.isBarsMaterial(material)) {
            BlockState bars = AllBlocks.COPYCAT_BARS.defaultBlockState()
                .setValue(WrenchableDirectionalBlock.FACING, facing);
            boolean vertical = state.getValue(CopycatPanelBlock.FACING).getAxis() == Direction.Axis.Y;
            BlockStateModel model = getModelOf(material);
            TextureAtlasSprite particle = model.particleMaterial().sprite();
            SpriteHolder spriteHolder = new SpriteHolder(particle);
            emitter.pushTransform(quad -> {
                if (spriteHolder.isDefault && quad.cullFace() == null && quad.nominalFace() == Direction.UP) {
                    spriteHolder.set(spriteFinder.find(quad));
                }
                return false;
            });
            ((FabricBlockStateModel) model).emitQuads(emitter, level, pos, material, random, Predicates.alwaysFalse());
            emitter.popTransform();
            TextureAtlasSprite targetSprite = spriteHolder.get();
            emitter.pushTransform(quad -> {
                Direction direction = quad.cullFace();
                TextureAtlasSprite sprite;
                if (direction != null) {
                    if (directionData.isUncull(direction)) {
                        quad.cullFace(null);
                    }
                    sprite = vertical || direction.getAxis() == Direction.Axis.Y ? targetSprite : particle;
                } else {
                    sprite = particle;
                }
                if (quad.ambientOcclusion() == TriState.DEFAULT) {
                    quad.ambientOcclusion(defaultAo);
                }
                if (emissive) {
                    quad.emissive(true);
                }
                TextureAtlasSprite original = spriteFinder.find(quad);
                FabricBakedModelHelper.updateSpriteUv(quad, 0, original, sprite);
                FabricBakedModelHelper.updateSpriteUv(quad, 1, original, sprite);
                FabricBakedModelHelper.updateSpriteUv(quad, 2, original, sprite);
                FabricBakedModelHelper.updateSpriteUv(quad, 3, original, sprite);
                return true;
            });
            ((FabricBlockStateModel) getModelOf(bars)).emitQuads(
                emitter,
                level,
                pos,
                bars,
                random,
                direction -> cullTest.test(direction) && directionData.isCull(direction)
            );
            emitter.popTransform();
        } else {
            Direction opposite = facing.getOpposite();
            Vec3 normal = Vec3.atLowerCornerOf(facing.getUnitVec3i());
            Vec3 normalScaled14 = normal.scale(0.875f);
            AABB front = CUBE_AABB.contract(normal.x * 0.9375d, normal.y * 0.9375d, normal.z * 0.9375d);
            Vec3 frontOffset = Vec3.ZERO;
            AABB back = CUBE_AABB.contract(normal.x * 0.875d, normal.y * 0.875d, normal.z * 0.875d)
                .move(normalScaled14);
            Vec3 backOffset = normal.scale(-0.8125f);
            MutableMesh mutableMesh = Renderer.get().mutableMesh();
            QuadEmitter meshEmitter = mutableMesh.emitter();
            emitter.pushTransform(quad -> {
                Direction direction = quad.cullFace();
                if (direction != null && directionData.isUncull(direction)) {
                    quad.cullFace(null);
                }
                if (quad.ambientOcclusion() == TriState.DEFAULT) {
                    quad.ambientOcclusion(defaultAo);
                }
                if (emissive) {
                    quad.emissive(true);
                }
                if (direction != facing) {
                    TextureAtlasSprite sprite = spriteFinder.find(quad);
                    if (direction != opposite) {
                        meshEmitter.copyFrom(quad);
                        FabricBakedModelHelper.cropAndMove(meshEmitter, sprite, front, frontOffset);
                        meshEmitter.emit();
                        FabricBakedModelHelper.cropAndMove(quad, sprite, back, backOffset);
                    } else {
                        FabricBakedModelHelper.cropAndMove(quad, sprite, front, frontOffset);
                    }
                    return true;
                }
                if (direction != opposite) {
                    FabricBakedModelHelper.cropAndMove(quad, spriteFinder.find(quad), back, backOffset);
                    return true;
                }
                return false;
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
}
