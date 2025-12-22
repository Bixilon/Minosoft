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

package de.bixilon.minosoft.gui.rendering.gui.atlas.textures

import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kmath.vec.vec2.i.Vec2i
import de.bixilon.minosoft.gui.rendering.RenderUtil.fixUVEnd
import de.bixilon.minosoft.gui.rendering.RenderUtil.fixUVStart
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer.RGBA8Buffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer.TextureBuffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.loader.MemoryLoader
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture

class AtlasTexture(
    val size: Vec2i,
) {
    private val pixel = Vec2f(1.0f) / size
    val buffer = RGBA8Buffer(size)
    val texture = Texture(MemoryLoader { buffer }, 0)

    fun request(size: Vec2i): Vec2i? = null

    fun put(offset: Vec2i, source: TextureBuffer, start: Vec2i, size: Vec2i): CodeTexturePart {
        this.buffer.put(source, start, offset, size)

        return CodeTexturePart(this.texture, (pixel * offset).apply { unsafe.fixUVStart() }, (pixel * (offset + size)).apply { unsafe.fixUVEnd() }, size)
    }
}
