#version 330 core

out vec4 outColor;

in vec3 passTextureCoordinates;
in vec3 passCharColor;

uniform sampler2DArray texureArray;


void main() {
    vec4 textureColor = texture(texureArray, passTextureCoordinates);
    if (textureColor.a == 0) {
        textureColor.a = 0.1f;
        textureColor.r = 0.1f;
        textureColor.g = 0.1f;
        textureColor.b = 0.1f;
        //discard;
    } else {
        textureColor.rgb = passCharColor * (vec3(1.0f) / textureColor.rgb);
    }
    outColor = textureColor;
}
