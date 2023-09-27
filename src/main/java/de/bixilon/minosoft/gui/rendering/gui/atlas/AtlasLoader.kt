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

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.json.JsonObject
import de.bixilon.minosoft.assets.util.InputStreamUtil.readJsonObject
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.identified.ResourceLocationUtil.extend
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class AtlasLoader(val context: RenderContext) {
    private val raw: MutableMap<ResourceLocation, Map<String, RawAtlasElement>> = HashMap()


    fun load(id: String, packFormat: Int, data: JsonObject): RawAtlasElement? {
        val textureName = data["texture"]?.toResourceLocation()?.texture() ?: throw IllegalArgumentException("Missing texture!")

        val assets = context.connection.assetsManager.getAssetsManager(textureName) ?: return null
        // TODO: check packFormat match

        return RawAtlasElement.deserialize(textureName, assets, data)
    }

    fun load(id: String, data: JsonObject): RawAtlasElement? {
        // TODO: sort all by versionId (descending) and load first best matching
        // try to load first best
        TODO()
    }

    fun load(name: ResourceLocation, data: JsonObject) {
        val elements: MutableMap<String, RawAtlasElement> = hashMapOf()

        for ((id, element) in data) {
            elements[id] = load(id, element.unsafeCast()) ?: continue
        }

        raw[name] = elements
    }

    fun load(name: ResourceLocation) {
        val file = name.atlas()
        val data = context.connection.assetsManager[file].readJsonObject()

        load(name, data)
    }

    fun load(): Map<ResourceLocation, Atlas> {
        val map: MutableMap<ResourceLocation, Atlas> = HashMap()
        val textures = AtlasTextureManager(context)

        for ((atlasName, atlas) in this.raw) {
            val data: MutableMap<String, AtlasElement> = HashMap()

            for ((elementName, element) in atlas) {
                data[elementName] = element.load(textures)
            }
            map[atlasName] = Atlas(data)
        }

        return map
    }

    fun ResourceLocation.atlas(): ResourceLocation {
        return this.extend(prefix = "gui/atlas/", suffix = ".json")
    }
}
