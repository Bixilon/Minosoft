/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
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

layout (location = 0) in vec3 vinPosition;
layout (location = 1) in vec2 vinUV;
layout (location = 2) in float vinPartTransformNormal; // part(0x1FD0000) transform (0x7F000), normal (0xFFF)

out vec3 finFragmentPosition;


uniform uint uIndexLayer;
uniform uint uTintColor;
uniform uint uSkinParts;

flat out uint finAllowTransparency;

out vec4 finTintColor;

#include "minosoft:skeletal/vertex"
#include "minosoft:color"
#include "minosoft:animation"

#define POSITIVE_INFINITY 1.0f / 0.0f

void main() {
    uint partTransformNormal = floatBitsToUint(vinPartTransformNormal);
    uint skinPart = (partTransformNormal >> 19u & 0xFFu);
    if (skinPart > 0u && ((1u << (skinPart - 1u)) & uSkinParts) == 0u) {
        gl_Position = vec4(POSITIVE_INFINITY);
        finTintColor.a = 0.0f;
        return;
    }
    finAllowTransparency = skinPart;
    run_skeletal(partTransformNormal, vinPosition);
    finTintColor *= getRGBColor(uTintColor & 0xFFFFFFu);

    setTexture(vinUV, uIndexLayer);
}
