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

#ifndef INCLUDE_MINOSOFT_TINT
#define INCLUDE_MINOSOFT_TINT

#ifdef SHADER_TYPE_VERTEX
out vec4 finTintColor;
#endif

#ifdef SHADER_TYPE_FRAGMENT
in vec4 finTintColor;


void applyTint() {
    if (finTintColor.a <= 0.0f) discard;
    foutColor *= finTintColor;
}
#endif


#endif
