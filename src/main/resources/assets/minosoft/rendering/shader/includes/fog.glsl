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
uniform float uFogStart = 60.0f;
uniform float uFogEnd = 75.0f;

float calulate_fog_alpha(float distance) {
    if (distance < uFogStart) {
        return 1.0f;
    }
    if (distance > uFogEnd) {
        discard;
    }

    return pow(1.0f - (distance - uFogStart) / (uFogEnd - uFogStart), 2);
}

float calculate_fog() {
    float distance = length(finFragmentPosition.xz - uCameraPosition.xz);
    return calulate_fog_alpha(distance);
}

void set_fog_alpha() {
    foutColor.a = calculate_fog();
}
