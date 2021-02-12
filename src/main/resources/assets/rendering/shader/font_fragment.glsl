#version 330 core

out vec4 outColor;

in vec3 passTextureCoordinates;
in vec4 passCharColor;

uniform sampler2DArray texureArray;


void main() {
    vec4 textureColor = texture(texureArray, passTextureCoordinates);
    if (passCharColor.a == 1.0f && textureColor.a == 0) {
        discard;
    }
    textureColor.rgb = passCharColor.rgb * vec3(1.0f) / textureColor.rgb;
    textureColor.a = passCharColor.a;
    outColor = textureColor;
}
