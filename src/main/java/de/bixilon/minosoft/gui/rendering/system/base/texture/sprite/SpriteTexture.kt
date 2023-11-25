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

package de.bixilon.minosoft.gui.rendering.system.base.texture.sprite

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureStates
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureTransparencies
import de.bixilon.minosoft.gui.rendering.system.base.texture.array.TextureArrayProperties
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.TextureData
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.TextureRenderData
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.memory.MemoryTexture
import de.bixilon.minosoft.gui.rendering.textures.properties.ImageProperties
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY

@Deprecated("Slow, because of synchronized loading of mcmeta")
class SpriteTexture(private val original: Texture) : Texture {
    override var array: TextureArrayProperties by original::array
    override var properties: ImageProperties by original::properties
    override var renderData: TextureRenderData by original::renderData
    override val transparency: TextureTransparencies by original::transparency
    override val mipmaps: Int get() = original.mipmaps

    override var state: TextureStates = TextureStates.DECLARED
        private set

    override lateinit var data: TextureData
    override var size: Vec2i = Vec2i(-1, -1)
        private set

    var splitTextures: MutableList<MemoryTexture> = mutableListOf()


    @Synchronized
    override fun load(context: RenderContext) {
        if (state == TextureStates.LOADED) {
            return
        }
        original.load(context)
        val original = original.data.buffer

        val animationProperties = properties.animation!!
        size = Vec2i(animationProperties.width, animationProperties.height)


        for (i in 0 until animationProperties.frameCount) {
            val buffer = original.create(size)
            buffer.put(original, Vec2i(0, size.y), Vec2i.EMPTY, size)

            val splitTexture = MemoryTexture(size, mipmaps = this.original.mipmaps, buffer = buffer)

            splitTextures += splitTexture
        }

        state = TextureStates.LOADED
    }
}
