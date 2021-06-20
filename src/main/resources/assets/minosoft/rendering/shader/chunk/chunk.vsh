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
layout (location = 1) in vec2 vinUVCoordinates;
layout (location = 2) in uint vinTextureLayer;

layout (location = 3) in int vinAnimationIndex;
layout (location = 4) in uint vinTintColor;

flat out uint finTextureIndex1;
out vec3 finTextureCoordinates1;
flat out uint finTextureIndex2;
out vec3 finTextureCoordinates2;
out float finInterpolation;

out vec4 finTintColor;

uniform mat4 uViewProjectionMatrix;


#include "minosoft:animation"

#include "minosoft:color"

void main() {
    gl_Position = uViewProjectionMatrix * vec4(vinPosition, 1.0f);
    finTintColor = getRGBColor(vinTintColor);


    if (vinAnimationIndex == -1) {
        finTextureIndex1 = vinTextureLayer >> 24u;

        finTextureCoordinates1 = vec3(vinUVCoordinates, (vinTextureLayer & 0xFFFFFFu));

        finInterpolation = 0.0f;
        return;
    }

    uvec4 data = uAnimationData[vinAnimationIndex];
    uint firstTexture = data.x;
    uint secondTexture = data.y;
    uint interpolation = data.z;

    finTextureIndex1 = firstTexture >> 24u;
    finTextureCoordinates1 = vec3(vinUVCoordinates, firstTexture & 0xFFFFFFu);

    finTextureIndex2 = secondTexture >> 24u;
    finTextureCoordinates2 = vec3(vinUVCoordinates, secondTexture & 0xFFFFFFu);

    finInterpolation = interpolation / 100.0f;
}
