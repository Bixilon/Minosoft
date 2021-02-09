#version 330 core

out vec4 outColor;

in vec3 passTextureCoordinates;

uniform sampler2DArray texureArray;


void main() {
    vec4 textureColor = texture(texureArray, passTextureCoordinates);
    if (textureColor.a == 0) {
        textureColor.a = 0.4f;
        //discard;
    }
    textureColor.r = 0.8f;
    textureColor.g = 0.8f;
    textureColor.b = 0.0f;
    outColor = textureColor;
}
