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

package de.bixilon.minosoft.gui.rendering.system.dummy.texture

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.TextureRenderData
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util.EMPTY

object DummyTextureRenderData : TextureRenderData {
    override val shaderTextureId: Int = 0
    override val animationData: Int = -1

    override fun transformUV(end: Vec2?): Vec2 {
        return end ?: Vec2.EMPTY
    }

    override fun transformUV(end: FloatArray?): FloatArray {
        return end ?: floatArrayOf(1.0f, 1.0f)
    }
}
