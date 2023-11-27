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

package de.bixilon.minosoft.gui.rendering.models.item

import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.json.JsonUtil.toJsonObject
import de.bixilon.minosoft.gui.rendering.models.block.BlockModel
import de.bixilon.minosoft.gui.rendering.models.raw.display.DisplayPositions
import de.bixilon.minosoft.gui.rendering.models.raw.display.ModelDisplay
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureManager
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class ItemModel(
    val display: Map<DisplayPositions, ModelDisplay>? = null,
    val textures: Map<String, Any>?,
) {

    fun load(textures: TextureManager): ItemModelPrototype? {
        if (this.textures == null) return null
        val particle = this.textures["particle"]?.let { textures.static.create(it.toResourceLocation().texture()) }

        val layers: MutableList<IndexedValue<Texture>> = mutableListOf()
        for ((key, texture) in this.textures) {
            if (!key.startsWith("layer")) continue
            val index = key.removePrefix("layer").toInt()
            layers += IndexedValue(index, textures.static.create(texture.toResourceLocation().texture()))
        }
        if (layers.isEmpty()) return null

        layers.sortBy { it.index }
        val array = layers.map { it.value }.toTypedArray()

        return ItemModelPrototype(array, particle)
    }

    companion object {

        fun deserialize(parent: ItemModel?, data: JsonObject): ItemModel {
            val display = data["display"]?.toJsonObject()?.let { BlockModel.display(it, parent?.display) } ?: parent?.display
            val textures = data["textures"]?.toJsonObject()?.let { BlockModel.textures(it, parent?.textures) } ?: parent?.textures

            // TODO: overrides, predicates

            return ItemModel(display, textures)
        }
    }

}
