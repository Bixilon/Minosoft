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
import de.bixilon.minosoft.data.mappings.blocks.entites.BlockEntityType
import de.bixilon.minosoft.data.mappings.items.Item
import de.bixilon.minosoft.data.mappings.registry.RegistryItem
import de.bixilon.minosoft.data.mappings.registry.ResourceLocationDeserializer
import de.bixilon.minosoft.data.mappings.versions.VersionMapping
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.TintColorCalculator
import de.bixilon.minosoft.gui.rendering.chunk.models.renderable.BlockLikeRenderer

open class Block(final override val resourceLocation: ResourceLocation, mappings: VersionMapping, data: JsonObject) : RegistryItem {
    open val explosionResistance: Float = data["explosion_resistance"]?.asFloat ?: 0.0f
    open val tintColor: RGBColor? = data["tint_color"]?.asInt?.let { TintColorCalculator.getJsonColor(it) }
    open val randomOffsetType: RandomOffsetTypes? = data["offset_type"]?.asString?.let { RandomOffsetTypes[it] }
    open val tint: ResourceLocation? = data["tint"]?.asString?.let { ResourceLocation(it) }
    open val renderOverride: List<BlockLikeRenderer>? = null
    open var blockEntityType: BlockEntityType? = null
        protected set

    private val itemId: Int = data["item"]?.asInt ?: 0

    open lateinit var states: Set<BlockState>
        protected set
    open lateinit var defaultState: BlockState
        protected set
    open lateinit var item: Item
        protected set

    override fun postInit(versionMapping: VersionMapping) {
        item = versionMapping.itemRegistry[itemId]
        blockEntityType = versionMapping.blockEntityTypeRegistry.getByBlock(this)
    }

    override fun toString(): String {
        return resourceLocation.full
    }

    companion object : ResourceLocationDeserializer<Block> {
        override fun deserialize(mappings: VersionMapping?, resourceLocation: ResourceLocation, data: JsonObject): Block {
            check(mappings != null) { "VersionMapping is null!" }

            val block = when (data["class"].asString) {
                "FluidBlock" -> FluidBlock(resourceLocation, mappings, data)
                else -> Block(resourceLocation, mappings, data)
            }


            val states: MutableSet<BlockState> = mutableSetOf()
            for ((stateId, stateJson) in data["states"].asJsonObject.entrySet()) {
                check(stateJson is JsonObject) { "Not a state element!" }
                val state = BlockState.deserialize(block, mappings, stateJson, mappings.models)
                mappings.blockStateIdMap[stateId.toInt()] = state
                states.add(state)
            }

            block.states = states.toSet()
            block.defaultState = mappings.blockStateIdMap[data["default_state"].asInt]!!
            return block
        }
    }
}
