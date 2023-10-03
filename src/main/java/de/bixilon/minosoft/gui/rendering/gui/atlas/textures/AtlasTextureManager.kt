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
import de.bixilon.minosoft.assets.AssetsManager
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.TextureData
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.memory.MemoryTexture
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.memory.TextureGenerator
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.readTexture
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY
import java.nio.ByteBuffer

class AtlasTextureManager(private val context: RenderContext) {
    private val cache: MutableMap<ResourceLocation, TextureData> = HashMap()
    private val targets: MutableList<Target> = mutableListOf()

    private fun getTexture(texture: ResourceLocation, assets: AssetsManager): TextureData {
        this.cache[texture]?.let { return it }

        val data = assets[texture].readTexture()
        this.cache[texture] = data

        return data
    }

    fun add(texture: ResourceLocation, assets: AssetsManager, start: Vec2i, end: Vec2i, resolution: Vec2i): AtlasTexture {
        val textureData = getTexture(texture, assets)
        val scale = textureData.size / resolution

        val realStart = start * scale
        val realEnd = end * scale
        val size = (realEnd + 1) - realStart

        val target = request(size)

        return target.put(textureData.buffer, textureData.size, realStart, realEnd)
    }

    private fun request(size: Vec2i): Target {
        // TODO: optimize that
        val target = Target(Vec2i.EMPTY, TextureGenerator.allocate(size), size)
        this.targets += target

        return target
    }


    fun load() {
        for (target in targets) {
            context.textures.staticTextures += target.texture
        }
        this.cache.clear()
        targets.clear()
    }

    private data class Target(
        val offset: Vec2i,
        val buffer: ByteBuffer,
        val size: Vec2i,
    ) {
        val texture = MemoryTexture(size, mipmaps = false, buffer = buffer)

        fun put(source: ByteBuffer, size: Vec2i, start: Vec2i, end: Vec2i): AtlasTexture {
            // TODO

            return AtlasTexture(texture, Vec2(0, 0), Vec2(1, 1))
        }
    }

}
