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

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.assets.AssetsManager
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer.TextureBuffer
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.readTexture
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY

class AtlasTextureManager(private val context: RenderContext) {
    private val cache: MutableMap<ResourceLocation, TextureBuffer> = HashMap()
    private val textures: MutableList<AtlasTexture> = mutableListOf()

    private fun getTexture(texture: ResourceLocation, assets: AssetsManager): TextureBuffer {
        this.cache[texture]?.let { return it }

        val data = assets[texture].readTexture()
        this.cache[texture] = data

        return data
    }

    fun add(texture: ResourceLocation, assets: AssetsManager, start: Vec2i, end: Vec2i, resolution: Vec2i): CodeTexturePart {
        val buffer = getTexture(texture, assets)
        val scale = buffer.size / resolution

        val realStart = start * scale
        val realEnd = end * scale
        val size = realEnd - realStart

        val target = request(size)

        return target.put(buffer, realStart, size)
    }

    private fun request(size: Vec2i): Target {
        for (texture in textures) {
            val offset = texture.request(size) ?: continue
            return Target(texture, offset)
        }

        val target = createTarget(size)
        this.textures += target.texture

        return target
    }

    private fun createTarget(minSize: Vec2i): Target {
        // TODO: create larger atlases to optimize space
        return Target(AtlasTexture(minSize), Vec2i.EMPTY)
    }


    fun load() {
        for (texture in textures) {
            context.textures.static += texture
        }
        this.cache.clear()
        this.textures.clear()
    }

    private data class Target(
        val texture: AtlasTexture,
        val offset: Vec2i,
    ) {

        fun put(source: TextureBuffer, start: Vec2i, size: Vec2i): CodeTexturePart {
            return texture.put(offset, source, start, size)
        }
    }
}
