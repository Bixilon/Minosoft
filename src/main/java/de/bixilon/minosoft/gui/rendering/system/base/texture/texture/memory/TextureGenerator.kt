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

package de.bixilon.minosoft.gui.rendering.system.base.texture.texture.memory

import de.bixilon.kotlinglm.vec2.Vec2i
import de.matthiasmann.twl.utils.PNGDecoder
import org.lwjgl.BufferUtils

object TextureGenerator {

    fun getBufferSize(size: Vec2i) = size.x * size.y * PNGDecoder.Format.RGBA.numComponents

    fun allocate(size: Vec2i) = BufferUtils.createByteBuffer(getBufferSize(size))
}
