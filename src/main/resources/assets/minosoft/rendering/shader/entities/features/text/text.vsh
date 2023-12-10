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

layout (location = 0) in vec2 vinPosition;
layout (location = 1) in vec2 vinUV;
layout (location = 2) in float vinIndexLayerAnimation;// texture index (0xF0000000), texture layer (0x0FFFF000)
layout (location = 3) in float vinTint;

uniform mat4 uViewProjectionMatrix;
uniform mat4 uMatrix;
uniform uint uTintColor;

out vec3 finFragmentPosition;


#include "minosoft:color"
#include "minosoft:animation"

void main() {
    vec4 position = uMatrix * vec4(vinPosition, 0.0f, 1.0f);
    gl_Position = uViewProjectionMatrix * position;
    finTintColor = getRGBAColor(floatBitsToUint(vinTint)) * getRGBColor(uTintColor);
    finFragmentPosition = position.xyz;

    setTexture(vinUV, vinIndexLayerAnimation);
}
