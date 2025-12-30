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

#define FOG

out lowp vec4 foutColor;


#include "minosoft:tint"
#include "minosoft:texture"
#include "minosoft:alpha"
#include "minosoft:fog"
#include "minosoft:animation"

flat in uint finAllowTransparency;

void main() {
    if (finTintColor.a == 0.0f) discard;
    applyDefaults();
    applyTint();
    vec4 texel = getTexture(finTextureArray, vec3(finTextureUV, finTextureLayer));
    if (finAllowTransparency != 0u) {
        if (texel.a < 0.5f) discard;
    } else {
        texel.a = 1.0f;
    }

    foutColor *= texel;

    #ifdef FOG
    fog_set();
    #endif
}
