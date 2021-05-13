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
package de.bixilon.minosoft.data.mappings.blocks

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.items.Item
import de.bixilon.minosoft.data.mappings.registry.RegistryItem
import de.bixilon.minosoft.data.mappings.registry.ResourceLocationDeserializer
import de.bixilon.minosoft.data.mappings.versions.VersionMapping
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.TintColorCalculator
import de.bixilon.minosoft.gui.rendering.chunk.models.renderable.BlockLikeRenderer

open class Block(
    override val resourceLocation: ResourceLocation,
    val explosionResistance: Float = 0.0f,
    val hasDynamicShape: Boolean = false,
    val tintColor: RGBColor? = null,
    val randomOffsetType: RandomOffsetTypes? = null,
    private val itemId: Int = 0,
    val tint: ResourceLocation? = null,
    val renderOverride: MutableList<BlockLikeRenderer>? = null,
) : RegistryItem {
    lateinit var defaultState: BlockState
        private set
    lateinit var item: Item
        private set
    val states: MutableSet<BlockState> = mutableSetOf()

    override fun postInit(versionMapping: VersionMapping) {
        item = versionMapping.itemRegistry.get(itemId)
    }

    override fun toString(): String {
        return resourceLocation.full
    }

    companion object : ResourceLocationDeserializer<Block> {
        override fun deserialize(mappings: VersionMapping?, resourceLocation: ResourceLocation, data: JsonObject): Block {
            check(mappings != null) { "VersionMapping is null!" }

            var renderOverride: MutableList<BlockLikeRenderer>? = null


            val explosionResistance = data["explosion_resistance"]?.asFloat ?: 0.0f
            val hasDynamicShape = data["has_dynamic_shape"]?.asBoolean ?: false
            val tintColor = data["tint_color"]?.asInt?.let { TintColorCalculator.getJsonColor(it) }
            val randomOffsetType = data["offset_type"]?.asString?.let { RandomOffsetTypes[it] }
            val itemId = data["item"]?.asInt ?: 0
            val tint = data["tint"]?.asString?.let { ResourceLocation(it) }

            val block = when (data["class"].asString) {
                "FluidBlock" -> {
                    val stillFluid = mappings.fluidRegistry.get(data["still_fluid"].asInt)
                    val flowingFluid = mappings.fluidRegistry.get(data["flow_fluid"].asInt)
                    renderOverride = mutableListOf()


                    FluidBlock(
                        resourceLocation = resourceLocation,
                        explosionResistance = explosionResistance,
                        hasDynamicShape = hasDynamicShape,
                        tintColor = tintColor,
                        randomOffsetType = randomOffsetType,
                        itemId = itemId,
                        tint = tint,
                        renderOverride = renderOverride,
                        stillFluid = stillFluid,
                        flowingFluid = flowingFluid,
                    )
                }
                else -> Block(
                    resourceLocation = resourceLocation,
                    explosionResistance = explosionResistance,
                    hasDynamicShape = hasDynamicShape,
                    tintColor = tintColor,
                    randomOffsetType = randomOffsetType,
                    itemId = itemId,
                    tint = tint,
                    renderOverride = renderOverride,
                )
            }


            if (block.resourceLocation.full == "minecraft:oak_stairs") {
                var a = 1
            }
            for ((stateId, stateJson) in data["states"].asJsonObject.entrySet()) {
                check(stateJson is JsonObject) { "Not a state element!" }
                val state = BlockState.deserialize(block, mappings, stateJson, mappings.models)
                mappings.blockStateIdMap[stateId.toInt()] = state
                block.states.add(state)
            }

            block.defaultState = mappings.blockStateIdMap[data["default_state"].asInt]!!
            return block
        }
    }
}
