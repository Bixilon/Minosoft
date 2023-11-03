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
layout (location = 2) in float vinIndexLayerAnimation;// texture index (0xF0000000), texture layer (0x0FFFF000)
layout (location = 3) in float vinLightTint;// Light (0xFF000000); 3 bytes color (0x00FFFFFF)

uniform mat4 uViewProjectionMatrix;
uniform mat4 uMatrix;


flat out uint finTextureIndex;
out vec3 finTextureCoordinates;
out vec3 finFragmentPosition;

out vec4 finTintColor;



#include "minosoft:color"
#include "minosoft:light"

void main() {
    gl_Position = uViewProjectionMatrix * uMatrix * vec4(vinPosition, 1.0f);
    uint lightTint = floatBitsToUint(vinLightTint);
    finTintColor = getRGBColor(lightTint & 0xFFFFFFu);//  * getLight(lightTint >> 24u); // TODO
    finFragmentPosition = vinPosition;

    uint indexLayerAnimation = floatBitsToUint(vinIndexLayerAnimation);
    finTextureIndex = indexLayerAnimation >> 28u;
    finTextureCoordinates = vec3(vinUV, ((indexLayerAnimation >> 12) & 0xFFFFu));
}
