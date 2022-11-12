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

uniform sampler2D uTexture;

#include "minosoft:alpha"

const vec3 BLACK = vec3(0.0f);
const vec3 WHITE = vec3(1.0f);

void main() {
    foutColor = texture(uTexture, finUV);
    discard_alpha();
    float gray = (foutColor.r + foutColor.g + foutColor.b) / 3.0f;
    if (gray < 0.5f) {
        foutColor.rgb = BLACK;
    } else {
        foutColor.rgb = WHITE;
    }
}
