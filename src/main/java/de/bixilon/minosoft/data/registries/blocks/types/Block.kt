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
package de.bixilon.minosoft.data.registries.blocks.types

import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.kutil.cast.CollectionCast.asAnyMap
import de.bixilon.kutil.json.JsonUtil.asJsonObject
import de.bixilon.kutil.primitive.DoubleUtil.toDouble
import de.bixilon.kutil.primitive.FloatUtil.toFloat
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.player.Hands
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.blocks.BlockFactories
import de.bixilon.minosoft.data.registries.blocks.BlockFactory
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.blocks.RandomOffsetTypes
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.registries.registries.registry.RegistryItem
import de.bixilon.minosoft.data.registries.registries.registry.codec.ResourceLocationCodec
import de.bixilon.minosoft.data.registries.shapes.VoxelShape
import de.bixilon.minosoft.gui.rendering.camera.target.targets.BlockTarget
import de.bixilon.minosoft.gui.rendering.input.interaction.InteractionResults
import de.bixilon.minosoft.gui.rendering.tint.TintProvider
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.util.*

open class Block(
    final override val identifier: ResourceLocation,
    registries: Registries,
    data: Map<String, Any>,
) : RegistryItem() {
    open val explosionResistance: Float = data["explosion_resistance"]?.toFloat() ?: 0.0f
    open val randomOffsetType: RandomOffsetTypes? = data["offset_type"].nullCast<String>()?.let { RandomOffsetTypes[it] }
    open val tint: ResourceLocation? = data["tint"].nullCast<String>()?.let { ResourceLocation.of(it) }

    open lateinit var states: Set<BlockState>
        protected set
    open lateinit var defaultState: BlockState
        protected set
    val item: Item = unsafeNull()
    open lateinit var properties: Map<BlockProperties, List<Any>>
    open val friction = data["friction"]?.toDouble() ?: 0.6
    open val velocityMultiplier = data["velocity_multiplier"]?.toDouble() ?: 1.0 // ToDo: They exist since ~1.15
    open val jumpVelocityMultiplier = data["jump_velocity_multiplier"]?.toDouble() ?: 1.0
    var tintProvider: TintProvider? = null

    var soundGroup = data["sound_group"]?.toInt()?.let { registries.soundGroup[it] }

    init {
        this::item.inject(data["item"])
    }

    override fun toString(): String {
        return identifier.full
    }

    open fun getPlacementState(connection: PlayConnection, target: BlockTarget): BlockState? = defaultState

    open fun onBreak(connection: PlayConnection, blockPosition: Vec3i, blockState: BlockState, blockEntity: BlockEntity?) = Unit

    open fun onPlace(connection: PlayConnection, blockPosition: Vec3i, blockState: BlockState) = Unit

    open fun canPlaceAt(connection: PlayConnection, blockPosition: Vec3i, blockState: BlockState): Boolean = true

    open fun onUse(connection: PlayConnection, target: BlockTarget, hand: Hands, itemStack: ItemStack?): InteractionResults {
        return InteractionResults.PASS
    }

    fun withProperties(vararg properties: Pair<BlockProperties, Any>): BlockState {
        return this.defaultState.withProperties(*properties)
    }

    fun withProperties(properties: Map<BlockProperties, Any>): BlockState {
        return this.defaultState.withProperties(properties)
    }

    open fun randomTick(connection: PlayConnection, blockState: BlockState, blockPosition: Vec3i, random: Random) = Unit

    open fun onEntityLand(connection: PlayConnection, entity: Entity, blockPosition: Vec3i, blockState: BlockState) = Unit

    open fun onEntityCollision(connection: PlayConnection, entity: Entity, blockState: BlockState, blockPosition: Vec3i) = Unit

    open fun getOutlineShape(connection: PlayConnection, blockState: BlockState, blockPosition: Vec3i): VoxelShape {
        return blockState.outlineShape
    }

    open fun canCull(blockState: BlockState, other: BlockState): Boolean = true

    companion object : ResourceLocationCodec<Block>, BlockFactory<Block> {
        override fun deserialize(registries: Registries?, resourceLocation: ResourceLocation, data: Map<String, Any>): Block {
            check(registries != null) { "Registries is null!" }

            val className = data["class"].toString()
            var factory = BlockFactories[className]
            if (factory == null) {
                Log.log(LogMessageType.VERSION_LOADING, LogLevels.VERBOSE) { "Block for class $className not found, defaulting..." }
                factory = Block
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

        override fun build(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>): Block {
            return Block(resourceLocation, registries, data)
        }
    }
}
