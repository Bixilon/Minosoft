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


uniform sampler2DArray uTextures[7];

vec4 getTexture(uint textureId, vec3 textureCoordinates) { // ToDo: This method is just stupid and workarounds a opengl crash with mesa drivers
    #ifdef __NVIDIA
    return texture(uTextures[textureId], textureCoordinates);
    #else
    switch (textureId) {
        case 1u: return texture(uTextures[1], textureCoordinates);
        case 2u: return texture(uTextures[2], textureCoordinates);
        case 3u: return texture(uTextures[3], textureCoordinates);
        case 4u: return texture(uTextures[4], textureCoordinates);
        case 5u: return texture(uTextures[5], textureCoordinates);
        case 6u: return texture(uTextures[6], textureCoordinates);
    }
    return texture(uTextures[0], textureCoordinates);
    #endif
}