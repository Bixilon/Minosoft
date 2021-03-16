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

package de.bixilon.minosoft.gui.rendering.hud.elements.primitive

import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.hud.atlas.TextureLike
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4

class ImageElement(
    start: Vec2 = Vec2(0, 0),
    var textureLike: TextureLike?,
    end: Vec2 = textureLike?.size ?: Vec2(0, 0),
    val z: Int = 0,
    val tintColor: RGBColor? = null,
) : EndElement(start, end) {
    init {
        recalculateSize()
    }

    override fun recalculateSize() {
        size = end - start
        parent?.recalculateSize()
    }


    private fun addToStart(start: Vec2, elementPosition: Vec2): Vec2 {
        return Vec2(start.x + elementPosition.x, start.y - elementPosition.y)
    }

    private fun addToEnd(start: Vec2, elementPosition: Vec2): Vec2 {
        return Vec2(start.x + elementPosition.x, start.y - elementPosition.y)
    }

    override fun prepareCache(start: Vec2, scaleFactor: Float, matrix: Mat4, z: Int) {
        val ourStart = addToStart(start, this.start * scaleFactor)
        val modelStart = matrix * Vec4(ourStart, 1.0f, 1.0f)
        val ourEnd = addToEnd(start, this.end * scaleFactor)
        val modelEnd = matrix * Vec4(ourEnd, 1.0f, 1.0f)

        val uvStart = textureLike?.uvStart ?: Vec2()
        val uvEnd = textureLike?.uvEnd ?: Vec2()

        val realZ = RenderConstants.HUD_Z_COORDINATE + RenderConstants.HUD_Z_COORDINATE_Z_FACTOR * (this.z + z)

        fun addVertex(position: Vec2, textureUV: Vec2) {
            cache.addVertex(Vec3(position, realZ), textureUV, textureLike?.texture, tintColor)
        }

        addVertex(Vec2(modelStart.x, modelStart.y), Vec2(uvStart.x, uvStart.y))
        addVertex(Vec2(modelStart.x, modelEnd.y), Vec2(uvStart.x, uvEnd.y))
        addVertex(Vec2(modelEnd.x, modelStart.y), Vec2(uvEnd.x, uvStart.y))
        addVertex(Vec2(modelStart.x, modelEnd.y), Vec2(uvStart.x, uvEnd.y))
        addVertex(Vec2(modelEnd.x, modelEnd.y), Vec2(uvEnd.x, uvEnd.y))
        addVertex(Vec2(modelEnd.x, modelStart.y), Vec2(uvEnd.x, uvStart.y))
    }
}
