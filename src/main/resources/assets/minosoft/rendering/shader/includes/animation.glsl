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
uint animationArray1; float animationLayer1;
uint animationArray2; float animationLayer2;
float animationInterpolation;
#ifndef HAS_GEOMETRY_SHADER
flat out uint finAnimationArray1; out float finAnimationLayer1;
flat out uint finAnimationArray2; out float finAnimationLayer2;
out vec2 finAnimationUV;
out float finAnimationInterpolation;
#endif
#elif defined SHADER_TYPE_GEOMETRY
flat out uint finAnimationArray1; out float finAnimationLayer1;
flat out uint finAnimationArray2; out float finAnimationLayer2;
out vec2 finAnimationUV;
out float finAnimationInterpolation;
#elif defined SHADER_TYPE_FRAGMENT
flat in uint finAnimationArray1; in float finAnimationLayer1;
flat in uint finAnimationArray2; in float finAnimationLayer2;
in vec2 finAnimationUV;
in float finAnimationInterpolation;
#endif



#ifdef SHADER_TYPE_FRAGMENT
#include "minosoft:texture"
#include "minosoft:alpha"

vec4 getAnimationTexture() {
    vec4 texel1 = getTexture(finAnimationArray1, vec3(finAnimationUV, finAnimationLayer1));
    discard_if_0(texel1.a);

    float interpolation = finAnimationInterpolation;

    if (interpolation == 0.0f) { // no animation
        return texel1;
    }
    vec4 texel2 = getTexture(finAnimationArray2, vec3(finAnimationUV, finAnimationLayer2));
    discard_if_0(texel2.a);

    return mix(texel1, texel2, interpolation);
}

void applyTexel() {
    vec4 texel = getAnimationTexture();
    foutColor *= texel;

    #ifdef FOG
    fog_set();
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

void animation_notAnimated(uint animation) {
    animationArray1 = animation_extractArray(animation);
    animationLayer1 = animation_extractLayer(animation);
    animationInterpolation = 0.0f;
}

void animation_animated(uint animationId) {
    uvec4 data = uAnimationData[animationId];

    uint texture1 = data.x;
    animationArray1 = animation_extractArray(texture1);
    animationLayer1 = animation_extractLayer(texture1);

    uint texture2 = data.y;
    animationArray2 = animation_extractArray(texture2);
    animationLayer2 = animation_extractLayer(texture2);

    animationInterpolation = data.z / 100.0f;
}

void setTexture(uint animation) {
    uint animationId = animation_extractAnimationId(animation);
    if (animationId == INVALID_ANIMATION) {
        animation_notAnimated(animation);
        return;
    }
    animation_animated(animationId);
}

void setTexture(float animation) {
    setTexture(floatBitsToUint(animation));
}

#ifndef HAS_GEOMETRY_SHADER
void setTexture(vec2 uv, uint animation) {
    setTexture(animation);
    finAnimationUV = uv;

    finAnimationArray1 = animationArray1; finAnimationLayer1 = animationLayer1;
    finAnimationArray2 = animationArray2; finAnimationLayer2 = animationLayer2;
    finAnimationInterpolation = animationInterpolation;
}

void setTexture(vec2 uv, float animation) {
    setTexture(uv, floatBitsToUint(animation));
}
#endif
#endif

#endif
