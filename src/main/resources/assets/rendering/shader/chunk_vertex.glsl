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
layout (location = 2) in int textureLayer;

out vec3 passTextureCoordinates;

uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;
uniform int currentTick;

const int TEXTURE_COUNT = 1024;
uniform int[TEXTURE_COUNT] animatedTextureFrameTimes;// ToDo: Support frames more textures
uniform int[TEXTURE_COUNT] animatedTextureAnimations;
uniform float[TEXTURE_COUNT] textureSingleSizeY;

void main() {
    gl_Position = projectionMatrix * viewMatrix *  vec4(inPosition, 1.0f);
    int textureAnimations = animatedTextureAnimations[textureLayer];
    if (textureAnimations == 1) {
        passTextureCoordinates = vec3(textureIndex, textureLayer);
        return;
    }

    int frameTime = animatedTextureFrameTimes[textureLayer];
    passTextureCoordinates = vec3(textureIndex.x, textureIndex.y + (textureSingleSizeY[textureLayer] * ((currentTick / frameTime) % textureAnimations)), textureLayer);
}
