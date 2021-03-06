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

package de.bixilon.minosoft.gui.rendering.hud.elements

import de.bixilon.minosoft.gui.rendering.hud.HUDMesh
import de.bixilon.minosoft.gui.rendering.hud.atlas.HUDAtlasElement
import glm_.vec2.Vec2
import kotlin.math.abs

class ProgressBar(
    private val emptyAtlasElement: HUDAtlasElement,
    private val fullAtlasElement: HUDAtlasElement,
    private val hudElement: HUDElement,
) {
    val size: Vec2 = emptyAtlasElement.binding.size

    fun draw(hudMesh: HUDMesh, start: Vec2, end: Vec2, progress: Float, z: Int = 1) {
        //hudElement.drawImage(start, end, hudMesh, emptyAtlasElement, z)

        if (progress == 0.0f) {
            return
        }

        val xDiff = abs(end.x - start.x)

        val ourXDiff = xDiff * progress

        val textureStart = Vec2((fullAtlasElement.binding.start.x * fullAtlasElement.texture.widthFactor) / fullAtlasElement.texture.width.toFloat(), (fullAtlasElement.binding.start.y * fullAtlasElement.texture.heightFactor) / fullAtlasElement.texture.height.toFloat())
        var textureEnd = Vec2(((fullAtlasElement.binding.end.x + 1) * fullAtlasElement.texture.widthFactor) / (fullAtlasElement.texture.width + 1.0f), ((fullAtlasElement.binding.end.y + 1) * fullAtlasElement.texture.heightFactor) / (fullAtlasElement.texture.height + 1.0f))

        textureEnd = Vec2((textureEnd.x - textureStart.x) * progress, textureEnd.y)

        //   hudElement.drawImage(start, Vec2(start.x + ourXDiff, end.y), hudMesh, fullAtlasElement, textureStart, textureEnd, z + 1)
    }
}
