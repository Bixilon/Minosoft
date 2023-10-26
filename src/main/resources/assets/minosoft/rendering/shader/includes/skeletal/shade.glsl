/*
 * Minosoft
 * Copyright (C) 2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

float decodeNormal(uint data) {
    return (data / 15.0f) * 2.0f - 1.0f;
}

vec3 decodeNormal(float normal) {
    uint combined = floatBitsToUint(normal);
    uint x = combined & 0x0Fu;
    uint y = combined >> 8u & 0x0Fu;
    uint z = combined >> 4u & 0x0Fu;
    return vec3(decodeNormal(x), decodeNormal(y), decodeNormal(z));
}

vec3 transformNormal(vec3 normal, mat4 transform) {
    //  return normalize(mat3(transpose(inverse(transform))) * normal);
    return mat3(transform) * normal;
}

float getShade(vec3 normal) {
    if (normal.y < -0.5f) return 0.5f;
    if (normal.y > 0.5f) return 1.0f;
    if (normal.x < -0.5f || normal.x > 0.5f) return 0.6f;
    if (normal.z < -0.5f || normal.z > 0.5f) return 0.8f;

    return 1.0f;
}
