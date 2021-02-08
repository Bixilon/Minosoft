#version 330 core

out vec4 outColor;

in vec3 passTextureCoordinates;

uniform sampler2DArray texureArray;


void main() {
    vec4 texColor = texture(texureArray, passTextureCoordinates);
    if (texColor.a == 0) {
        discard;
    }
    texColor.r = 0.8f;
    texColor.g = 0.8f;
    texColor.b = 0.0f;
    outColor = texColor;
}
