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

#version 330 core

in vec2 finUV;

out vec4 foutColor;

uniform sampler2D uColor;
uniform sampler2D uDepth;


void main() {
    vec4 color = texture(uColor, finUV);
    if (color.a == 0.0f) {
        discard;
    }
    float depth = texture(uDepth, finUV).r;
    foutColor = vec4(color.xyz, depth);

    if (foutColor.a == 0.0f) {
        discard;
    }
}
