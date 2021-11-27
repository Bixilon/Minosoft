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
layout (location = 3) in uint vinIndexLayerAnimation;

layout (location = 4) in float vinScale;
layout (location = 5) in uint vinTintColor;


#include "minosoft:animation"

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


    uint animationIndex = vinIndexLayerAnimation & 0xFFFu;

    if (animationIndex == 0u) {
        ginVertex.textureIndex1 = vinIndexLayerAnimation >> 28u;
        ginVertex.textureLayer1 = ((vinIndexLayerAnimation >> 12) & 0xFFFFu);

        ginVertex.interpolation = 0.0f;
        return;
    }

    uvec4 data = uAnimationData[animationIndex -1u];
    uint texture1 = data.x;
    uint texture2 = data.y;
    uint interpolation = data.z;



    ginVertex.textureIndex1 = texture1 >> 28u;
    ginVertex.textureLayer1 = (texture1 >> 12) & 0xFFFFu;

    ginVertex.textureIndex2 = texture2 >> 28u;
    ginVertex.textureLayer2 = (texture2 >> 12) & 0xFFFFu;

    ginVertex.interpolation = interpolation / 100.0f;
}
