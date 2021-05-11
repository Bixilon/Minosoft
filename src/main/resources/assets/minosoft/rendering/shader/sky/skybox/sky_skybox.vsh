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

layout (location = 0) in vec3 inPosition;
out vec4 color;

uniform mat4 skyViewProjectionMatrix;

uniform vec4 bottomColor;
uniform vec4 topColor;

void main() {
    gl_Position = (skyViewProjectionMatrix * vec4(inPosition, 1.0)).xyww;

    if (inPosition.y < 0.5f) {
        color = bottomColor;
    } else {
        color = topColor;
    }
}
