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

package de.bixilon.minosoft.gui.rendering.models.loader

import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.minosoft.assets.util.InputStreamUtil.readJsonObject
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.data.registries.item.items.block.BlockItem
import de.bixilon.minosoft.data.registries.item.items.block.legacy.PixLyzerBlockItem
import de.bixilon.minosoft.gui.rendering.models.item.ItemModel
import de.bixilon.minosoft.gui.rendering.models.item.ItemModelPrototype
import de.bixilon.minosoft.gui.rendering.models.loader.ModelFixer.fixPrefix
import de.bixilon.minosoft.gui.rendering.models.loader.ModelLoader.Companion.model
import de.bixilon.minosoft.gui.rendering.models.loader.legacy.CustomModel
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class ItemLoader(private val loader: ModelLoader) {
    private val cache: MutableMap<ResourceLocation, ItemModel> = HashMap(loader.context.connection.registries.item.size)
    val assets = loader.context.connection.assetsManager
    val version = loader.context.connection.version

    fun loadItem(name: ResourceLocation): ItemModel? {
        val file = name.itemModel()
        cache[file]?.let { return it }
        val data = assets.getOrNull(file)?.readJsonObject()
        if (data == null) {
            Log.log(LogMessageType.LOADING, LogLevels.WARN) { "Can not find item model $name" }
            return null
        }

        val parent = data["parent"]?.toString()?.let { loadItem(it.toResourceLocation()) }

        val model = ItemModel.deserialize(parent, data)
        cache[file] = model
        return model
    }

    private fun loadItem(item: Item): ItemModel? {
        val file = (if (item is CustomModel) item.getModelName(version) else item.identifier) ?: return null

        return loadItem(file)
    }

    fun load(latch: AbstractLatch?) {
        for (item in loader.context.connection.registries.item) {
            if (item is BlockItem<*> || item is PixLyzerBlockItem) continue // block models are loaded in a different step
            if (item.model != null) continue // already has a model set
            val model = loadItem(item) ?: continue

            val prototype = model.load(loader.context.textures)
            item.model = prototype
        }
    }

    fun bake(latch: AbstractLatch?) {
        for (item in loader.context.connection.registries.item) {
            val prototype = item.model.nullCast<ItemModelPrototype>() ?: continue

            item.model = prototype.bake()
        }
    }

    fun cleanup() {
        this.cache.clear()
    }

    fun fixTexturePath(name: ResourceLocation): ResourceLocation {
        return ResourceLocation(name.namespace, name.path.fixPrefix(loader.packFormat, 4, "items/", "item/"))
    }

    private fun ResourceLocation.itemModel(): ResourceLocation {
        return this.prefix("item/").model()
    }
}
