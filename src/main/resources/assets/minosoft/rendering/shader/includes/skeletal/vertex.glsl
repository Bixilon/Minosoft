/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

uniform mat4 uViewProjectionMatrix;

#include "minosoft:skeletal/buffer"
#include "minosoft:skeletal/shade"


void run_skeletal(uint inTransformNormal, vec3 inPosition) {
    mat4 transform = uSkeletalTransforms[(inTransformNormal >> 12u) & 0x7Fu];
    vec4 position = transform * vec4(inPosition, 1.0f);
    gl_Position = uViewProjectionMatrix * position;
    vec3 normal = transformNormal(decodeNormal(inTransformNormal & 0xFFFu), transform);

    finTintColor = vec4(vec3(getShade(normal)), 1.0f);
    finFragmentPosition = position.xyz;
}
