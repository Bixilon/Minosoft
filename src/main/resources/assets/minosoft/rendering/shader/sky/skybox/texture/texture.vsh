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
layout (location = 1) in uint uvIndex;

uniform mat4 uSkyViewProjectionMatrix;
uniform uint uIndexLayer;

flat out uint finTextureIndex;
out vec3 finTextureCoordinates;

#include "minosoft:uv"
#include "minosoft:color"
#include "minosoft:light"

void main() {
    gl_Position = uSkyViewProjectionMatrix * vec4(vinPosition, 1.0f);

    finTextureIndex = uIndexLayer >> 28u;
    finTextureCoordinates = vec3(CONST_UV[uvIndex] * 15.0f, ((uIndexLayer >> 12) & 0xFFFFu));
}
