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

layout (location = 3) in int animationIndex;
layout (location = 4) in uint tintColor;

flat out uint passFirstTextureIdIndex;
out vec3 passFirstTextureCoordinates;
flat out uint passSecondTextureIdIndex;
out vec3 passSecondTextureCoordinates;
out float passInterpolateBetweenTextures;

out vec4 passTintColor;

uniform mat4 viewProjectionMatrix;


layout(std140) uniform AnimatedDataBuffer
{
    uvec4 globalAnimationData[ANIMATED_TEXTURE_COUNT];// ToDo: WTF. Why 4 values??
};


void main() {
    gl_Position = viewProjectionMatrix * vec4(inPosition, 1.0f);
    passTintColor = vec4(((tintColor >> 16u) & 0xFFu) / 255.0f, ((tintColor >> 8u) & 0xFFu) / 255.0f, (tintColor & 0xFFu) / 255.0f, 1.0f);


    if (animationIndex == -1) {
        passFirstTextureIdIndex = textureLayer >> 24u;

        passFirstTextureCoordinates = vec3(textureIndex, (textureLayer & 0xFFFFFFu));

        passInterpolateBetweenTextures = 0.0f;
        return;
    }

    uvec4 data = globalAnimationData[animationIndex];
    uint firstTexture = data.x;
    uint secondTexture = data.y;
    uint interpolation = data.z;

    passFirstTextureIdIndex = firstTexture >> 24u;
    passFirstTextureCoordinates = vec3(textureIndex, firstTexture & 0xFFFFFFu);

    passSecondTextureIdIndex = secondTexture >> 24u;
    passSecondTextureCoordinates = vec3(textureIndex, secondTexture & 0xFFFFFFu);

    passInterpolateBetweenTextures = interpolation / 100.0f;

}
