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
layout (triangle_strip) out;
layout (max_vertices = 4) out;

uniform mat4 uViewProjectionMatrix;
uniform vec3 uCameraRight;
uniform vec3 uCameraUp;


in Vertex
{
    uint textureIndex1;
    uint textureLayer1;
    uint textureIndex2;
    uint textureLayer2;
    float interpolation;
    vec2 minUVCoordinates;
    vec2 maxUVCoordinates;

    float scale;
    vec4 tintColor;
} ginVertex[];


flat out uint finTextureIndex1;
out vec3 finTextureCoordinates1;
flat out uint finTextureIndex2;
out vec3 finTextureCoordinates2;
flat out float finInterpolation;

out vec4 finTintColor;

void main()
{
    vec3 pointPosition = gl_in[0].gl_Position.xyz;


    finTextureIndex1 = ginVertex[0].textureIndex1;
    finTextureIndex2 = ginVertex[0].textureIndex2;
    finInterpolation = ginVertex[0].interpolation;
    finTintColor = ginVertex[0].tintColor;


    gl_Position = uViewProjectionMatrix * vec4(pointPosition - (uCameraRight - uCameraUp) * ginVertex[0].scale, 1.0);
    finTextureCoordinates1 = vec3(ginVertex[0].minUVCoordinates.x, ginVertex[0].minUVCoordinates.y, ginVertex[0].textureLayer1);
    finTextureCoordinates2 = vec3(ginVertex[0].minUVCoordinates.x, ginVertex[0].minUVCoordinates.y, ginVertex[0].textureLayer2);
    EmitVertex();

    gl_Position = uViewProjectionMatrix * vec4(pointPosition - (uCameraRight + uCameraUp) * ginVertex[0].scale, 1.0);
    finTextureCoordinates1 = vec3(ginVertex[0].minUVCoordinates.x, ginVertex[0].maxUVCoordinates.y, ginVertex[0].textureLayer1);
    finTextureCoordinates2 = vec3(ginVertex[0].minUVCoordinates.x, ginVertex[0].maxUVCoordinates.y, ginVertex[0].textureLayer2);
    EmitVertex();

    gl_Position = uViewProjectionMatrix * vec4(pointPosition + (uCameraRight + uCameraUp) * ginVertex[0].scale, 1.0);
    finTextureCoordinates1 = vec3(ginVertex[0].maxUVCoordinates.x, ginVertex[0].minUVCoordinates.y, ginVertex[0].textureLayer1);
    finTextureCoordinates2 = vec3(ginVertex[0].maxUVCoordinates.x, ginVertex[0].minUVCoordinates.y, ginVertex[0].textureLayer2);
    EmitVertex();

    gl_Position = uViewProjectionMatrix * vec4(pointPosition + (uCameraRight - uCameraUp) * ginVertex[0].scale, 1.0);
    finTextureCoordinates1 = vec3(ginVertex[0].maxUVCoordinates.x, ginVertex[0].maxUVCoordinates.y, ginVertex[0].textureLayer1);
    finTextureCoordinates2 = vec3(ginVertex[0].maxUVCoordinates.x, ginVertex[0].maxUVCoordinates.y, ginVertex[0].textureLayer2);
    EmitVertex();


    EndPrimitive();
}
