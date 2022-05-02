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

import de.bixilon.kotlinglm.mat4x4.Mat4
import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec2.Vec2t
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.system.base.texture.ShaderIdentifiable
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh
import de.bixilon.minosoft.gui.rendering.util.mesh.MeshStruct
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.orthoTimes
import de.bixilon.minosoft.util.collections.floats.AbstractFloatList
import de.bixilon.minosoft.util.collections.floats.DirectArrayFloatList

class GUIMesh(
    renderWindow: RenderWindow,
    val matrix: Mat4,
    data: DirectArrayFloatList,
) : Mesh(renderWindow, GUIMeshStruct, initialCacheSize = 40000, clearOnLoad = false, data = data), GUIVertexConsumer {

    override fun addVertex(position: Vec2t<*>, texture: ShaderIdentifiable, uv: Vec2, tint: RGBColor, options: GUIVertexOptions?) {
        addVertex(data, matrix, position, texture, uv, tint, options)
    }

    override fun addCache(cache: GUIMeshCache) {
        data.addAll(cache.data)
    }

    data class GUIMeshStruct(
        val position: Vec2,
        val uv: Vec2,
        val indexLayerAnimation: Int,
        val tintColor: RGBColor,
    ) {
        companion object : MeshStruct(GUIMeshStruct::class)
    }

    companion object {

        fun addVertex(data: AbstractFloatList, matrix: Mat4, position: Vec2t<*>, texture: ShaderIdentifiable, uv: Vec2, tint: RGBColor, options: GUIVertexOptions?) {
            val outPosition = matrix orthoTimes position
            var color = tint.rgba

            if (options != null) {
                options.tintColor?.let { color = tint.mix(it).rgba }

                if (options.alpha != 1.0f) {
                    val alpha = color and 0xFF
                    color = color and 0xFF.inv()

                    color = color or ((alpha * options.alpha).toInt() and 0xFF)
                }
            }

            data.add(outPosition.x)
            data.add(outPosition.y)
            data.add(uv.x)
            data.add(uv.y)
            data.add(Float.fromBits(texture.shaderId))
            data.add(Float.fromBits(color))
        }
    }

    override fun ensureSize(size: Int) {
        data.ensureSize(size)
    }
}
