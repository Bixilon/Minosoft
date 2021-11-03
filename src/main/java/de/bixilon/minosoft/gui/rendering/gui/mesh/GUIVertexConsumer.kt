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
import de.bixilon.minosoft.gui.rendering.gui.hud.atlas.TextureLike
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh
import glm_.vec2.Vec2
import glm_.vec2.Vec2t

interface GUIVertexConsumer {

    fun addVertex(position: Vec2t<*>, z: Int, texture: AbstractTexture, uv: Vec2, tint: RGBColor, options: GUIVertexOptions?)

    fun addQuad(start: Vec2t<*>, end: Vec2t<*>, z: Int, texture: AbstractTexture, uvStart: Vec2, uvEnd: Vec2, tint: RGBColor, options: GUIVertexOptions?) {
        val positions = arrayOf(
            Vec2(start.x.toFloat(), start.y),
            Vec2(end.x.toFloat(), start.y),
            end,
            Vec2(start.x, end.y),
        )
        val texturePositions = arrayOf(
            Vec2(uvEnd.x, uvStart.y),
            uvStart,
            Vec2(uvStart.x, uvEnd.y),
            uvEnd,
        )

        for ((vertexIndex, textureIndex) in Mesh.QUAD_DRAW_ODER) {
            addVertex(positions[vertexIndex], z, texture, texturePositions[textureIndex], tint, options)
        }
    }

    fun addQuad(start: Vec2t<*>, end: Vec2t<*>, z: Int, texture: TextureLike, tint: RGBColor, options: GUIVertexOptions?) {
        addQuad(start, end, z, texture.texture, texture.uvStart, texture.uvEnd, tint, options)
    }

    fun addCache(cache: GUIMeshCache)
}
