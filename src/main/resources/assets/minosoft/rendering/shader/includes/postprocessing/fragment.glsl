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

#ifdef POSTPROCESSING_FOG
in vec3 finVertexPosition;
uniform vec3 uCameraPosition;
uniform float uFogStart;
uniform float uFogEnd;
uniform vec4 uFogColor;


float getFogFactor(float distance) {
    if (distance >= uFogEnd) {
        return 0.0f;
    }
    if (distance <= uFogStart) {
        return 1.0f;
    }

    // ToDo: Exponential fog
    return (uFogEnd - distance) / (uFogEnd - uFogStart);
}

    #endif


void main() {
    work();

    #ifdef POSTPROCESSING_FOG
    float fogFactor = getFogFactor(distance(uCameraPosition, finVertexPosition));

    if (fogFactor != 1.0f) {
        foutColor = vec4(mix(uFogColor.rgb, foutColor.rgb, fogFactor), foutColor.a);
    };
    #endif
}
