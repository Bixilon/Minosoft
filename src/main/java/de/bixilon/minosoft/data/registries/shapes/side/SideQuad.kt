/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.registries.shapes.side

import de.bixilon.kmath.vec.vec2.f.Vec2f
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet

data class SideQuad(
    val min: Vec2f,
    val max: Vec2f,
) {

    constructor(minX: Float, minZ: Float, maxX: Float, maxZ: Float) : this(Vec2f(minOf(minX, maxX), minOf(minZ, maxZ)), Vec2f(maxOf(minX, maxX), maxOf(minZ, maxZ)))


    fun surfaceArea(): Float {
        val surface = (max.x - min.x) * (max.y - min.y)
        if (surface <= 0.0f) return 0.0f
        return surface
    }
}
