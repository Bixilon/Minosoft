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
import de.bixilon.minosoft.gui.rendering.models.builtin.BuiltinModels
import de.bixilon.minosoft.gui.rendering.models.unbaked.UnbakedBlockModel
import de.bixilon.minosoft.gui.rendering.models.unbaked.UnbakedItemModel
import de.bixilon.minosoft.gui.rendering.models.unbaked.UnbakedModel
import de.bixilon.minosoft.util.KUtil.fromJson
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.KUtil.unsafeCast
import de.bixilon.minosoft.util.Util
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.asCompound
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class ModelLoader(
    val jar: ZipInputStream,
) {
    private val unbakedBlockModels: MutableMap<ResourceLocation, UnbakedModel> = BuiltinModels.BUILTIN_MODELS.toMutableMap()
    private val blockStateJsons: MutableMap<ResourceLocation, Map<String, Any>> = mutableMapOf()
    private val modelJsons: MutableMap<ResourceLocation, Map<String, Any>> = mutableMapOf()

    private fun loadJsons() {
        // ToDo: Integrate in assets manager
        var entry: ZipEntry? = jar.nextEntry
        while (entry != null) {
            if (!entry.name.startsWith("assets/minecraft/models/") && !entry.name.startsWith("assets/minecraft/blockstates/")) {
                entry = jar.nextEntry
                continue
            }
            val name = entry.name.removePrefix("assets/minecraft/").removeSuffix(".json")
            val jsonString = Util.readReader(BufferedReader(InputStreamReader(jar)), false)

            val json = jsonString.fromJson().asCompound()
            if (name.startsWith("models/")) {
                modelJsons[name.removePrefix("models/").toResourceLocation()] = json
            } else {
                blockStateJsons[name.removePrefix("blockstates/").toResourceLocation()] = json
            }


            entry = jar.nextEntry
        }
    }

    fun load() {
        loadJsons()
        Log.log(LogMessageType.VERSION_LOADING, LogLevels.VERBOSE) { "Loaded ${blockStateJsons.size} block states and ${modelJsons.size} model jsons!" }


        fun loadBlockModel(name: ResourceLocation, json: Map<String, Any>? = null): UnbakedModel {
            unbakedBlockModels[name]?.let { return it.unsafeCast() }
            val data = json ?: modelJsons[name] ?: error("Can not find json: $name")

            val parent = data["parent"]?.toResourceLocation()?.let { loadBlockModel(it) }

            val model = UnbakedBlockModel(parent, data)

            unbakedBlockModels[name] = model
            return model
        }


        fun loadItemModel(name: ResourceLocation, json: Map<String, Any>? = null): UnbakedModel {
            unbakedBlockModels[name]?.let { return it.unsafeCast() }
            val data = json ?: modelJsons[name] ?: error("Can not find json: $name")

            val parent = data["parent"]?.toResourceLocation()?.let { loadItemModel(it) }

            val model = UnbakedItemModel(parent, data)

            unbakedBlockModels[name] = model
            return model
        }

        for ((name, json) in modelJsons) {
            if (name.path.startsWith("block/")) {
                loadBlockModel(name, json)
            } else if (name.path.startsWith("item/")) {
                loadItemModel(name, json)
            } else {
                TODO("Unknown block model type: $name")
            }
        }
        Log.log(LogMessageType.VERSION_LOADING, LogLevels.VERBOSE) { "Done loading models!" }
    }
}
