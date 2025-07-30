/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
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

import de.bixilon.minosoft.data.world.vec.vec2.f.Vec2f
import de.bixilon.kutil.collections.primitive.floats.AbstractFloatList
import de.bixilon.kutil.collections.primitive.floats.HeapArrayFloatList
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.RenderOrder
import de.bixilon.minosoft.gui.rendering.system.base.texture.shader.ShaderTexture

class GUIMeshCache(
    var halfSize: Vec2f,
    override val order: RenderOrder,
    val context: RenderContext,
    initialCacheSize: Int = 1000,
    var data: AbstractFloatList = HeapArrayFloatList(initialCacheSize),
) : GUIVertexConsumer {
    private val whiteTexture = context.textures.whiteTexture

    var revision: Long = 0
    var offset: Vec2f = Vec2f.EMPTY
    var options: GUIVertexOptions? = null

    fun clear() {
        data.clear()
    }


    override fun addVertex(x: Float, y: Float, texture: ShaderTexture?, u: Float, v: Float, tint: RGBAColor, options: GUIVertexOptions?) {
        GUIMesh.addVertex(data, halfSize, x, y, texture ?: whiteTexture.texture, u, v, tint, options)
        revision++
    }

    override fun addVertex(x: Float, y: Float, textureId: Float, u: Float, v: Float, tint: RGBAColor, options: GUIVertexOptions?) {
        GUIMesh.addVertex(data, halfSize, x, y, textureId, u, v, tint, options)
    }

    override fun addCache(cache: GUIMeshCache) {
        data.add(cache.data)
        revision++
    }

    override fun ensureSize(size: Int) {
        data.ensureSize(size)
    }
}
