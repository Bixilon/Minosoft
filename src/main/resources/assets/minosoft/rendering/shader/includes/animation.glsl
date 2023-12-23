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
* - ANIMATED_TEXTURE_COUNT
* - FIXED_MIPMAP_LEVEL
* - TRANSPARENT
* - FOG
*/

// buffer
#ifdef SHADER_TYPE_VERTEX
layout(std140) uniform uSpriteBuffer
{
    uvec4 uAnimationData[ANIMATED_TEXTURE_COUNT];
};
#endif


// in/out

#if defined SHADER_TYPE_VERTEX
#ifdef HAS_GEOMETRY_SHADER
uint finAnimationArray1; vec3 finAnimationPosition1;
uint finAnimationArray2; vec3 finAnimationPosition2;
float finAnimationInterpolation;
#else
flat out uint finAnimationArray1; out vec3 finAnimationPosition1;
flat out uint finAnimationArray2; out vec3 finAnimationPosition2;
out float finAnimationInterpolation;
#endif
#elif defined SHADER_TYPE_GEOMETRY
flat out uint finAnimationArray1; out vec3 finAnimationPosition1;
flat out uint finAnimationArray2; out vec3 finAnimationPosition2;
out float finAnimationInterpolation;
#elif defined SHADER_TYPE_FRAGMENT
flat in uint finAnimationArray1; in vec3 finAnimationPosition1;
flat in uint finAnimationArray2; in vec3 finAnimationPosition2;
in float finAnimationInterpolation;
#endif



#ifdef SHADER_TYPE_FRAGMENT
#include "minosoft:texture"
#include "minosoft:alpha"

vec4 getAnimationTexture() {
    vec4 texel1 = getTexture(finAnimationArray1, finAnimationPosition1);
    discard_if_0(texel1.a);

    float interpolation = finAnimationInterpolation;

    if (interpolation == 0.0f) { // no animation
        return texel1;
    }
    vec4 texel2 = getTexture(finAnimationArray2, finAnimationPosition2);
    discard_if_0(texel2.a);

    return mix(texel1, texel2, interpolation);
}

void applyTexel() {
    vec4 texel = getAnimationTexture();
    foutColor = texel;

    #ifdef FOG
    set_fog();
    #endif
}

#endif

#ifdef SHADER_TYPE_VERTEX
#define INVALID_ANIMATION uint(-1)

uint animation_extractAnimationId(uint animation) {
    return (animation & 0xFFFu) - 1u;
}
uint animation_extractArray(uint animation) {
    return animation >> 28u;
}
uint animation_extractLayer(uint animation) {
    return (animation >> 12) & 0xFFFFu;
}
vec3 animation_texturePosition(vec2 uv, uint animation) {
    return vec3(uv, animation_extractLayer(animation));
}

void animation_notAnimated(vec2 uv, uint animation) {
    finAnimationArray1 = animation_extractArray(animation);
    finAnimationPosition1 = animation_texturePosition(uv, animation);
    finAnimationInterpolation = 0.0f;
}

void animation_animated(uint animationId, vec2 uv) {
    uvec4 data = uAnimationData[animationId];

    uint texture1 = data.x;
    finAnimationArray1 = animation_extractArray(texture1);
    finAnimationPosition1 = animation_texturePosition(uv, texture1);

    uint texture2 = data.y;
    finAnimationArray2 = animation_extractArray(texture2);
    finAnimationPosition2 = animation_texturePosition(uv, texture2);

    finAnimationInterpolation = data.z / 100.0f;
}

void setTexture(vec2 uv, uint animation) {
    uint animationId = animation_extractAnimationId(animation);
    if (animationId == INVALID_ANIMATION) {
        animation_notAnimated(uv, animation);
        return;
    }
    animation_animated(animationId, uv);
}

void setTexture(vec2 uv, float animation) {
    setTexture(uv, floatBitsToUint(animation));
}
#endif

#endif
