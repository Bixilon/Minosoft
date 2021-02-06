#version 330 core

out vec4 outColor;

in vec3 passTextureCoordinates;

uniform sampler2DArray texureArray;

void main() {
    vec4 texColor = texture(texureArray, passTextureCoordinates);
    if (texColor.a == 0) { // ToDo: This only works for alpha == 0. What about semi transparency? We would need to sort the faces, etc. See: https://learnopengl.com/Advanced-OpenGL/Blending
        discard;
    }
    outColor = texColor;
}
