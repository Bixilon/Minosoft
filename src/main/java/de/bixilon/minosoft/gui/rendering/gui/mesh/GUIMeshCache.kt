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
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import de.bixilon.minosoft.util.collections.ArrayFloatList
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec2.Vec2t
import glm_.vec4.Vec4

class GUIMeshCache(
    val matrix: Mat4,
    initialCacheSize: Int = 1000,
) : GUIVertexConsumer {
    val data: ArrayFloatList = ArrayFloatList(initialCacheSize)

    override fun addVertex(position: Vec2t<*>, z: Int, texture: AbstractTexture, uv: Vec2, tint: RGBColor) {
        val outPosition = matrix * Vec4(position.x.toFloat(), position.y.toFloat(), 1.0f, 1.0f)
        data.addAll(floatArrayOf(
            outPosition.x,
            outPosition.y,
            GUIMesh.BASE_Z + GUIMesh.Z_MULTIPLIER * z,
            uv.x,
            uv.y,
            Float.fromBits(texture.renderData?.layer ?: RenderConstants.DEBUG_TEXTURE_ID),
            Float.fromBits(tint.rgba),
        ))
    }

    override fun addCache(cache: GUIMeshCache) {
        data.addAll(cache.data)
    }
}
