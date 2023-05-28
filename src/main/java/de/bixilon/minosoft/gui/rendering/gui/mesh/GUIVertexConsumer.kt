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
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.gui.atlas.TextureLike
import de.bixilon.minosoft.gui.rendering.system.base.texture.ShaderIdentifiable

interface GUIVertexConsumer {
    val order: Array<Pair<Int, Int>>

    fun addVertex(position: Vec2, texture: ShaderIdentifiable, uv: Vec2, tint: RGBColor, options: GUIVertexOptions?)
    fun addVertex(position: Vec2i, texture: ShaderIdentifiable, uv: Vec2, tint: RGBColor, options: GUIVertexOptions?) {
        addVertex(Vec2(position), texture, uv, tint, options)
    }

    fun addQuad(start: Vec2, end: Vec2, texture: ShaderIdentifiable, uvStart: Vec2 = UV_START, uvEnd: Vec2 = UV_END, tint: RGBColor, options: GUIVertexOptions?) {
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

    fun addQuad(start: Vec2i, end: Vec2i, texture: ShaderIdentifiable, uvStart: Vec2 = UV_START, uvEnd: Vec2 = UV_END, tint: RGBColor, options: GUIVertexOptions?) {
        val positions = arrayOf(
            Vec2(start),
            Vec2(end.x, start.y),
            Vec2(end),
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

    fun addQuad(start: Vec2, end: Vec2, texture: TextureLike, tint: RGBColor, options: GUIVertexOptions?) {
        addQuad(start, end, texture.texture, texture.uvStart, texture.uvEnd, tint, options)
    }

    fun addQuad(start: Vec2i, end: Vec2i, texture: TextureLike, tint: RGBColor, options: GUIVertexOptions?) {
        addQuad(start, end, texture.texture, texture.uvStart, texture.uvEnd, tint, options)
    }

    fun addCache(cache: GUIMeshCache)

    fun ensureSize(size: Int)

    companion object {
        val UV_START = Vec2(0.0f, 0.0f)
        val UV_END = Vec2(1.0f, 1.0f)
    }
}
