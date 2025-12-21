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

#ifndef INCLUDE_MINOSOFT_ANIMATION
#define INCLUDE_MINOSOFT_ANIMATION

/*
* Available defines:
* - FIXED_MIPMAP_LEVEL
* - TRANSPARENT
* - FOG
*/

// in/out

#ifdef SHADER_TYPE_VERTEX
uint textureArray;
float textureLayer;

#ifndef HAS_GEOMETRY_SHADER
flat out uint finTextureArray;
out float finTextureLayer;
out mediump vec2 finTextureUV;
#endif

#elif defined SHADER_TYPE_GEOMETRY
 flat out uint finTextureArray;
out float finTextureLayer;
out mediump vec2 finTextureUV;

#elif defined SHADER_TYPE_FRAGMENT
 flat in uint finTextureArray;
in float finTextureLayer;
in mediump vec2 finTextureUV;
#endif



#ifdef SHADER_TYPE_FRAGMENT
#include "minosoft:texture"
#include "minosoft:alpha"


void applyTexel() {
vec4 texel = getTexture(finTextureArray, vec3(finTextureUV, finTextureLayer));
discard_if_0(texel.a);

foutColor *= texel;

    #ifdef FOG
    fog_set();
    #endif
}

#endif

#ifdef SHADER_TYPE_VERTEX

uint animation_extractArray(uint animation) {
    return animation >> 28u;
}
uint animation_extractLayer(uint animation) {
    return (animation >> 12) & 0xFFFFu;
}
void setTexture(uint animation) {
textureArray = animation_extractArray(animation);
textureLayer = animation_extractLayer(animation);
}

void setTexture(float animation) {
    setTexture(floatBitsToUint(animation));
}

#ifndef HAS_GEOMETRY_SHADER
void setTexture(vec2 uv, uint animation) {
    setTexture(animation);
    finTextureUV = uv;

    finTextureArray = textureArray;
finTextureLayer = textureLayer;
}

void setTexture(vec2 uv, float animation) {
    setTexture(uv, floatBitsToUint(animation));
}
#endif
#endif

#endif
