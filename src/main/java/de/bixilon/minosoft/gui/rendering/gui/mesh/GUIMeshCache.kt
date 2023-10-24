/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
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
import de.bixilon.kutil.collections.primitive.floats.AbstractFloatList
import de.bixilon.kutil.collections.primitive.floats.HeapArrayFloatList
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.RenderOrder
import de.bixilon.minosoft.gui.rendering.system.base.texture.shader.ShaderTexture
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util.EMPTY

class GUIMeshCache(
    var halfSize: Vec2,
    override val order: RenderOrder,
    val context: RenderContext,
    initialCacheSize: Int = 1000,
    var data: AbstractFloatList = HeapArrayFloatList(initialCacheSize),
) : GUIVertexConsumer {
    private val whiteTexture = context.textures.whiteTexture

    var revision: Long = 0
    var offset: Vec2 = Vec2.EMPTY
    var options: GUIVertexOptions? = null

    fun clear() {
        if (data.finished) {
            data = HeapArrayFloatList(initialSize = data.size)
        } else {
            data.clear()
        }
    }


    override fun addVertex(x: Float, y: Float, texture: ShaderTexture?, u: Float, v: Float, tint: RGBColor, options: GUIVertexOptions?) {
        GUIMesh.addVertex(data, halfSize, x, y, texture ?: whiteTexture.texture, u, v, tint, options)
        revision++
    }

    override fun addCache(cache: GUIMeshCache) {
        data.add(cache.data)
        revision++
    }

    override fun ensureSize(size: Int) {
        data.ensureSize(size)
    }
}
