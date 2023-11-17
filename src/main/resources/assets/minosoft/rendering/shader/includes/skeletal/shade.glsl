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
#define DEGREE_90 1.5707964f

float decodeNormalY(uint data) {
    if (data < 8u) return (data / 8.0f) - 1.0f;
    return (data - 8u) / 7.0f;
}

vec3 decodeNormal(uint normal) {
    uint x = normal & 0x0Fu;
    uint y = normal >> 8u & 0x0Fu;
    uint z = normal >> 4u & 0x0Fu;
    return vec3(x / 15.0f, decodeNormalY(y), z / 15.0f);
}

vec3 transformNormal(vec3 normal, mat4 transform) {
    //  return normalize(mat3(transpose(inverse(transform))) * normal);
    //   return mat3(transpose(inverse(transform))) * normal;
    return normalize(mat3(transform) * normal);
}

float interpolateShade(float normal, float max) {
    float delta = normal;
    if (delta < 0.0f) delta = -delta;
    if (delta <= 0.003f) return 0.0f;
    if (delta >= 1.0f) return max;
    delta = asin(delta) / DEGREE_90;  // asin is just defined in |x| <= 1

    return delta * max;
}

float getShade(vec3 normal) {
    float x = interpolateShade(normal.x, 0.6f);
    float y;
    if (normal.y < 0.0f) {
        y = interpolateShade(normal.y, 0.5f);
    } else {
        y = interpolateShade(normal.y, 1.0f);
    }
    float z = interpolateShade(normal.z, 0.8f);

    return (x + y + z);
}
