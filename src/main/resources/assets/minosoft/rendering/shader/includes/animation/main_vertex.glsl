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

void run_animation() {
    uint animationIndex = vinIndexLayerAnimation & 0xFFFu;
    if (animationIndex == 0u) {
        finTextureIndex1 = vinIndexLayerAnimation >> 28u;
        finTextureCoordinates1 = vec3(vinUV, ((vinIndexLayerAnimation >> 12) & 0xFFFFu));
        finInterpolation = 0.0f;
        return;
    }

    uvec4 data = uAnimationData[animationIndex - 1u];
    uint texture1 = data.x;
    uint texture2 = data.y;
    uint interpolation = data.z;

    finTextureIndex1 = texture1 >> 28u;
    finTextureCoordinates1 = vec3(vinUV, ((texture1 >> 12) & 0xFFFFu));

    finTextureIndex2 = texture2 >> 28u;
    finTextureCoordinates2 = vec3(vinUV, ((texture2 >> 12) & 0xFFFFu));

    finInterpolation = interpolation / 100.0f;
}
