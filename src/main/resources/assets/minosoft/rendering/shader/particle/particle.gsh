/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

#version 330

layout (points) in;
layout (triangle_strip, max_vertices = 4) out;

uniform mat4 uViewProjectionMatrix;
uniform vec3 uCameraRight;
uniform vec3 uCameraUp;

out vec3 finAnimationPosition1;
out vec3 finAnimationPosition2;
out vec4 finTintColor;

in Vertex
{
    vec2 minUV;
    vec2 maxUV;
    float layer1;
    float layer2;

    float scale;
    vec4 tintColor;
} ginVertex[];


void emit(vec3 offset, vec2 uv) {
    vec3 pointPosition = gl_in[0].gl_Position.xyz;

    gl_Position = uViewProjectionMatrix * vec4(pointPosition + offset * ginVertex[0].scale, 1.0);
    finAnimationPosition1 = vec3(uv, ginVertex[0].layer1);
    finAnimationPosition2 = vec3(uv, ginVertex[0].layer2);

    EmitVertex();
}

void main() {
    finTintColor = ginVertex[0].tintColor;


    emit(-(uCameraRight - uCameraUp), vec2(ginVertex[0].minUV.x, ginVertex[0].minUV.y));
    emit(-(uCameraRight + uCameraUp), vec2(ginVertex[0].minUV.x, ginVertex[0].maxUV.y));
    emit(uCameraRight + uCameraUp, vec2(ginVertex[0].maxUV.x, ginVertex[0].minUV.y));
    emit(uCameraRight - uCameraUp, vec2(ginVertex[0].maxUV.x, ginVertex[0].maxUV.y));

    EndPrimitive();
}
