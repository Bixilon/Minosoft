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

vec4 getTexture(uint textureId, vec3 uv) { // ToDo: This method is just stupid and workarounds an opengl crash with mesa drivers
    #if defined __NVIDIA || defined __AMD
    return texture(uTextures[textureId], uv);
    #else
    switch (textureId) {
        case 1u: return texture(uTextures[1], uv);
        case 2u: return texture(uTextures[2], uv);
        case 3u: return texture(uTextures[3], uv);
        case 4u: return texture(uTextures[4], uv);
        case 5u: return texture(uTextures[5], uv);
        case 6u: return texture(uTextures[6], uv);
    }
    return texture(uTextures[0], uv);
    #endif
}

vec4 getTexture(uint textureId, vec3 uv, float mipmapLevel) { // ToDo: This method is just stupid and workarounds an opengl crash with mesa drivers
    #if defined __NVIDIA || defined __AMD
    return textureLod(uTextures[textureId], uv, mipmapLevel);
    #else
    switch (textureId) {
        case 1u: return textureLod(uTextures[1], uv, mipmapLevel);
        case 2u: return textureLod(uTextures[2], uv, mipmapLevel);
        case 3u: return textureLod(uTextures[3], uv, mipmapLevel);
        case 4u: return textureLod(uTextures[4], uv, mipmapLevel);
        case 5u: return textureLod(uTextures[5], uv, mipmapLevel);
        case 6u: return textureLod(uTextures[6], uv, mipmapLevel);
    }
    return textureLod(uTextures[0], uv, mipmapLevel);
    #endif
}

