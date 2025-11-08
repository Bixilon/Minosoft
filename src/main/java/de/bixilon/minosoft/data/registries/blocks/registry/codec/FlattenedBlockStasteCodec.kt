/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.registries.blocks.registry.codec

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.cast.CollectionCast.asAnyMap
import de.bixilon.kutil.enums.inline.enums.IntInlineEnumSet
import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperty
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.state.BlockStateFlags
import de.bixilon.minosoft.data.registries.blocks.state.builder.BlockStateBuilder
import de.bixilon.minosoft.data.registries.blocks.state.manager.BlockStateManager
import de.bixilon.minosoft.data.registries.blocks.state.manager.PropertyStateManager
import de.bixilon.minosoft.data.registries.blocks.state.manager.SingleStateManager
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.protocol.versions.Version
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet

object FlattenedBlockStasteCodec {

    fun deserialize(block: Block, flags: IntInlineEnumSet<BlockStateFlags>, data: JsonObject, version: Version, registries: Registries): BlockStateManager {
        val properties: MutableMap<BlockProperty<*>, MutableSet<Any>> = mutableMapOf()

        val states: MutableSet<BlockState> = ObjectOpenHashSet()

        for ((stateId, stateJson) in data["states"].asAnyMap()) {
            val settings = BlockStateBuilder.of(block, flags, registries, stateJson.unsafeCast())
            val state = block.buildState(version, settings)

            states += state

            registries.blockState[stateId.toInt()] = state

            for ((property, value) in state.properties) {
                properties.getOrPut(property) { ObjectOpenHashSet() } += value
            }
        }

        val default = registries.blockState.forceGet(data["default_state"].toInt())!!

        if (states.size == 1) {
            return SingleStateManager(default)
        }

        return PropertyStateManager(properties.mapValues { it.value.toTypedArray() }, states, default)
    }
}
