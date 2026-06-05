package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.zurrtum.create.content.decoration.copycat.CopycatBlock;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelLighter;
import net.minecraft.client.renderer.block.BlockModelLighter.AdjacencyInfo;
import net.minecraft.client.renderer.block.BlockModelLighter.AmbientVertexRemap;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.block.state.BlockState;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockModelLighter.class)
public class BlockModelLighterMixin {
    @Shadow
    @Final
    private MutableBlockPos scratchPos;

    @Shadow
    private boolean faceCubic;

    @Shadow
    @Final
    private BlockModelLighter.Cache cache;

    @Shadow
    private boolean facePartial;

    @Shadow
    @Final
    private float[] faceShape;

    @Inject(method = "prepareQuadAmbientOcclusion(Lnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/client/resources/model/geometry/BakedQuad;Lcom/mojang/blaze3d/vertex/QuadInstance;)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/block/BlockModelLighter;faceCubic:Z", opcode = Opcodes.GETFIELD, ordinal = 0), cancellable = true)
    private void smooth(
        BlockAndTintGetter level,
        BlockState state,
        BlockPos centerPosition,
        BakedQuad quad,
        QuadInstance outputInstance,
        CallbackInfo ci,
        @Local Direction direction
    ) {
        if (!faceCubic) {
            return;
        }
        scratchPos.setWithOffset(centerPosition, direction);
        BlockState nextState = level.getBlockState(scratchPos);
        if (!(nextState.getBlock() instanceof CopycatBlock) || !nextState.emissiveRendering()) {
            return;
        }
        MutableBlockPos searchPos = scratchPos;
        BlockPos lightPos = centerPosition.relative(direction);
        AdjacencyInfo aoFace = AdjacencyInfo.fromFacing(direction);
        searchPos.setWithOffset(lightPos, aoFace.corners[0]);
        BlockState searchState = level.getBlockState(searchPos);
        int light0 = cache.getLightCoords(searchState, level, searchPos);
        float ao0 = cache.getShadeBrightness(searchState, level, searchPos);
        boolean isClear0 = !searchState.isViewBlocking(level, searchPos) || searchState.getLightDampening() == 0;
        searchPos.setWithOffset(lightPos, aoFace.corners[1]);
        searchState = level.getBlockState(searchPos);
        int light1 = cache.getLightCoords(searchState, level, searchPos);
        float ao1 = cache.getShadeBrightness(searchState, level, searchPos);
        boolean isClear1 = !searchState.isViewBlocking(level, searchPos) || searchState.getLightDampening() == 0;
        searchPos.setWithOffset(lightPos, aoFace.corners[2]);
        searchState = level.getBlockState(searchPos);
        int light2 = cache.getLightCoords(searchState, level, searchPos);
        float ao2 = cache.getShadeBrightness(searchState, level, searchPos);
        boolean isClear2 = !searchState.isViewBlocking(level, searchPos) || searchState.getLightDampening() == 0;
        searchPos.setWithOffset(lightPos, aoFace.corners[3]);
        searchState = level.getBlockState(searchPos);
        int light3 = cache.getLightCoords(searchState, level, searchPos);
        float ao3 = cache.getShadeBrightness(searchState, level, searchPos);
        boolean isClear3 = !searchState.isViewBlocking(level, searchPos) || searchState.getLightDampening() == 0;
        int cLight0, cLight1, cLight2, cLight3;
        float cAo0, cAo1, cAo2, cAo3;
        boolean cIsClear0, cIsClear1, cIsClear2, cIsClear3;
        if (!isClear2 && !isClear0) {
            cAo0 = ao0;
            cLight0 = light0;
            cIsClear0 = false;
        } else {
            searchPos.setWithOffset(lightPos, aoFace.corners[0]).move(aoFace.corners[2]);
            searchState = level.getBlockState(searchPos);
            cAo0 = cache.getShadeBrightness(searchState, level, searchPos);
            cLight0 = cache.getLightCoords(searchState, level, searchPos);
            cIsClear0 = !searchState.isViewBlocking(level, searchPos) || searchState.getLightDampening() == 0;
        }
        if (!isClear3 && !isClear0) {
            cAo1 = ao0;
            cLight1 = light0;
            cIsClear1 = false;
        } else {
            searchPos.setWithOffset(lightPos, aoFace.corners[0]).move(aoFace.corners[3]);
            searchState = level.getBlockState(searchPos);
            cAo1 = cache.getShadeBrightness(searchState, level, searchPos);
            cLight1 = cache.getLightCoords(searchState, level, searchPos);
            cIsClear1 = !searchState.isViewBlocking(level, searchPos) || searchState.getLightDampening() == 0;
        }
        if (!isClear2 && !isClear1) {
            cAo2 = ao1;
            cLight2 = light1;
            cIsClear2 = false;
        } else {
            searchPos.setWithOffset(lightPos, aoFace.corners[1]).move(aoFace.corners[2]);
            searchState = level.getBlockState(searchPos);
            cAo2 = cache.getShadeBrightness(searchState, level, searchPos);
            cLight2 = cache.getLightCoords(searchState, level, searchPos);
            cIsClear2 = !searchState.isViewBlocking(level, searchPos) || searchState.getLightDampening() == 0;
        }
        if (!isClear3 && !isClear1) {
            cAo3 = ao1;
            cLight3 = light1;
            cIsClear3 = false;
        } else {
            searchPos.setWithOffset(lightPos, aoFace.corners[1]).move(aoFace.corners[3]);
            searchState = level.getBlockState(searchPos);
            cAo3 = cache.getShadeBrightness(searchState, level, searchPos);
            cLight3 = cache.getLightCoords(searchState, level, searchPos);
            cIsClear3 = !searchState.isViewBlocking(level, searchPos) || searchState.getLightDampening() == 0;
        }
        float aoCenter = cache.getShadeBrightness(level.getBlockState(lightPos), level, lightPos);
        AmbientVertexRemap remap = AmbientVertexRemap.fromFacing(direction);
        float lightLevel1 = (ao3 + ao0 + cAo1 + aoCenter) * 0.25F;
        float lightLevel2 = (ao2 + ao0 + cAo0 + aoCenter) * 0.25F;
        float lightLevel3 = (ao2 + ao1 + cAo2 + aoCenter) * 0.25F;
        float lightLevel4 = (ao3 + ao1 + cAo3 + aoCenter) * 0.25F;
        if (facePartial && aoFace.doNonCubicWeight) {
            float vert0weight01 = faceShape[aoFace.vert0Weights[0].index] * faceShape[aoFace.vert0Weights[1].index];
            float vert0weight23 = faceShape[aoFace.vert0Weights[2].index] * faceShape[aoFace.vert0Weights[3].index];
            float vert0weight45 = faceShape[aoFace.vert0Weights[4].index] * faceShape[aoFace.vert0Weights[5].index];
            float vert0weight67 = faceShape[aoFace.vert0Weights[6].index] * faceShape[aoFace.vert0Weights[7].index];
            float vert1weight01 = faceShape[aoFace.vert1Weights[0].index] * faceShape[aoFace.vert1Weights[1].index];
            float vert1weight23 = faceShape[aoFace.vert1Weights[2].index] * faceShape[aoFace.vert1Weights[3].index];
            float vert1weight45 = faceShape[aoFace.vert1Weights[4].index] * faceShape[aoFace.vert1Weights[5].index];
            float vert1weight67 = faceShape[aoFace.vert1Weights[6].index] * faceShape[aoFace.vert1Weights[7].index];
            float vert2weight01 = faceShape[aoFace.vert2Weights[0].index] * faceShape[aoFace.vert2Weights[1].index];
            float vert2weight23 = faceShape[aoFace.vert2Weights[2].index] * faceShape[aoFace.vert2Weights[3].index];
            float vert2weight45 = faceShape[aoFace.vert2Weights[4].index] * faceShape[aoFace.vert2Weights[5].index];
            float vert2weight67 = faceShape[aoFace.vert2Weights[6].index] * faceShape[aoFace.vert2Weights[7].index];
            float vert3weight01 = faceShape[aoFace.vert3Weights[0].index] * faceShape[aoFace.vert3Weights[1].index];
            float vert3weight23 = faceShape[aoFace.vert3Weights[2].index] * faceShape[aoFace.vert3Weights[3].index];
            float vert3weight45 = faceShape[aoFace.vert3Weights[4].index] * faceShape[aoFace.vert3Weights[5].index];
            float vert3weight67 = faceShape[aoFace.vert3Weights[6].index] * faceShape[aoFace.vert3Weights[7].index];
            outputInstance.setColor(
                remap.vert0, ARGB.gray(Math.clamp(
                    lightLevel1 * vert0weight01 + lightLevel2 * vert0weight23 + lightLevel3 * vert0weight45 + lightLevel4 * vert0weight67,
                    0.0F,
                    1.0F
                ))
            );
            outputInstance.setColor(
                remap.vert1, ARGB.gray(Math.clamp(
                    lightLevel1 * vert1weight01 + lightLevel2 * vert1weight23 + lightLevel3 * vert1weight45 + lightLevel4 * vert1weight67,
                    0.0F,
                    1.0F
                ))
            );
            outputInstance.setColor(
                remap.vert2, ARGB.gray(Math.clamp(
                    lightLevel1 * vert2weight01 + lightLevel2 * vert2weight23 + lightLevel3 * vert2weight45 + lightLevel4 * vert2weight67,
                    0.0F,
                    1.0F
                ))
            );
            outputInstance.setColor(
                remap.vert3, ARGB.gray(Math.clamp(
                    lightLevel1 * vert3weight01 + lightLevel2 * vert3weight23 + lightLevel3 * vert3weight45 + lightLevel4 * vert3weight67,
                    0.0F,
                    1.0F
                ))
            );
            int _tc1 = smoothBlend(light3, light0, cLight1, isClear3, isClear0, cIsClear1);
            int _tc2 = smoothBlend(light2, light0, cLight0, isClear2, isClear0, cIsClear0);
            int _tc3 = smoothBlend(light2, light1, cLight2, isClear2, isClear1, cIsClear2);
            int _tc4 = smoothBlend(light3, light1, cLight3, isClear3, isClear1, cIsClear3);
            outputInstance.setLightCoords(
                remap.vert0,
                LightCoordsUtil.smoothWeightedBlend(
                    _tc1,
                    _tc2,
                    _tc3,
                    _tc4,
                    vert0weight01,
                    vert0weight23,
                    vert0weight45,
                    vert0weight67
                )
            );
            outputInstance.setLightCoords(
                remap.vert1,
                LightCoordsUtil.smoothWeightedBlend(
                    _tc1,
                    _tc2,
                    _tc3,
                    _tc4,
                    vert1weight01,
                    vert1weight23,
                    vert1weight45,
                    vert1weight67
                )
            );
            outputInstance.setLightCoords(
                remap.vert2,
                LightCoordsUtil.smoothWeightedBlend(
                    _tc1,
                    _tc2,
                    _tc3,
                    _tc4,
                    vert2weight01,
                    vert2weight23,
                    vert2weight45,
                    vert2weight67
                )
            );
            outputInstance.setLightCoords(
                remap.vert3,
                LightCoordsUtil.smoothWeightedBlend(
                    _tc1,
                    _tc2,
                    _tc3,
                    _tc4,
                    vert3weight01,
                    vert3weight23,
                    vert3weight45,
                    vert3weight67
                )
            );
        } else {
            outputInstance.setLightCoords(
                remap.vert0,
                smoothBlend(light3, light0, cLight1, isClear3, isClear0, cIsClear1)
            );
            outputInstance.setLightCoords(
                remap.vert1,
                smoothBlend(light2, light0, cLight0, isClear2, isClear0, cIsClear0)
            );
            outputInstance.setLightCoords(
                remap.vert2,
                smoothBlend(light2, light1, cLight2, isClear2, isClear1, cIsClear2)
            );
            outputInstance.setLightCoords(
                remap.vert3,
                smoothBlend(light3, light1, cLight3, isClear3, isClear1, cIsClear3)
            );
            outputInstance.setColor(remap.vert0, ARGB.gray(lightLevel1));
            outputInstance.setColor(remap.vert1, ARGB.gray(lightLevel2));
            outputInstance.setColor(remap.vert2, ARGB.gray(lightLevel3));
            outputInstance.setColor(remap.vert3, ARGB.gray(lightLevel4));
        }
        CardinalLighting cardinalLighting = level.cardinalLighting();
        outputInstance.scaleColor(
            quad.materialInfo().shade() ? cardinalLighting.byFace(direction) : cardinalLighting.up());
        ci.cancel();
    }

    @Unique
    private static int smoothBlend(
        int lightA,
        int lightB,
        int lightC,
        boolean isClearA,
        boolean isClearB,
        boolean isClearC
    ) {
        int lightABlock = lightA & 0xFFFF;
        int lightASky = lightA >>> 16 & 0xFFFF;
        int lightBBlock = lightB & 0xFFFF;
        int lightBSky = lightB >>> 16 & 0xFFFF;
        int lightCBlock = lightC & 0xFFFF;
        int lightCSky = lightC >>> 16 & 0xFFFF;
        int minBlock = 0x10000;
        int minSky = 0x10000;
        if (isClearA) {
            minBlock = lightABlock;
            minSky = lightASky;
        }
        if (isClearB) {
            minBlock = Math.min(minBlock, lightBBlock);
            minSky = Math.min(minSky, lightBSky);
        }
        if (isClearC) {
            minBlock = Math.min(minBlock, lightCBlock);
            minSky = Math.min(minSky, lightCSky);
        }
        minBlock &= 0xFFFF;
        minSky &= 0xFFFF;
        lightA = Math.max(lightASky, minSky) << 16 | Math.max(lightABlock, minBlock);
        lightB = Math.max(lightBSky, minSky) << 16 | Math.max(lightBBlock, minBlock);
        lightC = Math.max(lightCSky, minSky) << 16 | Math.max(lightCBlock, minBlock);
        return lightA + lightB + lightC + LightCoordsUtil.FULL_BRIGHT >> 2 & 0xFF00FF;
    }
}
