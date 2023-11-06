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

package de.bixilon.minosoft.gui.rendering.gui.atlas.textures

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureStates
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureTransparencies
import de.bixilon.minosoft.gui.rendering.system.base.texture.array.TextureArrayProperties
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.TextureData
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.TextureRenderData
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil
import de.bixilon.minosoft.gui.rendering.textures.properties.ImageProperties

class AtlasTexture(
    override val size: Vec2i,
) : Texture {
    override val transparency: TextureTransparencies = TextureTransparencies.TRANSLUCENT
    override val mipmaps: Boolean get() = false
    private val pixel = Vec2(1.0f) / size

    override lateinit var array: TextureArrayProperties
    override lateinit var renderData: TextureRenderData
    override var data: TextureData = TextureData(size)
    override var properties = ImageProperties.DEFAULT
    override val state: TextureStates = TextureStates.LOADED

    override fun load(context: RenderContext) = Unit

    fun request(size: Vec2i): Vec2i? = null

    fun put(offset: Vec2i, source: TextureData, start: Vec2i, size: Vec2i): CodeTexturePart {
        TextureUtil.copy(start, source, offset, this.data, size)

        return CodeTexturePart(this, pixel * offset, pixel * (offset + size), size)
    }
}
