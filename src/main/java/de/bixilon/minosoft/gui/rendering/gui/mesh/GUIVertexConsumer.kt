/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
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

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec2.Vec2t
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.gui.atlas.TextureLike
import de.bixilon.minosoft.gui.rendering.system.base.texture.ShaderIdentifiable

interface GUIVertexConsumer {
    val order: Array<Pair<Int, Int>>

    fun addVertex(position: Vec2t<*>, texture: ShaderIdentifiable, uv: Vec2, tint: RGBColor, options: GUIVertexOptions?)

    fun addQuad(start: Vec2t<*>, end: Vec2t<*>, texture: ShaderIdentifiable, uvStart: Vec2 = Vec2(0.0f, 0.0f), uvEnd: Vec2 = Vec2(1.0f, 1.0f), tint: RGBColor, options: GUIVertexOptions?) {
        val positions = arrayOf(
            start,
            Vec2(end.x, start.y),
            end,
            Vec2(start.x, end.y),
        )
        val texturePositions = arrayOf(
            Vec2(uvEnd.x, uvStart.y),
            uvStart,
            Vec2(uvStart.x, uvEnd.y),
            uvEnd,
        )

        for ((vertexIndex, textureIndex) in order) {
            addVertex(positions[vertexIndex], texture, texturePositions[textureIndex], tint, options)
        }
    }

    fun addQuad(start: Vec2t<*>, end: Vec2t<*>, texture: TextureLike, tint: RGBColor, options: GUIVertexOptions?) {
        addQuad(start, end, texture.texture, texture.uvStart, texture.uvEnd, tint, options)
    }

    fun addCache(cache: GUIMeshCache)

    fun ensureSize(size: Int)
}
