package com.zurrtum.create.client.catnip.render;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public enum EntityBlockRenderType {
    SOLID(
        Sheets.cutoutBlockItemSheet(),
        PonderRenderTypes.getEntityBlockSolid(),
        PonderRenderTypes.getNetherEntityBlockSolid()
    ), CUTOUT(
        Sheets.cutoutBlockItemSheet(),
        PonderRenderTypes.getEntityBlockCutout(),
        PonderRenderTypes.getNetherEntityBlockCutout()
    ), TRANSLUCENT(
        Sheets.translucentBlockItemSheet(),
        PonderRenderTypes.getEntityBlockTranslucent(),
        PonderRenderTypes.getNetherEntityBlockTranslucent()
    ), SOLID_LIGHT(
        RenderTypes.solidMovingBlock(),
        PonderRenderTypes.getEntityBlockLightSolid(),
        PonderRenderTypes.getNetherEntityBlockLightSolid()
    ), CUTOUT_LIGHT(
        RenderTypes.cutoutMovingBlock(),
        PonderRenderTypes.getEntityBlockLightCutout(),
        PonderRenderTypes.getNetherEntityBlockLightCutout()
    ), TRANSLUCENT_LIGHT(
        RenderTypes.translucentMovingBlock(),
        PonderRenderTypes.getEntityBlockLightTranslucent(),
        PonderRenderTypes.getNetherEntityBlockLightTranslucent()
    );
    private static final EntityBlockRenderType[] VALUES = values();
    private final RenderType type;
    private final RenderType overworld;
    private final RenderType nether;

    public static EntityBlockRenderType from(int index) {
        return VALUES[index];
    }

    EntityBlockRenderType(RenderType type, RenderType overworld, RenderType nether) {
        this.type = type;
        this.overworld = overworld;
        this.nether = nether;
    }

    public RenderType getRenderType(int cardinalLighting) {
        return switch (cardinalLighting) {
            case 1 -> overworld;
            case 2 -> nether;
            default -> type;
        };
    }

    public RenderType getLightRenderType(int cardinalLighting) {
        return switch (this) {
            case SOLID -> SOLID_LIGHT.getRenderType(cardinalLighting);
            case CUTOUT -> CUTOUT_LIGHT.getRenderType(cardinalLighting);
            case TRANSLUCENT -> TRANSLUCENT_LIGHT.getRenderType(cardinalLighting);
            default -> getRenderType(cardinalLighting);
        };
    }

    public boolean isLight() {
        return switch (this) {
            case SOLID_LIGHT, CUTOUT_LIGHT, TRANSLUCENT_LIGHT -> true;
            default -> false;
        };
    }
}
