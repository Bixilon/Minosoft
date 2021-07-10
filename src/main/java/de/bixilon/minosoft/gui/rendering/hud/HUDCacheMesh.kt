/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.hud

import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import de.bixilon.minosoft.util.collections.ArrayFloatList
import glm_.vec2.Vec2
import glm_.vec3.Vec3

class HUDCacheMesh(
    initialCacheSize: Int = 1000,
) {
    private val data = ArrayFloatList(initialCacheSize)

    val cache: ArrayFloatList
        get() = data

    fun addVertex(position: Vec3, textureCoordinates: Vec2, texture: AbstractTexture?, tintColor: RGBColor? = null) {
        data.addAll(floatArrayOf(
            position.x,
            position.y,
            position.z,
            textureCoordinates.x,
            textureCoordinates.y,
            Float.fromBits(texture?.renderData?.layer ?: RenderConstants.DEBUG_TEXTURE_ID),
            if (tintColor == null) {
                0.0f
            } else {
                Float.fromBits(tintColor.rgba)
            },
        ))
    }

    val size: Int
        get() = data.size

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
