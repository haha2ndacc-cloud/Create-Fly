/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package com.zurrtum.create.client.model.obj;

import com.mojang.blaze3d.platform.Transparency;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.foundation.model.BakedModelHelper;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker.Interner;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * Vertex consumer that outputs {@linkplain BakedQuad baked quads}.
 * <p>
 * This consumer accepts data in {@link com.mojang.blaze3d.vertex.DefaultVertexFormat#BLOCK} and is not picky about
 * ordering or missing elements, but will not automatically populate missing data (color will be black, for example).
 * <p>
 * Built quads must be retrieved after building four vertices
 */
public class QuadBakingVertexConsumer implements VertexConsumer {
    private final Vector3f[] positions = new Vector3f[4];
    private final long[] uvs = new long[4];
    private final Vector3f[] normals = new Vector3f[4];
    private int vertexIndex;
    private boolean building;

    private int tintIndex = -1;
    private Direction direction = Direction.DOWN;
    @Nullable
    private TextureAtlasSprite sprite;
    @Nullable
    private ChunkSectionLayer chunkLayer;
    @Nullable
    private RenderType itemRenderType;
    private boolean shade = true;
    private int lightEmission;

    public QuadBakingVertexConsumer() {
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        if (building) {
            if (++vertexIndex > 4) {
                throw new IllegalStateException("Expected quad export after fourth vertex");
            }
        }
        building = true;

        positions[vertexIndex] = new Vector3f(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        normals[vertexIndex] = new Vector3f(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer setColor(int packedColor) {
        return this;
    }

    @Override
    public VertexConsumer setColor(int r, int g, int b, int a) {
        return this;
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        uvs[vertexIndex] = UVPair.pack(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        return this;
    }

    @Override
    public VertexConsumer setLineWidth(float lineWidth) {
        return this;
    }

    public void setTintIndex(int tintIndex) {
        this.tintIndex = tintIndex;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    @SuppressWarnings("deprecation")
    public void setSprite(TextureAtlasSprite texture, Transparency transparency) {
        RenderType itemRenderType;
        if (texture.atlasLocation().equals(TextureAtlas.LOCATION_BLOCKS)) {
            itemRenderType =
                transparency.hasTranslucent() ? Sheets.translucentBlockItemSheet() : Sheets.cutoutBlockItemSheet();
        } else {
            itemRenderType = transparency.hasTranslucent() ? Sheets.translucentItemSheet() : Sheets.cutoutItemSheet();
        }
        setSprite(texture, ChunkSectionLayer.byTransparency(transparency), itemRenderType);
    }

    public void setSprite(TextureAtlasSprite sprite, ChunkSectionLayer chunkLayer, RenderType itemRenderType) {
        this.sprite = sprite;
        this.chunkLayer = chunkLayer;
        this.itemRenderType = itemRenderType;
    }

    public void setShade(boolean shade) {
        this.shade = shade;
    }

    public void setLightEmission(int lightEmission) {
        this.lightEmission = lightEmission;
    }

    public BakedQuad bakeQuad(Interner interner) {
        if (!building || ++vertexIndex != 4) {
            throw new IllegalStateException("Not enough vertices available. Vertices in buffer: " + vertexIndex);
        }
        if (sprite == null) {
            throw new IllegalStateException("No sprite set");
        }
        if (chunkLayer == null) {
            throw new IllegalStateException("No ChunkSectionLayer set");
        }
        if (itemRenderType == null) {
            throw new IllegalStateException("No item RenderType set");
        }

        BakedQuad.MaterialInfo materialInfo = new BakedQuad.MaterialInfo(
            sprite,
            chunkLayer,
            itemRenderType,
            tintIndex,
            shade,
            lightEmission
        );
        BakedQuad quad = new BakedQuad(
            positions[0],
            positions[1],
            positions[2],
            positions[3],
            uvs[0],
            uvs[1],
            uvs[2],
            uvs[3],
            direction,
            interner.materialInfo(materialInfo)
        );
        BakedModelHelper.setNormals(quad, normals);
        return quad;
    }
}
