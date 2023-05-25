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

#version 330 core

layout (location = 0) in vec3 vinPosition;
layout (location = 1) in float vinUVIndex;
layout (location = 2) in float vinWidth;

uniform mat4 uViewProjectionMatrix;
uniform uint uIndexLayer;
uniform float uTextureOffset;

uniform vec3 uCameraPosition;

flat out uint finTextureIndex;
out vec3 finTextureCoordinates;
out vec3 finFragmentPosition; // fog

#define DENSITY 2.0f
#define HEIGHT 150.0f

#include "minosoft:uv"
#include "minosoft:color"
#include "minosoft:light"

void main() {
    vec3 position = vinPosition;
    if (position.y < 0.0f) {
        position.y = uCameraPosition.y - HEIGHT;
    } else if (position.y > 0.0f) {
        position.y = uCameraPosition.y + HEIGHT;
    }
    gl_Position = uViewProjectionMatrix * vec4(position, 1.0f);

    finTextureIndex = uIndexLayer >> 28u;

    vec2 uv = CONST_UV[floatBitsToUint(vinUVIndex)];
    uv.x *= (vinWidth / DENSITY); // TODO: insert width
    uv.y *= (HEIGHT / DENSITY);

    finTextureCoordinates = vec3(uv, ((uIndexLayer >> 12u) & 0xFFFFu));
    finTextureCoordinates.x += uTextureOffset;
    finTextureCoordinates.y += uTextureOffset;

    finFragmentPosition = position.xyz;
}
