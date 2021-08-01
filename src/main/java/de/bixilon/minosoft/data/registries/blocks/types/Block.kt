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
package de.bixilon.minosoft.data.registries.blocks.types

import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.inventory.ItemStack
import de.bixilon.minosoft.data.player.Hands
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.VoxelShape
import de.bixilon.minosoft.data.registries.blocks.*
import de.bixilon.minosoft.data.registries.blocks.entites.BlockEntityType
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.items.Item
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.registries.registries.registry.RegistryItem
import de.bixilon.minosoft.data.registries.registries.registry.ResourceLocationDeserializer
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.TintColorCalculator
import de.bixilon.minosoft.gui.rendering.block.renderable.WorldEntryRenderer
import de.bixilon.minosoft.gui.rendering.input.camera.hit.RaycastHit
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.mapCast
import de.bixilon.minosoft.util.KUtil.nullCast
import de.bixilon.minosoft.util.KUtil.toDouble
import de.bixilon.minosoft.util.KUtil.toInt
import de.bixilon.minosoft.util.KUtil.unsafeCast
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.asCompound
import glm_.vec3.Vec3i
import kotlin.random.Random

open class Block(
    final override val resourceLocation: ResourceLocation,
    registries: Registries,
    data: Map<String, Any>,
) : RegistryItem() {
    open val explosionResistance: Float = data["explosion_resistance"]?.unsafeCast<Float>() ?: 0.0f
    open val tintColor: RGBColor? = data["tint_color"]?.toInt()?.let { TintColorCalculator.getJsonColor(it) }
    open val randomOffsetType: RandomOffsetTypes? = data["offset_type"].nullCast<String>()?.let { RandomOffsetTypes[it] }
    open val tint: ResourceLocation? = data["tint"].nullCast<String>()?.let { ResourceLocation(it) }
    open val renderOverride: List<WorldEntryRenderer>? = null
    open var blockEntityType: BlockEntityType? = null
        protected set

    open lateinit var states: Set<BlockState>
        protected set
    open lateinit var defaultState: BlockState
        protected set
    val item: Item? = null
    open lateinit var properties: Map<BlockProperties, List<Any>>
    open val friction = data["friction"]?.toDouble() ?: 0.6
    open val velocityMultiplier = data["velocity_multiplier"]?.toDouble() ?: 1.0 // ToDo: They exist since ~1.15
    open val jumpVelocityMultiplier = data["jump_velocity_multiplier"]?.toDouble() ?: 1.0

    init {
        this::item.inject(data["item"])
    }

    override fun postInit(registries: Registries) {
        blockEntityType = registries.blockEntityTypeRegistry[this]
    }

    override fun toString(): String {
        return resourceLocation.full
    }

    open fun getPlacementState(connection: PlayConnection, raycastHit: RaycastHit): BlockState? {
        return defaultState
    }

    open fun onBreak(connection: PlayConnection, blockPosition: Vec3i, blockState: BlockState, blockEntity: BlockEntity?) {

    }

    open fun onPlace(connection: PlayConnection, blockPosition: Vec3i, blockState: BlockState) {

    }

    open fun canPlaceAt(connection: PlayConnection, blockPosition: Vec3i, blockState: BlockState): Boolean {
        return true
    }

    open fun onUse(connection: PlayConnection, blockState: BlockState, blockPosition: Vec3i, raycastHit: RaycastHit, hands: Hands, itemStack: ItemStack?): BlockUsages {
        if (blockEntityType == null) {
            return BlockUsages.PASS
        }
        return BlockUsages.SUCCESS
    }

    fun withProperties(vararg properties: Pair<BlockProperties, Any>): BlockState {
        return this.defaultState.withProperties(*properties)
    }

    fun withProperties(properties: Map<BlockProperties, Any>): BlockState {
        return this.defaultState.withProperties(properties)
    }

    open fun randomTick(connection: PlayConnection, blockState: BlockState, blockPosition: Vec3i, random: Random) {}

    open fun onEntityLand(connection: PlayConnection, entity: Entity, blockPosition: Vec3i, blockState: BlockState) {}

    open fun onEntityCollision(connection: PlayConnection, entity: Entity, blockState: BlockState, blockPosition: Vec3i) {}

    open fun getOutlineShape(connection: PlayConnection, blockState: BlockState, blockPosition: Vec3i): VoxelShape {
        return blockState.outlineShape
    }

    companion object : ResourceLocationDeserializer<Block>, BlockFactory<Block> {
        override fun deserialize(registries: Registries?, resourceLocation: ResourceLocation, data: Map<String, Any>): Block {
            check(registries != null) { "Registries is null!" }

            val block = DefaultBlockFactories[data["class"].unsafeCast()]?.build(resourceLocation, registries, data) ?: Block(resourceLocation, registries, data)

            val properties: MutableMap<BlockProperties, MutableSet<Any>> = mutableMapOf()

            val states: MutableSet<BlockState> = mutableSetOf()
            for ((stateId, stateJson) in data["states"]?.mapCast()!!) {
                check(stateJson is Map<*, *>) { "Not a state element!" }
                val state = BlockState.deserialize(block, registries, stateJson.asCompound(), registries.models)
                registries.blockStateRegistry[stateId.toInt()] = state
                states.add(state)
                for ((property, value) in state.properties) {
                    properties.getOrPut(property) { mutableSetOf() } += value
                }
            }

            val propertiesOut: MutableMap<BlockProperties, List<Any>> = mutableMapOf()

            for ((property, values) in properties) {
                propertiesOut[property] = values.toList()
            }

            block.states = states.toSet()
            block.defaultState = registries.blockStateRegistry.forceGet(data["default_state"].unsafeCast())!!
            block.properties = propertiesOut.toMap()
            return block
        }

        override fun build(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>): Block {
            return Block(resourceLocation, registries, data)
        }
    }
}
