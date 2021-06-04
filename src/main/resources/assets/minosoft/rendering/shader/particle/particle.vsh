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
layout (location = 1) in vec2 vinMinUVCoordinates;
layout (location = 2) in vec2 vinMaxUVCoordinates;
layout (location = 3) in uint vinTextureLayer;
layout (location = 4) in int vinAnimationIndex;

layout (location = 5) in float vinScale;
layout (location = 6) in uint vinTintColor;


layout(std140) uniform uAnimationBuffer
{
    uvec4 uAnimationData[ANIMATED_TEXTURE_COUNT];
};

out Vertex
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
} ginVertex;

#include "minosoft:color"

void main() {
    gl_Position = vec4(vinPosition, 1.0f);

    ginVertex.maxUVCoordinates = vinMaxUVCoordinates;
    ginVertex.minUVCoordinates = vinMinUVCoordinates;

    ginVertex.scale = vinScale;
    ginVertex.tintColor = getRGBAColor(vinTintColor);

    if (vinAnimationIndex == -1) {
        ginVertex.textureIndex1 = vinTextureLayer >> 24u;
        ginVertex.textureLayer1 = vinTextureLayer & 0xFFFFFFu;

        ginVertex.interpolation = 0.0f;
        return;
    }

    uvec4 data = uAnimationData[vinAnimationIndex];
    uint texture1 = data.x;
    uint texture2 = data.y;
    uint interpolation = data.z;

    ginVertex.textureIndex1 = texture1 >> 24u;
    ginVertex.textureLayer1 = texture1 & 0xFFFFFFu;

    ginVertex.textureIndex2 = texture2 >> 24u;
    ginVertex.textureLayer2 = texture2 & 0xFFFFFFu;

    ginVertex.interpolation = interpolation / 100.0f;
}
