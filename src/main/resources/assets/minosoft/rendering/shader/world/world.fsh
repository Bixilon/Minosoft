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

out vec4 foutColor;

flat in uint finTextureIndex1;
in vec3 finTextureCoordinates1;
flat in uint finTextureIndex2;
in vec3 finTextureCoordinates2;
in float finInterpolation;

in vec4 finTintColor;

#include "minosoft:texture"

void main() {
    vec4 firstTexelColor = getTexture(finTextureIndex1, finTextureCoordinates1);
    if (firstTexelColor.a == 0.0f) {
        discard;
    }

    if (finInterpolation == 0.0f) {
        foutColor = firstTexelColor * finTintColor;
        #ifdef TRANSPARENT
        if (foutColor.a < 0.3f){
            discard;
        } else {
            foutColor.a = 1.0f;
        }
            #endif
        return;
    }

    vec4 secondTexelColor = getTexture(finTextureIndex2, finTextureCoordinates2);

    if (secondTexelColor.a == 0.0f) {
        discard;
    }

    foutColor = mix(firstTexelColor, secondTexelColor, finInterpolation) * finTintColor;

    #ifdef TRANSPARENT
    if (foutColor.a < 0.3f) {
        discard;
    } else {
        foutColor.a = 1.0f;
    }
        #endif
}
