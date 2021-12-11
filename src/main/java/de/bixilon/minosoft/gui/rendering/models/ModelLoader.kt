/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.models

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.models.builtin.BuiltinModels
import de.bixilon.minosoft.gui.rendering.models.unbaked.GenericUnbakedModel
import de.bixilon.minosoft.gui.rendering.models.unbaked.UnbakedBlockModel
import de.bixilon.minosoft.gui.rendering.models.unbaked.UnbakedItemModel
import de.bixilon.minosoft.gui.rendering.models.unbaked.block.RootModel
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.KUtil.unsafeCast
import de.bixilon.minosoft.util.json.Jackson
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class ModelLoader(
    val data: Map<String, ByteArray>,
    val renderWindow: RenderWindow,
) {
    private val unbakedBlockModels: MutableMap<ResourceLocation, GenericUnbakedModel> = BuiltinModels.BUILTIN_MODELS.toMutableMap()
    private val unbakedBlockStateModels: MutableMap<ResourceLocation, RootModel> = mutableMapOf()
    private val blockStateJsons: MutableMap<ResourceLocation, Map<String, Any>> = mutableMapOf()
    private val modelJsons: MutableMap<ResourceLocation, Map<String, Any>> = mutableMapOf()

    private val registry: Registries = renderWindow.connection.registries

    private fun loadJsons() {
        // ToDo: Integrate in assets manager
        for ((entryName, data) in data) {
            if (!entryName.startsWith("assets/minecraft/models/") && !entryName.startsWith("assets/minecraft/blockstates/")) {
                continue
            }
            val name = entryName.removePrefix("assets/minecraft/").removeSuffix(".json")
            val json: Map<String, Any> = Jackson.MAPPER.readValue(String(data), Jackson.JSON_MAP_TYPE)

            if (name.startsWith("models/")) {
                modelJsons[name.removePrefix("models/").toResourceLocation()] = json
            } else {
                blockStateJsons[name.removePrefix("blockstates/").toResourceLocation()] = json
            }
        }
    }

    private fun cleanup() {
        unbakedBlockModels.clear()
        unbakedBlockStateModels.clear()
        modelJsons.clear()
        blockStateJsons.clear()
    }

    private fun loadBlockModel(name: ResourceLocation, data: Map<String, Any>? = null): GenericUnbakedModel {
        unbakedBlockModels[name]?.let { return it.unsafeCast() }
        val data = data ?: modelJsons[name] ?: error("Can not find json: $name")

        val parent = data["parent"]?.toResourceLocation()?.let { loadBlockModel(it) }

        val model = UnbakedBlockModel(parent, data)

        unbakedBlockModels[name] = model
        return model
    }

    private fun loadItemModel(name: ResourceLocation, data: Map<String, Any>? = null): GenericUnbakedModel {
        unbakedBlockModels[name]?.let { return it.unsafeCast() }
        val data = data ?: modelJsons[name] ?: error("Can not find json: $name")

        val parent = data["parent"]?.toResourceLocation()?.let { loadItemModel(it) }

        val model = UnbakedItemModel(parent, data)

        unbakedBlockModels[name] = model
        return model
    }

    private fun loadModels() {
        for ((name, data) in modelJsons) {
            if (name.path.startsWith("block/")) {
                loadBlockModel(name, data)
            } else if (name.path.startsWith("item/")) {
                loadItemModel(name, data)
            } else {
                TODO("Unknown block model type: $name")
            }
        }
    }

    private fun loadBlockStates() {
        for ((name, data) in blockStateJsons) {
            val model = RootModel(unbakedBlockModels, data) ?: continue
            unbakedBlockStateModels[name] = model
        }
    }

    private fun bakeModels() {
        for ((name, model) in unbakedBlockStateModels) {
            val block = registry.blockRegistry[name] ?: continue

            for (state in block.states) {
                state.blockModel = model.getModelForState(state).bake(renderWindow).unsafeCast()
            }
        }
    }

    fun load() {
        loadJsons()
        Log.log(LogMessageType.VERSION_LOADING, LogLevels.VERBOSE) { "Loaded ${blockStateJsons.size} block states and ${modelJsons.size} model jsons!" }

        loadModels()
        Log.log(LogMessageType.VERSION_LOADING, LogLevels.VERBOSE) { "Done loading unbaked models!" }

        loadBlockStates()
        Log.log(LogMessageType.VERSION_LOADING, LogLevels.VERBOSE) { "Done loading block states!" }

        bakeModels()
        Log.log(LogMessageType.VERSION_LOADING, LogLevels.VERBOSE) { "Done baking models!" }

        cleanup()
    }
}
