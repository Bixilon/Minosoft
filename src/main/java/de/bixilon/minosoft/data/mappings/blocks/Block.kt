/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.mappings.blocks

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.items.Item
import de.bixilon.minosoft.data.mappings.registry.RegistryItem
import de.bixilon.minosoft.data.mappings.registry.ResourceLocationDeserializer
import de.bixilon.minosoft.data.mappings.versions.VersionMapping
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.TintColorCalculator
import de.bixilon.minosoft.gui.rendering.chunk.models.loading.BlockCondition

data class Block(
    override val resourceLocation: ResourceLocation,
    val explosionResistance: Float = 0.0f,
    val hasCollision: Boolean = false,
    val hasDynamicShape: Boolean = false,
    val tintColor: RGBColor? = null,
    private val itemId: Int = 0,
    val tint: ResourceLocation? = null,
) : RegistryItem {
    lateinit var defaultState: BlockState
        private set
    lateinit var item: Item
        private set
    val states: MutableSet<BlockState> = mutableSetOf()
    var multipartMapping: MutableMap<BlockCondition, MutableList<JsonObject>>? = null

    override fun postInit(versionMapping: VersionMapping) {
        item = versionMapping.itemRegistry.get(itemId)
    }

    override fun toString(): String {
        return resourceLocation.full
    }

    companion object : ResourceLocationDeserializer<Block> {
        override fun deserialize(mappings: VersionMapping?, resourceLocation: ResourceLocation, data: JsonObject): Block {
            check(mappings != null) { "VersionMapping is null!" }
            val block = Block(
                resourceLocation = resourceLocation, explosionResistance = data["explosion_resistance"]?.asFloat ?: 0.0f,
                hasCollision = data["has_collision"]?.asBoolean ?: false,
                hasDynamicShape = data["has_dynamic_shape"]?.asBoolean ?: false,
                tintColor = data["tint_color"]?.asInt?.let { TintColorCalculator.getJsonColor(it) },
                itemId = data["item"]?.asInt ?: 0,
                tint = data["tint"]?.asString?.let { ResourceLocation(it) },
            )

            // block states

            data["render"]?.asJsonObject?.get("multipart")?.asJsonArray?.let { multipart ->
                block.multipartMapping = mutableMapOf()
                for (item in multipart) {
                    val part = item.asJsonObject
                    val propertiesJson = part["properties"]
                    val condition = propertiesJson?.let {
                        BlockCondition(propertiesJson)
                    } ?: BlockCondition.TRUE_CONDITION
                    val apply = part["apply"]
                    when {
                        apply.isJsonObject -> {
                            addMultiPart(block, condition, apply.asJsonObject!!)
                        }
                        apply.isJsonArray -> {
                            for (model in apply.asJsonArray) {
                                addMultiPart(block, condition, model.asJsonObject!!)
                            }
                        }
                    }
                }
            }

            for ((stateId, stateJson) in data["states"].asJsonObject.entrySet()) {
                check(stateJson is JsonObject) { "Not a state element!" }
                val state = BlockState.deserialize(block, mappings, stateJson, mappings.models)
                mappings.blockStateIdMap[stateId.toInt()] = state
            }

            block.defaultState = mappings.blockStateIdMap[data["default_state"].asInt]!!
            return block
        }

        private fun addMultiPart(block: Block, condition: BlockCondition, apply: JsonObject) {
            block.multipartMapping!![condition]?.add(apply)
                ?: run { block.multipartMapping!![condition] = mutableListOf(apply) }
        }
    }
}
