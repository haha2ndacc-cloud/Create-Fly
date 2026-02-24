package com.zurrtum.create.client.catnip.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import static com.zurrtum.create.client.ponder.Ponder.MOD_ID;
import static net.minecraft.client.renderer.RenderPipelines.MATRICES_PROJECTION_SNIPPET;

public class PonderRenderPipelines {
    public static final DepthStencilState DEFAULT_TEST_NOT_WRITE = new DepthStencilState(
        CompareOp.LESS_THAN_OR_EQUAL,
        false
    );
    public static final RenderPipeline GUI = register(
        "gui",
        RenderPipeline.builder(RenderPipelines.GUI_SNIPPET).withDepthStencilState(DEFAULT_TEST_NOT_WRITE)
    );
    public static final RenderPipeline ENTITY_TRANSLUCENT_CULL = register(
        "entity_translucent_cull",
        RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET).withShaderDefine("ALPHA_CUTOUT", 0.1F)
            .withSampler("Sampler1").withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(DEFAULT_TEST_NOT_WRITE)
    );
    public static final RenderPipeline ENTITY_TRANSLUCENT = register(
        "entity_translucent",
        RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET).withShaderDefine("ALPHA_CUTOUT", 0.1F)
            .withSampler("Sampler1").withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withCull(false).withDepthStencilState(DEFAULT_TEST_NOT_WRITE)
    );
    public static final RenderPipeline TRIANGLE_FAN = wrapSequential(register(
        "triangle_fan",
        RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_FAN)
    ));
    public static final RenderPipeline POSITION_COLOR_TRIANGLES = wrapSequential(register(
        "position_color_triangles",
        RenderPipeline.builder(MATRICES_PROJECTION_SNIPPET).withVertexShader("core/position_color")
            .withFragmentShader("core/position_color")
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES).withCull(false)
    ));
    public static final RenderPipeline POSITION_COLOR_STRIP = wrapSequential(register(
        "position_color_strip",
        RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_STRIP)
    ));

    public static RenderPipeline wrapSequential(RenderPipeline pipeline) {
        ((CustomRenderPipeline) pipeline).create$updateSequential();
        return pipeline;
    }

    private static RenderPipeline register(String id, RenderPipeline.Builder builder) {
        Identifier location = Identifier.fromNamespaceAndPath(MOD_ID, "pipeline/" + id);
        RenderPipeline pipeline = builder.withLocation(location).build();
        RenderPipelines.PIPELINES_BY_LOCATION.put(location, pipeline);
        return pipeline;
    }
}
