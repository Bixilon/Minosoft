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

#define FOG

in vec3 finFragmentPosition;


uniform vec3 uCameraPosition;
uniform float uFogStart = 60.0f * 60.0f;
uniform float uFogEnd = 75.0f * 75.0f;
uniform float uFogDistance = 15.0f * 15.0f;
uniform vec4 uFogColor;
uniform uint uFogFlags = 0u;


#define FOG_ENABLE 0x01u << 0u
#define FOG_USE_COLOR 0x01u << 1u

#ifndef DISTANCE_MULTIPLIER
#define DISTANCE_MULTIPLIER 1.0f
#endif

float fog_alpha_from_distance(float distance2) {
    if (distance2 < uFogStart) return 1.0f;
    if (distance2 > uFogEnd) return 0.0f;

    float alpha = 1.0f - (distance2 - uFogStart) / uFogDistance;

    return alpha * alpha;
}

float fog_calculate_distance() {
    vec3 distance = finFragmentPosition.xyz - uCameraPosition.xyz;
    distance.y *= 0.7f;// increase possible distance on y axis; if you change this, also update fog clipping in MatrixHandler.kt
    return dot(distance, distance);
}


float fog_calculate_alpha() {
    if (uFogStart > 1000.0f * 1000.0f) return 1.0f;

    float distance = fog_calculate_distance();
    return fog_alpha_from_distance(distance * DISTANCE_MULTIPLIER);
}

void fog_mix_alpha(float alpha) {
    if (alpha <= 0.0f) discard;
    foutColor.a = foutColor.a * alpha;
}

void fog_mix_color(float alpha) {
    foutColor.rgb = mix(uFogColor.rgb, foutColor.rgb, foutColor.a * alpha);// multiply with uFogColor.a to interpolate when fog color is unset?
    foutColor.a = 1.0f;
}

void fog_set() {
    if ((uFogFlags & FOG_ENABLE) == 0u) return;

    float alpha = fog_calculate_alpha();


    if ((uFogFlags & FOG_USE_COLOR) != 0u) {
        fog_mix_color(alpha);
    } else {
        fog_mix_alpha(alpha);
    }
}
