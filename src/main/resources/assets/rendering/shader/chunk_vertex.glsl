#version 330 core
layout (location = 0) in vec3 inPosition;
layout (location = 1) in vec2 textureIndex;
layout (location = 2) in int textureLayer;

out vec3 passTextureCoordinates;

uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;
uniform int currentTick;

const int TEXTURE_COUNT = 1024;
uniform int[TEXTURE_COUNT] animatedTextureFrameTimes;// ToDo: Support frames more textures
uniform int[TEXTURE_COUNT] animatedTextureAnimations;
uniform float[TEXTURE_COUNT] textureSingleSizeY;

void main() {
    gl_Position = projectionMatrix * viewMatrix *  vec4(inPosition, 1.0f);
    int textureAnimations = animatedTextureAnimations[textureLayer];
    if (textureAnimations == 1) {
        passTextureCoordinates = vec3(textureIndex, textureLayer);
        return;
    }

    int frameTime = animatedTextureFrameTimes[textureLayer];
    passTextureCoordinates = vec3(textureIndex.x, textureIndex.y + (textureSingleSizeY[textureLayer] * ((currentTick / frameTime) % textureAnimations)), textureLayer);
}
