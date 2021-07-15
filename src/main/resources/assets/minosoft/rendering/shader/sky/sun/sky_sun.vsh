/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

#version 330 core

layout (location = 0) in vec3 vinPosition;
layout (location = 1) in vec2 vinUVCoordinates;
layout (location = 2) in uint vinTextureLayer;
layout (location = 3) in uint vinTintColor;

out vec4 finTintColor;
flat out uint finTextureIndex;
out vec3 finTextureCoordinates;

uniform mat4 uSkyViewProjectionMatrix;

#include "minosoft:color"

void main() {
    gl_Position = (uSkyViewProjectionMatrix * vec4(vinPosition, 1.0f)).xyww - vec4(0.0f, 0.0f, 0.000001f, 0.0f);// prevent face fighting

    finTextureCoordinates = vec3(vinUVCoordinates, vinTextureLayer & 0xFFFFFFu);
    finTextureIndex = vinTextureLayer >> 24u;
    finTintColor = getRGBAColor(vinTintColor);
}
