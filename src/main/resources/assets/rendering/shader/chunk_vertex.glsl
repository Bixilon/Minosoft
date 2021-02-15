#version 330 core
layout (location = 0) in vec3 inPosition;
layout (location = 1) in vec2 textureIndex;
layout (location = 2) in float textureLayer;

out vec3 passTextureCoordinates;


uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;

void main() {
    gl_Position = projectionMatrix * viewMatrix *  vec4(inPosition, 1.0f);
    passTextureCoordinates = vec3(textureIndex, textureLayer);
}
