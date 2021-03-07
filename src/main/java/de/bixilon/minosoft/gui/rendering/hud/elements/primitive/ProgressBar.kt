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

import de.bixilon.minosoft.gui.rendering.hud.HUDMesh
import de.bixilon.minosoft.gui.rendering.hud.atlas.ProgressBarAtlasElement
import de.bixilon.minosoft.gui.rendering.hud.atlas.TextureLike
import de.bixilon.minosoft.gui.rendering.textures.Texture
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2

class ProgressBar(
    start: Vec2,
    val end: Vec2,
    private val atlasElement: ProgressBarAtlasElement,
    val progress: Float = 0.0f,
    val z: Int = 1,
) : Element(start) {

    init {
        recalculateSize()
    }

    override fun recalculateSize() {
        size = atlasElement.emptyAtlasElement.binding.size
    }

    // fun draw(elementMesh: ElementMesh, start: Vec2, end: Vec2, progress: Float, z: Int = 1) {
    //   elementMesh.addElement(start, end, emptyAtlasElement, z)

    //   if (progress == 0.0f) {
    //       return
    //   }

    //   val xDiff = abs(end.x - start.x)

    //   val ourXDiff = xDiff * progress

    //   val textureStart = Vec2((fullAtlasElement.binding.start.x * fullAtlasElement.texture.widthFactor) / fullAtlasElement.texture.width.toFloat(), (fullAtlasElement.binding.start.y * fullAtlasElement.texture.heightFactor) / fullAtlasElement.texture.height.toFloat())
    //   var textureEnd = Vec2(((fullAtlasElement.binding.end.x + 1) * fullAtlasElement.texture.widthFactor) / (fullAtlasElement.texture.width + 1.0f), ((fullAtlasElement.binding.end.y + 1) * fullAtlasElement.texture.heightFactor) / (fullAtlasElement.texture.height + 1.0f))

    //   textureEnd = Vec2((textureEnd.x - textureStart.x) * progress, textureEnd.y)

    //   elementMesh.addElement(start, Vec2(start.x + ourXDiff, end.y), object : TextureLike {
    //       override val texture: Texture
    //           get() = fullAtlasElement.texture
    //       override val uvStart: Vec2
    //           get() = textureStart
    //       override val uvEnd: Vec2
    //           get() = textureEnd

    //   }, z + 1)
//    }

    override fun prepareVertices(start: Vec2, scaleFactor: Float, hudMesh: HUDMesh, matrix: Mat4, z: Int) {
        val emptyImageElement = ImageElement(this.start, end, atlasElement.emptyAtlasElement, z)

        emptyImageElement.prepareVertices(start, scaleFactor, hudMesh, matrix, z)

        if (progress == 0.0f) {
            return
        }


        val fullImageElement = ImageElement(this.start, Vec2(end.x * progress, end.y), object : TextureLike {
            override val texture: Texture
                get() = atlasElement.fullAtlasElement.texture
            override val uvStart: Vec2
                get() = atlasElement.fullAtlasElement.uvStart
            override val uvEnd: Vec2
                get() = Vec2((atlasElement.fullAtlasElement.uvEnd.x - atlasElement.fullAtlasElement.uvStart.x) * progress + atlasElement.fullAtlasElement.uvStart.x, atlasElement.fullAtlasElement.uvEnd.y)

        }, z + 1)

        fullImageElement.prepareVertices(start, scaleFactor, hudMesh, matrix, z)

    }
}
