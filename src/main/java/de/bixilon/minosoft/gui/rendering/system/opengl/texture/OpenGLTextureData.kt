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
    val uvEnd: Vec2,
    override val animationData: Int = -1,
) : TextureRenderData {
    override val layer: Int = (array shl 24) or (index)

    override fun transformUV(end: Vec2): Vec2 {
        return end * uvEnd
    }
}
