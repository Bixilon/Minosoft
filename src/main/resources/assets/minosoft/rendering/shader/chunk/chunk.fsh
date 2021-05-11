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

out vec4 outColor;

flat in uint passFirstTextureIdIndex;
in vec3 passFirstTextureCoordinates;
flat in uint passSecondTextureIdIndex;
in vec3 passSecondTextureCoordinates;
in float passInterpolateBetweenTextures;

in vec4 passTintColor;

uniform sampler2DArray textureArray[7];

vec4 getTexture(uint textureId, vec3 textureCoordinates){
    switch (textureId){
        case 0u : return texture(textureArray[0], textureCoordinates);
        case 1u: return texture(textureArray[1], textureCoordinates);
        case 2u: return texture(textureArray[2], textureCoordinates);
        case 3u: return texture(textureArray[3], textureCoordinates);
        case 4u: return texture(textureArray[4], textureCoordinates);
        case 5u: return texture(textureArray[5], textureCoordinates);
        case 6u: return texture(textureArray[6], textureCoordinates);
    }
    return texture(textureArray[0], textureCoordinates);
}

void main() {
    vec4 firstTexelColor = getTexture(passFirstTextureIdIndex, passFirstTextureCoordinates);
    if (firstTexelColor.a == 0.0f) { // ToDo: This only works for alpha == 0. What about semi transparency? We would need to sort the faces, etc. See: https://learnopengl.com/Advanced-OpenGL/Blending
        discard;
    }

    if (passInterpolateBetweenTextures == 0.0f) {
        outColor = firstTexelColor * passTintColor;
        return;
    }

    vec4 secondTexelColor =  getTexture(passSecondTextureIdIndex, passSecondTextureCoordinates);

    if (secondTexelColor.a == 0.0f) {
        discard;
    }

    outColor = mix(firstTexelColor, secondTexelColor, passInterpolateBetweenTextures) * passTintColor;
}
