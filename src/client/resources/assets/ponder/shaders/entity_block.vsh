#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>
#moj_import <minecraft:sample_lightmap.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
#if defined(OVERWORLD) || defined(NETHER)
in ivec2 UV1;
#endif
in ivec2 UV2;
#if defined(OVERWORLD) || defined(NETHER)
in vec3 Normal;
#endif

uniform sampler2D Sampler2;

out float sphericalVertexDistance;
out float cylindricalVertexDistance;
out vec4 vertexColor;
out vec2 texCoord0;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    sphericalVertexDistance = fog_spherical_distance(Position);
    cylindricalVertexDistance = fog_cylindrical_distance(Position);
    vertexColor = Color * sample_lightmap(Sampler2, UV2);
#ifdef NETHER_LIGHT
    vertexColor.rgb *= 0.9;
#elif defined(OVERWORLD)
    vec3 normal = normalize(Normal);
    vec3 n2 = normal * normal * vec3(0.6, 0.25, 0.8);
    vertexColor.rgb *= min(n2.x + n2.y * (3.0 + normal.y) + n2.z, 1.0);
#elif defined(NETHER)
    vec3 normal = normalize(Normal);
    vec3 n2 = normal * normal * vec3(0.6, 0.9, 0.8);
    vertexColor.rgb *= min(n2.x + n2.y + n2.z, 1.0);
#endif
    texCoord0 = UV0;
}
