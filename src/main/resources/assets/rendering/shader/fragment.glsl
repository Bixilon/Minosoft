#version 330 core

out vec4 outColor;

in vec3 passTextureCoordinates;

// texture sampler
uniform sampler2DArray texureArray;

void main() {
   outColor = texture(texureArray, passTextureCoordinates);
}
