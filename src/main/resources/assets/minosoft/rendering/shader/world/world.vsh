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
layout (location = 1) in vec2 vinUV;
layout (location = 2) in uint vinIndexLayerAnimation;// texture index (0xF0000000), texture layer (0x0FFFF000), animation index (0x00000FFF)
layout (location = 3) in uint vinTintColorAndLight;// Light (0xFF000000); 3 bytes color (0x00FFFFFF)

flat out uint finTextureIndex1;
out vec3 finTextureCoordinates1;
flat out uint finTextureIndex2;
out vec3 finTextureCoordinates2;
out float finInterpolation;

out vec4 finTintColor;

uniform mat4 uViewProjectionMatrix;

#define POSTPROCESSING_FOG

#include "minosoft:animation"
#include "minosoft:color"
#include "minosoft:light"

void work() {
    gl_Position = uViewProjectionMatrix * vec4(vinPosition, 1.0f);
    finTintColor = getRGBColor(vinTintColorAndLight & 0xFFFFFFu) * getLight(vinTintColorAndLight >> 24u);


    uint animationIndex = vinIndexLayerAnimation & 0xFFFu;
    if (animationIndex == 0u) {
        finTextureIndex1 = vinIndexLayerAnimation >> 28u;
        finTextureCoordinates1 = vec3(vinUV, ((vinIndexLayerAnimation >> 12) & 0xFFFFu));
        finInterpolation = 0.0f;
        return;
    }

    uvec4 data = uAnimationData[animationIndex - 1u];
    uint texture1 = data.x;
    uint texture2 = data.y;
    uint interpolation = data.z;

    finTextureIndex1 = texture1 >> 28u;
    finTextureCoordinates1 = vec3(vinUV, ((texture1 >> 12) & 0xFFFFu));

    finTextureIndex2 = texture2 >> 28u;
    finTextureCoordinates2 = vec3(vinUV, ((texture2 >> 12) & 0xFFFFu));

    finInterpolation = interpolation / 100.0f;
}

    #include "minosoft:postprocessing/vertex"
