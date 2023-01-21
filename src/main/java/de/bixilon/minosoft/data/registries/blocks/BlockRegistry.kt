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

package de.bixilon.minosoft.data.registries.blocks

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.cast.CollectionCast.asAnyMap
import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.registries.blocks.factory.BlockFactories
import de.bixilon.minosoft.data.registries.blocks.factory.BlockFactory
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.settings.BlockSettings
import de.bixilon.minosoft.data.registries.blocks.state.AdvancedBlockState
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.state.PropertyBlockState
import de.bixilon.minosoft.data.registries.blocks.state.builder.BlockStateBuilder
import de.bixilon.minosoft.data.registries.blocks.state.builder.BlockStateSettings
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.PixLyzerBlock
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.registries.registries.registry.MetaTypes
import de.bixilon.minosoft.data.registries.registries.registry.Registry

class BlockRegistry(
    parent: Registry<Block>? = null,
    flattened: Boolean = false,
) : Registry<Block>(parent = parent, codec = PixLyzerBlock, flattened = flattened, metaType = MetaTypes.BLOCK) {


    private fun updateStates(block: Block, data: JsonObject, registries: Registries) {
        val properties: MutableMap<BlockProperties, MutableSet<Any>> = mutableMapOf()

        val states: MutableSet<BlockState> = mutableSetOf()
        for ((stateId, stateJson) in data["states"].asAnyMap()) {
            val settings = BlockStateSettings.of(registries, stateJson.unsafeCast())
            val state = if (block is BlockStateBuilder) block.buildState(settings) else AdvancedBlockState(block, settings)

            states += state
            registries.blockState[stateId.toInt()] = state

            if (state !is PropertyBlockState) continue

            for ((property, value) in state.properties) {
                properties.getOrPut(property) { mutableSetOf() } += value
            }
        }

        val default = registries.blockState.forceGet(data["default_state"].toInt())!!

        block.updateStates(states, default, properties.mapValues { it.value.toTypedArray() })
    }

    override fun deserialize(resourceLocation: ResourceLocation, data: JsonObject, registries: Registries?): Block? {
        val factory = BlockFactories[resourceLocation]
        if (registries == null) throw NullPointerException("registries?")

        val block = factory?.build(registries, BlockSettings.of(registries, data)) ?: this.codec!!.deserialize(registries, resourceLocation, data) ?: return null

        updateStates(block, data, registries)

        return block
    }

    operator fun <T : Block> get(factory: BlockFactory<T>): T? {
        val item = this[factory.identifier] ?: return null
        return item.unsafeCast()
    }
}
