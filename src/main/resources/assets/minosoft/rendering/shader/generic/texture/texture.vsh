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
layout (location = 3) in uint vinTintColor;// Light (0xFF000000); 3 bytes color (0x00FFFFFF)

#include "minosoft:animation/header_vertex"

#include "minosoft:animation/buffer"
#include "minosoft:color"

uniform mat4 uViewProjectionMatrix;

void main() {
    gl_Position = uViewProjectionMatrix * vec4(vinPosition, 1.0f);
    finTintColor = getRGBColor(vinTintColor & 0xFFFFFFu);


    #include "minosoft:animation/main_vertex"
}
