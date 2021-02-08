#version 330 core
layout (location = 0) in vec2 inPosition;
layout (location = 1) in vec2 textureIndex;
layout (location = 2) in float textureLayer;

out vec3 passTextureCoordinates;

uniform float atlasSize;

void main() {
    gl_Position = vec4(inPosition, 0.0f, 1.0f);
    passTextureCoordinates = vec3(textureIndex / atlasSize, textureLayer);
}
