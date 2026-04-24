package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.infrastructure.model.SymmetryWandModel;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.render.FabricLayerRenderState;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.function.Supplier;

@Mixin(SymmetryWandModel.class)
public abstract class SymmetryWandModelMixin implements SpecialModelRenderer<ItemDisplayContext> {
    @Shadow
    @Final
    private Supplier<Vector3fc[]> vector;

    @Shadow
    @Final
    private Matrix4fc transformation;

    @Shadow
    @Final
    private ModelRenderProperties settings;

    @Shadow
    @Final
    private List<BakedQuad> item;

    @Shadow
    @Final
    private List<BakedQuad> core;

    @Shadow
    @Final
    private List<BakedQuad> coreGlow;

    @Shadow
    @Final
    private List<BakedQuad> bits;

    @Overwrite(remap = false)
    public void update(
        ItemStackRenderState state,
        ItemStack stack,
        ItemModelResolver resolver,
        ItemDisplayContext displayContext,
        @Nullable ClientLevel world,
        @Nullable ItemOwner user,
        int seed
    ) {
        state.appendModelIdentityElement(this);
        state.setAnimated();
        ItemStackRenderState.LayerRenderState renderState = state.newLayer();
        renderState.setExtents(vector);
        renderState.setLocalTransform(transformation);
        settings.applyToLayer(renderState, displayContext);
        QuadEmitter emitter = ((FabricLayerRenderState) renderState).emitter();
        for (BakedQuad quad : item) {
            emitter.fromBakedQuad(quad);
            emitter.emit();
        }
        int maxLight = displayContext == ItemDisplayContext.GUI ? 0 : LightCoordsUtil.FULL_BRIGHT;
        for (BakedQuad quad : core) {
            emitter.fromBakedQuad(quad);
            emitter.lightmap(maxLight, maxLight, maxLight, maxLight);
            emitter.emit();
        }
        for (BakedQuad quad : coreGlow) {
            emitter.fromBakedQuad(quad);
            emitter.lightmap(maxLight, maxLight, maxLight, maxLight);
            emitter.emit();
        }
        for (BakedQuad quad : bits) {
            emitter.fromBakedQuad(quad);
            emitter.emit();
        }
    }
}
