#version 330 core
layout (location = 0) in vec3 inPosition;
layout (location = 1) in float textureIndex;
layout (location = 2) in float textureLayer;

out vec3 vertexColor;
out vec3 passTextureCoordinates;


uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;
uniform vec3 chunkPosition;

vec2 textureIndexCoordinates[4] = vec2[4](
vec2(0.0f, 0.0f),
vec2(1.0f, 0.0f),
vec2(1.0f, 1.0f),
vec2(0.0f, 1.0f)
);

void main() {
    gl_Position = projectionMatrix * viewMatrix *  vec4(inPosition + vec3(chunkPosition.x * 16u, chunkPosition.y * 16u, chunkPosition.z * 16u), 1.0f);
    passTextureCoordinates = vec3(textureIndexCoordinates[int(textureIndex)], textureLayer);
}
