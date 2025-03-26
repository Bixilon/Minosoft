/*
 * Minosoft
 * Copyright (C) 2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
 
#ifndef VSH
#define VSH


#define UV_UNPACK_BITS 12u
#define UV_UNPACK_MASK ((1u << UV_UNPACK_BITS) - 1u)

mediump vec2 uv_unpack(uint raw) {
    float x = float((raw >> (1u * UV_UNPACK_BITS)) & UV_UNPACK_MASK) * (1.0f / float(UV_UNPACK_MASK));
    float y = float((raw >> (0u * UV_UNPACK_BITS)) & UV_UNPACK_MASK) * (1.0f / float(UV_UNPACK_MASK));
    
    return vec2(x, y);
}


#endif
