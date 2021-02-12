#version 330 core
layout (location = 0) in vec2 inPosition;
layout (location = 1) in vec2 textureIndex;
layout (location = 2) in float textureLayer;
layout (location = 3) in vec3 charColor;

out vec3 passTextureCoordinates;

out vec3 passCharColor;

void main() {
    gl_Position = vec4(inPosition, 0.0f, 1.0f);
    passTextureCoordinates = vec3(textureIndex, textureLayer);
    passCharColor = charColor;
}
