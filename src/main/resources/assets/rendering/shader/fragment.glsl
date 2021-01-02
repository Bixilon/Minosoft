#version 330 core
out vec4 FragColor;

in vec3 vertexColor;
in vec2 TexCoord;

// texture sampler
uniform sampler2D texture0;
uniform sampler2D texture1;

uniform float visibility;

void main() {
   vec2 newTextCoord = vec2(TexCoord.x, -TexCoord.y);
   FragColor = mix(texture(texture0, newTextCoord), texture(texture1, newTextCoord), visibility);
}
