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

in vec3 finFragmentPosition;


uniform vec3 uCameraPosition;
uniform float uFogStart = 60.0f * 60.0f;
uniform float uFogEnd = 75.0f * 75.0f;
uniform vec4 uFogColor;
uniform bool uUseFogColor = false;

float calulate_fog_alpha(float distance2) {
    if (distance2 < uFogStart) {
        return 1.0f;
    }
    if (distance2 > uFogEnd) {
        return 0.0f;
    }

    return pow(1.0f - (distance2 - uFogStart) / (uFogEnd - uFogStart), 2);
}

float calculate_fog() {
    if (uFogStart > 100000.0f) {
        return 1.0f;
    };
    #ifdef FOG_SPHERE
    vec3 distance_vec3 = finFragmentPosition.xyz - uCameraPosition.xyz;
    float distance = dot(distance_vec3, distance_vec3);
    #else
    vec2 distance_vec2 = finFragmentPosition.xz - uCameraPosition.xz;
    float distance = dot(distance_vec2, distance_vec2);
    #endif
    return calulate_fog_alpha(distance);
}

void set_fog() {
    float alpha = calculate_fog();
    if (uUseFogColor) {
        foutColor.rgb = mix(uFogColor.rgb, foutColor.rgb, foutColor.a * alpha);
        foutColor.a = 1.0f;
    } else {
        if (alpha <= 0.0f) {
            discard;
        }
        foutColor.a = foutColor.a * alpha;
    }
}
