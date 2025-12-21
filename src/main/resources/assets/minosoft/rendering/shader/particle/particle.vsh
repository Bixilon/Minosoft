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
layout (location = 1) in float vinMinUV;
layout (location = 2) in float vinMaxUV;
layout (location = 3) in float vinIndexLayerAnimation;

layout (location = 4) in float vinScale;
layout (location = 5) in float vinTintColor;
layout (location = 6) in float vinLight;


#include "minosoft:vsh"
#include "minosoft:light"


out Vertex
{
    vec2 minUV;
    vec2 maxUV;
    flat uint array;
    flat float layer1; flat float layer2;
    flat float interpolation;

    float scale;
    vec4 tintColor;
} ginVertex;

#include "minosoft:color"
#include "minosoft:animation"

void main() {
    gl_Position = vec4(vinPosition, 1.0f);

    ginVertex.maxUV = uv_unpack(floatBitsToUint(vinMaxUV));
    ginVertex.minUV = uv_unpack(floatBitsToUint(vinMinUV));

    ginVertex.scale = vinScale;
    ginVertex.tintColor = getRGBAColor(floatBitsToUint(vinTintColor)) * getLight(floatBitsToUint(vinLight) & 0xFFu);

    setTexture(vinIndexLayerAnimation);
    ginVertex.array = animationArray;
    ginVertex.layer1 = animationLayer1; ginVertex.layer2 = animationLayer2;
    ginVertex.interpolation = animationInterpolation;
}
