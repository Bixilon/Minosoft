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

package de.bixilon.minosoft.gui.rendering.system.opengl.texture

import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.TextureRenderData
import glm_.vec2.Vec2

class OpenGLTextureData(
    val array: Int,
    val index: Int,
    val uvEnd: Vec2?,
    override val animationData: Int = -1,
) : TextureRenderData {
    private val uvEndArray = uvEnd?.array
    override val shaderTextureId: Int = (array shl 28) or (index shl 12) or (animationData + 1)

    override fun transformUV(end: Vec2?): Vec2 {
        if (end == null) {
            return uvEnd ?: VEC2_ONE
        }
        if (uvEndArray == null) {
            return end
        }
        return Vec2(end.x * uvEndArray[0], end.y * uvEndArray[1])
    }

    override fun transformUV(end: FloatArray?): FloatArray {
        if (end == null) {
            return uvEndArray ?: ONE_ARRAY
        }
        if (uvEndArray == null) {
            return end
        }
        return floatArrayOf(end[0] * uvEndArray[0], end[1] * uvEndArray[1])
    }

    companion object {
        private val VEC2_ONE = Vec2(1.0f, 1.0f)
        private val ONE_ARRAY = VEC2_ONE.array
    }
}
