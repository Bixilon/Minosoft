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

import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.TextureRenderData
import de.bixilon.minosoft.gui.rendering.util.mesh.uv.PackedUV

class OpenGlTextureData(
    val array: Int,
    val index: Int,
    val uvEnd: Vec2f?,
    override val animationData: Int = -1,
) : TextureRenderData {
    override val shaderTextureId: Int = (array shl 28) or (index shl 12) or (animationData + 1)

    override fun transformUV(uv: Vec2f): Vec2f {
        if (uvEnd == null) return uv

        return Vec2f(uv.x * uvEnd.x, uv.y * uvEnd.y)
    }

    override fun transformU(u: Float): Float {
        val uvEnd = this.uvEnd ?: return u

        return uvEnd.x * u
    }

    override fun transformV(v: Float): Float {
        val uvEnd = this.uvEnd ?: return v

        return uvEnd.y * v
    }

    override fun transformUV(u: Float, v: Float): PackedUV {
        val uvEnd = this.uvEnd ?: return PackedUV(u, v)

        return PackedUV(uvEnd.x * u, uvEnd.y * v)
    }

    override fun transformUV(uv: PackedUV): PackedUV {
        val uvEnd = this.uvEnd ?: return uv

        return PackedUV(uv.u * uvEnd.x, uv.v * uvEnd.y)
    }
}
