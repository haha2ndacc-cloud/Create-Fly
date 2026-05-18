package com.zurrtum.create.client.catnip.render;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndLightGetter;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedTransferQueue;

public class SuperByteBufferThread extends Thread {
    private final SpriteShiftInstance spriteShiftInstance = new SpriteShiftInstance();
    private final ColorMultiplyInstance colorMultiplyInstance = new ColorMultiplyInstance();
    private final LightMixInstance lightMixInstance = new LightMixInstance();
    private final LinkedTransferQueue<SuperByteBufferTask> queue;
    private final ConcurrentLinkedQueue<SuperByteBufferTask> pool;

    public SuperByteBufferThread(
        int i,
        LinkedTransferQueue<SuperByteBufferTask> queue,
        ConcurrentLinkedQueue<SuperByteBufferTask> pool
    ) {
        super("SuperByteBuffer #" + i);
        this.queue = queue;
        this.pool = pool;
    }

    private SuperByteBufferTask spinThenTake() throws InterruptedException {
        long spinTime = System.nanoTime() + 10_000;
        do {
            SuperByteBufferTask state = queue.poll();
            if (state != null) {
                return state;
            }
            onSpinWait();
        } while (System.nanoTime() <= spinTime);
        return queue.take();
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public void run() {
        while (true) {
            try {
                SuperByteBufferTask task = queue.poll();
                if (task == null) {
                    task = spinThenTake();
                }
                AbstractEntityBlockLayer layer = task.layer;
                EntityBlockTemplateMesh template = layer.template;
                int vertexCount = template.vertexCount;
                layer.positions = template.positions;
                int flag = task.flag;
                switch (flag & 0b11111) {
                    case 0b00001 -> {
                        colorMultiplyInstance.update(task);
                        layer.uvs = template.uvs;
                        layer.lights = template.lights;
                        int[] templateColors = template.colors;
                        int[] colors = layer.colors = template.createColors();
                        for (int j = 0; j < vertexCount; j++) {
                            colorMultiplyInstance.multiply(templateColors, colors, j);
                        }
                    }
                    case 0b00010, 0b00100, 0b00110 -> {
                        var shiftFunction = spriteShiftInstance.update(task, flag);
                        layer.colors = template.colors;
                        layer.lights = template.lights;
                        float[] templateUvs = template.uvs;
                        float[] uvs = layer.uvs = template.createUVs();
                        for (int j = 0; j < vertexCount; j++) {
                            shiftFunction.shift(templateUvs, uvs, j << 1);
                        }
                    }
                    case 0b01000, 0b10000, 0b11000 -> {
                        var lightFunction = lightMixInstance.update(task, flag, layer, template);
                        layer.colors = template.colors;
                        layer.uvs = template.uvs;
                        int[] templateLights = template.lights;
                        int[] lights = layer.lights = template.createLights();
                        for (int j = 0; j < vertexCount; j++) {
                            lightFunction.mix(templateLights, lights, j);
                        }
                        lightMixInstance.clear();
                    }
                    case 0b00011, 0b00101, 0b00111 -> {
                        colorMultiplyInstance.update(task);
                        var shiftFunction = spriteShiftInstance.update(task, flag);
                        layer.lights = template.lights;
                        int[] templateColors = template.colors;
                        float[] templateUvs = template.uvs;
                        int[] colors = layer.colors = template.createColors();
                        float[] uvs = layer.uvs = template.createUVs();
                        for (int j = 0; j < vertexCount; j++) {
                            colorMultiplyInstance.multiply(templateColors, colors, j);
                            shiftFunction.shift(templateUvs, uvs, j << 1);
                        }
                    }
                    case 0b01001, 0b10001, 0b11001 -> {
                        colorMultiplyInstance.update(task);
                        var lightFunction = lightMixInstance.update(task, flag, layer, template);
                        layer.uvs = template.uvs;
                        int[] templateColors = template.colors;
                        int[] templateLights = template.lights;
                        int[] colors = layer.colors = template.createColors();
                        int[] lights = layer.lights = template.createLights();
                        for (int j = 0; j < vertexCount; j++) {
                            colorMultiplyInstance.multiply(templateColors, colors, j);
                            lightFunction.mix(templateLights, lights, j);
                        }
                        lightMixInstance.clear();
                    }
                    case 0b01010, 0b01100, 0b01110, 0b10010, 0b10100, 0b10110, 0b11010, 0b11100, 0b11110 -> {
                        var shiftFunction = spriteShiftInstance.update(task, flag);
                        var lightFunction = lightMixInstance.update(task, flag, layer, template);
                        layer.colors = template.colors;
                        float[] templateUvs = template.uvs;
                        int[] templateLights = template.lights;
                        float[] uvs = layer.uvs = template.createUVs();
                        int[] lights = layer.lights = template.createLights();
                        for (int j = 0; j < vertexCount; j++) {
                            shiftFunction.shift(templateUvs, uvs, j << 1);
                            lightFunction.mix(templateLights, lights, j);
                        }
                        lightMixInstance.clear();
                    }
                    case 0b01011, 0b01101, 0b01111, 0b10011, 0b10101, 0b10111, 0b11011, 0b11101, 0b11111 -> {
                        colorMultiplyInstance.update(task);
                        var shiftFunction = spriteShiftInstance.update(task, flag);
                        var lightFunction = lightMixInstance.update(task, flag, layer, template);
                        int[] templateColors = template.colors;
                        float[] templateUvs = template.uvs;
                        int[] templateLights = template.lights;
                        int[] colors = layer.colors = template.createColors();
                        float[] uvs = layer.uvs = template.createUVs();
                        int[] lights = layer.lights = template.createLights();
                        for (int j = 0; j < vertexCount; j++) {
                            colorMultiplyInstance.multiply(templateColors, colors, j);
                            shiftFunction.shift(templateUvs, uvs, j << 1);
                            lightFunction.mix(templateLights, lights, j);
                        }
                        lightMixInstance.clear();
                    }
                    default -> throw new IllegalArgumentException("Invalid flag: " + flag);
                }
                layer.future.complete(null);
                task.reset();
                pool.offer(task);
            } catch (InterruptedException e) {
                currentThread().interrupt();
                break;
            } catch (Exception e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
            }
        }
    }

    private static class ColorMultiplyInstance {
        int defaultColor = -1;
        int r, g, b, a;

        public void update(SuperByteBufferTask task) {
            defaultColor = task.color;
            r = ARGB.red(defaultColor);
            g = ARGB.green(defaultColor);
            b = ARGB.blue(defaultColor);
            a = ARGB.alpha(defaultColor);
        }

        public void multiply(int[] src, int[] dest, int index) {
            int color = src[index];
            dest[index] = color == -1 ? defaultColor : ARGB.color(
                ARGB.alpha(color) * a / 255,
                ARGB.red(color) * r / 255,
                ARGB.green(color) * g / 255,
                ARGB.blue(color) * b / 255
            );
        }
    }

    private static class SpriteShiftInstance {
        private final ShiftFunction simple = new ShiftFunction();
        private final ComplexShiftFunction complex = new ComplexShiftFunction();
        protected float offsetU, offsetV;
        private float diffU, diffV;

        public ShiftFunction update(SuperByteBufferTask task, int flag) {
            return switch (flag & 0b110) {
                case 0b010 -> shiftUVScrolling(task.shiftEntry, task.shiftU, task.shiftV);
                case 0b100 -> shiftUV(task.shiftEntry);
                case 0b110 -> shiftUVtoSheet(task.shiftEntry, task.shiftU, task.shiftV, task.sheetSize);
                default -> throw new IllegalArgumentException("Invalid flag: " + flag);
            };
        }

        private class ShiftFunction {
            public void shift(float[] src, float[] dest, int index) {
                dest[index] = src[index] + offsetU;
                dest[index + 1] = src[index + 1] + offsetV;
            }
        }

        private class ComplexShiftFunction extends ShiftFunction {
            @Override
            public void shift(float[] src, float[] dest, int index) {
                dest[index] = src[index] * diffU + offsetU;
                dest[index + 1] = src[index + 1] * diffV + offsetV;
            }
        }

        public ShiftFunction shiftUVScrolling(SpriteShiftEntry entry, float scrollU, float scrollV) {
            TextureAtlasSprite original = entry.getOriginal();
            TextureAtlasSprite target = entry.getTarget();
            offsetU = scrollU - original.getU0() + target.getU0();
            offsetV = scrollV - original.getV0() + target.getV0();
            return simple;
        }

        public ShiftFunction shiftUV(SpriteShiftEntry entry) {
            TextureAtlasSprite original = entry.getOriginal();
            TextureAtlasSprite target = entry.getTarget();
            float originalU0 = original.getU0();
            float originalV0 = original.getV0();
            float targetU0 = target.getU0();
            float targetV0 = target.getV0();
            diffU = (target.getU1() - targetU0) / (original.getU1() - originalU0);
            diffV = (target.getV1() - targetV0) / (original.getV1() - originalV0);
            offsetU = targetU0 - originalU0 * diffU;
            offsetV = targetV0 - originalV0 * diffV;
            return complex;
        }

        public ShiftFunction shiftUVtoSheet(SpriteShiftEntry entry, float uTarget, float vTarget, int sheetSize) {
            TextureAtlasSprite original = entry.getOriginal();
            TextureAtlasSprite target = entry.getTarget();
            float originalU0 = original.getU0();
            float originalV0 = original.getV0();
            float targetU0 = target.getU0();
            float targetV0 = target.getV0();
            float targetUDiff = target.getU1() - targetU0;
            float targetVDiff = target.getV1() - targetV0;
            diffU = targetUDiff / (original.getU1() - originalU0) / sheetSize;
            diffV = targetVDiff / (original.getV1() - originalV0) / sheetSize;
            offsetU = targetU0 + uTarget * targetUDiff - originalU0 * diffU;
            offsetV = targetV0 + vTarget * targetVDiff - originalV0 * diffV;
            return complex;
        }
    }

    private static class LightMixInstance {
        private final Long2IntOpenHashMap cache = new Long2IntOpenHashMap();
        private final CoverLightFunction cover = new CoverLightFunction();
        private final LevelLightFunction level = new LevelLightFunction();
        private final ComplexFunction complex = new ComplexFunction();
        private int blockLight, skyLight;
        protected @UnknownNullability BlockAndLightGetter blockAndLightGetter;
        protected @UnknownNullability Vector4fc[] lightPositions;
        protected Matrix4f lightTransform = new Matrix4f();
        protected Vector4f lightPos = new Vector4f();
        protected MutableBlockPos blockPos = new MutableBlockPos();

        private static class LightFunction {
            public static final LightFunction FULL_BRIGHT = new LightFunction();

            public void mix(int[] templateLights, int[] lights, int j) {
                lights[j] = LightCoordsUtil.FULL_BRIGHT;
            }
        }

        private class CoverLightFunction extends LightFunction {
            @Override
            public void mix(int[] templateLights, int[] lights, int j) {
                int templateLight = templateLights[j];
                int templateBlock = LightCoordsUtil.block(templateLight);
                int templateSky = LightCoordsUtil.sky(templateLight);
                lights[j] = LightCoordsUtil.pack(Math.max(blockLight, templateBlock), Math.max(skyLight, templateSky));
            }
        }

        private class LevelLightFunction extends LightFunction {
            @Override
            public void mix(int[] templateLights, int[] lights, int j) {
                lightPositions[j].mul(lightTransform, lightPos);
                blockPos.set(Mth.floor(lightPos.x()), Mth.floor(lightPos.y()), Mth.floor(lightPos.z()));
                lights[j] = LightCoordsUtil.max(
                    cache.computeIfAbsent(
                        blockPos.asLong(),
                        LightMixInstance.this::getLevelLight
                    ), templateLights[j]
                );
            }
        }

        private class ComplexFunction extends LevelLightFunction {
            @Override
            public void mix(int[] templateLights, int[] lights, int j) {
                int templateLight = templateLights[j];
                int templateBlock = LightCoordsUtil.block(templateLight);
                int templateSky = LightCoordsUtil.sky(templateLight);
                lightPositions[j].mul(lightTransform, lightPos);
                blockPos.set(Mth.floor(lightPos.x()), Mth.floor(lightPos.y()), Mth.floor(lightPos.z()));
                int levelLight = cache.computeIfAbsent(blockPos.asLong(), LightMixInstance.this::getLevelLight);
                int levelBlock = LightCoordsUtil.block(levelLight);
                int levelSky = LightCoordsUtil.sky(levelLight);
                lights[j] = LightCoordsUtil.pack(
                    Math.max(Math.max(levelBlock, blockLight), templateBlock),
                    Math.max(Math.max(levelSky, skyLight), templateSky)
                );
            }
        }

        private int getLevelLight(long pos) {
            return LightCoordsUtil.getLightCoords(blockAndLightGetter, blockPos);
        }

        public LightFunction update(
            SuperByteBufferTask task,
            int flag,
            AbstractEntityBlockLayer layer,
            EntityBlockTemplateMesh template
        ) {
            return switch (flag & 0b11000) {
                case 0b01000 -> light(task.packedLight);
                case 0b10000 -> light(task.blockAndLightGetter, layer.pose(), task.lightTransform, template);
                case 0b11000 ->
                    light(task.packedLight, task.blockAndLightGetter, layer.pose(), task.lightTransform, template);
                default -> throw new IllegalArgumentException("Invalid flag: " + flag);
            };
        }

        public LightFunction light(int packedLight) {
            if (packedLight == LightCoordsUtil.FULL_BRIGHT) {
                return LightFunction.FULL_BRIGHT;
            }
            blockLight = LightCoordsUtil.block(packedLight);
            skyLight = LightCoordsUtil.sky(packedLight);
            return cover;
        }

        public LightFunction light(
            BlockAndLightGetter blockAndLightGetter,
            Matrix4fc pose,
            @Nullable Matrix4fc lightTransform,
            EntityBlockTemplateMesh template
        ) {
            this.blockAndLightGetter = blockAndLightGetter;
            if (lightTransform == null) {
                this.lightTransform.set(pose);
            } else {
                lightTransform.mul(pose, this.lightTransform);
            }
            lightPositions = template.getLightPositions();
            return level;
        }

        public LightFunction light(
            int packedLight,
            BlockAndLightGetter blockAndLightGetter,
            Matrix4fc pose,
            @Nullable Matrix4fc lightTransform,
            EntityBlockTemplateMesh template
        ) {
            if (packedLight == LightCoordsUtil.FULL_BRIGHT) {
                return LightFunction.FULL_BRIGHT;
            }
            blockLight = LightCoordsUtil.block(packedLight);
            skyLight = LightCoordsUtil.sky(packedLight);
            this.blockAndLightGetter = blockAndLightGetter;
            if (lightTransform == null) {
                this.lightTransform.set(pose);
            } else {
                lightTransform.mul(pose, this.lightTransform);
            }
            lightPositions = template.getLightPositions();
            return complex;
        }

        public void clear() {
            if (blockAndLightGetter != null) {
                blockAndLightGetter = null;
                cache.clear();
            }
        }
    }
}
