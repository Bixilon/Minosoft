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

layout (location = 3) in uint animationLayers;
layout (location = 4) in uint tintColor;

flat out uint passTextureIdIndex;
out vec3 passTextureCoordinates;
out vec4 passTintColor;

uniform mat4 viewProjectionMatrix;
uniform int animationTick;


void main() {
    gl_Position = viewProjectionMatrix * vec4(inPosition, 1.0f);
    passTintColor = vec4(((tintColor >> 16u) & 0xFFu) / 255.0f, ((tintColor >> 8u) & 0xFFu) / 255.0f, (tintColor & 0xFFu) / 255.0f, 1.0f);

    passTextureIdIndex = textureLayer >> 24u;

    passTextureCoordinates = vec3(textureIndex, (textureLayer & 0xFFFFFFu) + (uint(animationTick) % animationLayers));
    return;
}
