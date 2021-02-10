#version 330 core
layout (location = 0) in vec2 inPosition;
layout (location = 1) in vec2 textureIndex;
layout (location = 2) in float textureLayer;

out vec3 passTextureCoordinates;


uniform mat4 projectionMatrix;

void main() {
    gl_Position = projectionMatrix * vec4(inPosition, 0.0f, 1.0f);
    passTextureCoordinates = vec3(textureIndex, textureLayer);
}
