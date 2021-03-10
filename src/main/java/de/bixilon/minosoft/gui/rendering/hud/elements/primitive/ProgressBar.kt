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

import de.bixilon.minosoft.gui.rendering.hud.atlas.HUDAtlasElement
import de.bixilon.minosoft.gui.rendering.hud.atlas.TextureLike
import de.bixilon.minosoft.gui.rendering.textures.Texture
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2

class ProgressBar(
    start: Vec2,
    end: Vec2,
    val emptyAtlasElement: HUDAtlasElement,
    val fullAtlasElement: HUDAtlasElement,
    val z: Int = 1,
) : EndElement(start, end) {
    private var _progress = 0.0f
    var progress: Float
        get() = _progress
        set(value) {
            check(value >= 0.0f) { "Value is smaller than 0" }
            check(value <= 1.0f) { "Value is greater than 1" }
            cache.clear()
            _progress = value
        }

    init {
        recalculateSize()
    }

    override fun recalculateSize() {
        size = emptyAtlasElement.binding.size
    }

    override fun prepareCache(start: Vec2, scaleFactor: Float, matrix: Mat4, z: Int) {
        val emptyImageElement = ImageElement(this.start, end, emptyAtlasElement, z)

        emptyImageElement.checkCache(start, scaleFactor, matrix, z)
        cache.addCache(emptyImageElement.cache)

        if (progress == 0.0f) {
            return
        }


        val fullImageElement = ImageElement(this.start, Vec2(end.x * progress, end.y), object : TextureLike {
            override val texture: Texture
                get() = fullAtlasElement.texture
            override val uvStart: Vec2
                get() = fullAtlasElement.uvStart
            override val uvEnd: Vec2
                get() = Vec2((fullAtlasElement.uvEnd.x - fullAtlasElement.uvStart.x) * progress + fullAtlasElement.uvStart.x, fullAtlasElement.uvEnd.y)

        }, z + 1)

        fullImageElement.checkCache(start, scaleFactor, matrix, z)
        cache.addCache(fullImageElement.cache)
    }
}
