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
package de.bixilon.minosoft.data.registries.blocks.types.pixlyzer

import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.cast.CollectionCast.asAnyMap
import de.bixilon.kutil.json.JsonUtil.asJsonObject
import de.bixilon.kutil.primitive.DoubleUtil.toDouble
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.registries.blocks.RandomOffsetTypes
import de.bixilon.minosoft.data.registries.blocks.factory.PixLyzerBlockFactories
import de.bixilon.minosoft.data.registries.blocks.factory.PixLyzerBlockFactory
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.settings.BlockSettings
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.factory.clazz.MultiClassFactory
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.registries.registries.registry.codec.ResourceLocationCodec
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.util.*

open class PixLyzerBlock(
    identifier: ResourceLocation,
    registries: Registries,
    data: Map<String, Any>,
) : Block(identifier, BlockSettings(soundGroup = data["sound_group"]?.toInt()?.let { registries.soundGroup[it] }, item = data["item"]?.toInt())) {

    @Deprecated("Interface")
    open val randomOffsetType: RandomOffsetTypes? = data["offset_type"].nullCast<String>()?.let { RandomOffsetTypes[it] }

    @Deprecated("Interface")
    open val friction = data["friction"]?.toDouble() ?: 0.6

    @Deprecated("Interface")
    open val velocityMultiplier = data["velocity_multiplier"]?.toDouble() ?: 1.0 // ToDo: They exist since ~1.15

    @Deprecated("Interface")
    open val jumpVelocityMultiplier = data["jump_velocity_multiplier"]?.toDouble() ?: 1.0

    override fun toString(): String {
        return identifier.toString()
    }

    companion object : ResourceLocationCodec<Block>, PixLyzerBlockFactory<Block>, MultiClassFactory<Block> {
        override val ALIASES: Set<String> = setOf("Block")

        override fun deserialize(registries: Registries?, resourceLocation: ResourceLocation, data: Map<String, Any>): PixLyzerBlock {
            check(registries != null) { "Registries is null!" }

            val className = data["class"].toString()
            var factory = PixLyzerBlockFactories[className]
            if (factory == null) {
                Log.log(LogMessageType.VERSION_LOADING, LogLevels.VERBOSE) { "Block for class $className not found, defaulting..." }
                factory = PixLyzerBlock
            }
            val block = factory.build(resourceLocation, registries, data)

            val properties: MutableMap<BlockProperties, MutableSet<Any>> = mutableMapOf()

            val states: MutableSet<BlockState> = mutableSetOf()
            for ((stateId, stateJson) in data["states"].asAnyMap()) {
                check(stateJson is Map<*, *>) { "Not a state element!" }
                val state = BlockState.deserialize(block, registries, stateJson.asJsonObject())
                registries.blockState[stateId.toInt()] = state
                states.add(state)
                for ((property, value) in state.properties) {
                    properties.getOrPut(property) { mutableSetOf() } += value
                }
            }

            val propertiesOut: MutableMap<BlockProperties, List<Any>> = mutableMapOf()

            for ((property, values) in properties) {
                propertiesOut[property] = values.toList()
            }

            block.states = states
            block.defaultState = registries.blockState.forceGet(data["default_state"].unsafeCast())!!
            block.properties = propertiesOut

            return block
        }

        override fun build(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>): PixLyzerBlock {
            return PixLyzerBlock(resourceLocation, registries, data)
        }
    }
}
