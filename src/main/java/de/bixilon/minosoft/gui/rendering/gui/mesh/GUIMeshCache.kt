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

package de.bixilon.minosoft.gui.rendering.gui.mesh

import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY
import de.bixilon.minosoft.util.collections.ArrayFloatList
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec2.Vec2t

class GUIMeshCache(
    val matrix: Mat4,
    initialCacheSize: Int = 1000,
) : GUIVertexConsumer {
    val data: ArrayFloatList = ArrayFloatList(initialCacheSize)
    var offset: Vec2i = Vec2i.EMPTY
    var z: Int = 0
    var maxZ: Int = 0

    override fun addVertex(position: Vec2t<*>, z: Int, texture: AbstractTexture, uv: Vec2, tint: RGBColor, options: GUIVertexOptions?) {
        data.addAll(GUIMesh.createVertex(matrix, position, z, texture, uv, tint, options))
    }

    override fun addCache(cache: GUIMeshCache) {
        data.addAll(cache.data)
    }
}
