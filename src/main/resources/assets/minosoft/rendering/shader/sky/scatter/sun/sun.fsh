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

precision highp float;


out vec4 foutColor;

in vec3 finFragmentPosition;

uniform float uIntensity;
uniform vec3 uSunPosition;

#define PI 3.1415926535897932384626433832795

#define LESS_Y 0.3f
#define LESS_SCALE_VALUE sin(LESS_Y * PI / 2)
#define LESS_MULTIPLICATOR_1 LESS_Y / LESS_SCALE_VALUE
#define LESS_MULTIPLICATOR_2 LESS_SCALE_VALUE / LESS_Y


#define CLOSE_DISTANCE 0.18f
#define MAX_DISTANCE 2.1f // (2 + 0.2 / 2)

#define END_DISTANCE MAX_DISTANCE - 0.2f

#define YELLOWISH vec3(1.0f, 0.6f, 0.15f)
#define REDISH vec3(1.0f, 0.4f, 0.05f)

void main() {
    foutColor = vec4(REDISH, uIntensity);
    float distance = length(uSunPosition - finFragmentPosition);

    float distanceMultiplier = 0.0f;
    if (distance < CLOSE_DISTANCE) {
        // 0.5..1.0
        float modifier = 1.0f - (distance / CLOSE_DISTANCE); // scale: 1 is near, 0 is close

        distanceMultiplier = modifier / 2 + 0.5f;
    } else if (distance > END_DISTANCE) {
        // 0.0..0.3
        float modifier = (distance - END_DISTANCE) / (MAX_DISTANCE - END_DISTANCE) / 2; // scale

        distanceMultiplier = (modifier * modifier) * 0.3f;
    } else {
        // 0.3..0.5
        float modifier = 1.0f - (distance - CLOSE_DISTANCE) / (END_DISTANCE - CLOSE_DISTANCE); // scale

        distanceMultiplier = modifier * 0.2f + 0.3f;
    }
    foutColor.a *= distanceMultiplier;

    float yDistance = abs(finFragmentPosition.y * 5);
    if (yDistance > 0.8f) {
        // edge, far less intense than in the center
        foutColor.a *= (1.0f - yDistance) * 5;
    }

    // 0.0..0.3 -> completely red
    // 0.3..0.4 -> more yellow
    // 0.4..0.8 -> more yellow (linear)
    // 0.8..0.9 -> completely yellow
    // 0.8..1.0 -> yellow (but alpha getting close to 0)
    if (yDistance > 0.8f) {
        foutColor.rgb = YELLOWISH;
    } else if (yDistance > 0.4f) {
        float scaled = yDistance - 0.4f;
        scaled *= 10.0f / 4.0f;
        foutColor.rgb = mix(mix(foutColor.rgb, YELLOWISH, 0.1f), YELLOWISH, scaled);
    } else if (yDistance > 0.3f) {
        float scaled = yDistance - 0.3f;
        scaled *= 10.0f;
        foutColor.rgb = mix(foutColor.rgb, mix(foutColor.rgb, YELLOWISH, 0.1f), scaled * scaled);
    } // else nothing, already red
}
