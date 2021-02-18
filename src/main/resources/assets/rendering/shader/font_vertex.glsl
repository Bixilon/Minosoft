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

layout (location = 0) in vec3 inPosition;
layout (location = 1) in vec2 textureIndex;
layout (location = 2) in uint textureLayer;
layout (location = 3) in uint charColor;

out vec3 passTextureCoordinates;

out vec4 passCharColor;

void main() {
    gl_Position = vec4(inPosition.xyz, 1.0f);
    passTextureCoordinates = vec3(textureIndex, textureLayer);
    // uncompact array: 8 bit each for red, gree, blue, alpha
    passCharColor = vec4(((charColor >> 24u) & 0xFFu) / 255.0f, ((charColor >> 16u) & 0xFFu) / 255.0f, ((charColor >> 8u) & 0xFFu) / 255.0f, (charColor & 0xFFu) / 255.0f);
}
