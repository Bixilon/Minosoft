/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

#version 330 core

layout (location = 0) in vec3 vinPosition;
layout (location = 1) in uint vinSide;

out vec3 finFragmentPosition;
flat out float finBrightness;


uniform mat4 uViewProjectionMatrix;
uniform float uOffset;
uniform float uYOffset;
uniform vec3 uCameraPosition;


void main() {
    vec3 position = vinPosition;
    position.x -= uOffset;
    position.y += uYOffset;

    gl_Position = uViewProjectionMatrix * vec4(position, 1.0);

    finFragmentPosition = position / 3;

    switch (vinSide) {
        case 0u: finBrightness = 0.7f; break; // DOWN
        case 1u: finBrightness = 1.0f; break; // UP
        case 2u: case 3u: finBrightness = 0.9f;  break; // NORTH, SOUTH
        case 4u: case 5u: finBrightness = 0.8f;  break; // WEST, EAST
    }
}
