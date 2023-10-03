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

package de.bixilon.minosoft.gui.rendering.gui.atlas

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.assets.AssetsManager
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.gui.atlas.textures.AtlasTextureManager
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.readTexture
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.toVec2i
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap

class RawAtlasElement(
    val texture: ResourceLocation,
    val resolution: Vec2i = RESOLUTION,
    val start: Vec2i = START,
    val end: Vec2i = resolution,
    val slots: Int2ObjectMap<AtlasArea>?,
    val areas: Map<String, AtlasArea>?,
    val assets: AssetsManager, // used for caching
) {

    fun load(textures: AtlasTextureManager): AtlasElement {
        val textureData = assets[texture].readTexture()

        val (texture, uvStart, uvEnd) = textures.add(textureData, start, end, resolution)
        val size = (end + 1) - start


        return AtlasElement(texture, uvStart, uvEnd, size, slots, areas)
    }

    companion object {
        val RESOLUTION = Vec2i(256, 256)
        val START = Vec2i(0, 0)


        private fun Any.toAreas(): Map<String, AtlasArea> {
            if (this !is Map<*, *>) throw IllegalArgumentException("Not a JsonObject!")
            val areas: MutableMap<String, AtlasArea> = HashMap()

            for ((name, data) in this) {
                areas[name.toString()] = AtlasArea.deserialize(data.unsafeCast())
            }
            return areas
        }

        private fun Any.toSlots(): Int2ObjectMap<AtlasArea> {
            if (this !is Map<*, *>) throw IllegalArgumentException("Not a JsonObject!")
            val areas = Int2ObjectOpenHashMap<AtlasArea>()

            for ((id, data) in this) {
                areas[id.toInt()] = AtlasArea.deserialize(data.unsafeCast())
            }
            return areas
        }

        fun deserialize(texture: ResourceLocation, assets: AssetsManager, data: JsonObject): RawAtlasElement {
            val resolution = data["resolution"]?.toVec2i() ?: RESOLUTION
            val start = data["start"]?.toVec2i() ?: START
            val end = data["end"]?.toVec2i() ?: resolution

            val slots = data["slots"]?.toSlots()
            val areas = data["areas"]?.toAreas()

            return RawAtlasElement(texture, resolution, start, end, slots, areas, assets)
        }
    }
}
