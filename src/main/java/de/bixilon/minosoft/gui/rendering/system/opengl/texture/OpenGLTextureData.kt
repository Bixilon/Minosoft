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

package de.bixilon.minosoft.gui.rendering.system.opengl.texture

import de.bixilon.minosoft.data.world.vec.vec2.f.Vec2f
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.TextureRenderData

class OpenGLTextureData(
    val array: Int,
    val index: Int,
    val uvEnd: Vec2f?,
    override val animationData: Int = -1,
) : TextureRenderData {
    override val shaderTextureId: Int = (array shl 28) or (index shl 12) or (animationData + 1)

    override fun transformUV(end: Vec2f?): Vec2f {
        if (end == null) {
            return uvEnd ?: VEC2_ONE
        }
        if (uvEnd == null) {
            return end
        }
        return Vec2f(end.x * uvEnd.x, end.y * uvEnd.y)
    }

    companion object {
        private val VEC2_ONE = Vec2f(1.0f, 1.0f)
    }
}
