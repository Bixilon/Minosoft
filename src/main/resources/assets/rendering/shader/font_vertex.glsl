#version 330 core
layout (location = 0) in vec3 inPosition;
layout (location = 1) in vec2 textureIndex;
layout (location = 2) in uint textureLayer;
layout (location = 3) in uint charColor;

out vec3 passTextureCoordinates;

out vec4 passCharColor;

void main() {
    gl_Position = vec4(inPosition.xyz, 1.0f);
    passTextureCoordinates = vec3(textureIndex, textureLayer);
    // uncompact array: 8 bit each for red, gree, blue, alpha
    passCharColor = vec4(((charColor >> 24u) & 0xFFu) / 255.0f, ((charColor >> 16u) & 0xFFu) / 255.0f, ((charColor >> 8u) & 0xFFu) / 255.0f, (charColor & 0xFFu) / 255.0f);
}
