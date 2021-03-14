/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.hud

import de.bixilon.minosoft.data.text.RGBColor
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import org.apache.commons.collections.primitives.ArrayFloatList

class HUDCacheMesh {
    private val data = ArrayFloatList()

    val cache: ArrayFloatList
        get() = data

    fun addVertex(position: Vec3, textureCoordinates: Vec2, textureLayer: Int, tintColor: RGBColor? = null) {
        data.add(position.x)
        data.add(position.y)
        data.add(position.z)
        data.add(textureCoordinates.x)
        data.add(textureCoordinates.y)
        data.add(Float.fromBits(textureLayer))
        if (tintColor == null) {
            data.add(0f)
        } else {
            data.add(Float.fromBits(tintColor.color))
        }
    }

    val size: Int
        get() = data.size()

    fun isEmpty(): Boolean {
        return data.isEmpty
    }

    fun clear() {
        data.clear()
    }

    fun addCache(cache: HUDCacheMesh) {
        data.addAll(cache.data)
    }
}
