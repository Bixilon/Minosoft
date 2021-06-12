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

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.inventory.ItemStack
import de.bixilon.minosoft.data.player.Hands
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.blocks.BlockUsages
import de.bixilon.minosoft.data.registries.blocks.RandomOffsetTypes
import de.bixilon.minosoft.data.registries.blocks.entites.BlockEntityType
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.types.portal.NetherPortalBlock
import de.bixilon.minosoft.data.registries.blocks.types.redstone.ComparatorBlock
import de.bixilon.minosoft.data.registries.blocks.types.redstone.RepeaterBlock
import de.bixilon.minosoft.data.registries.blocks.types.wall.LeverBlock
import de.bixilon.minosoft.data.registries.items.Item
import de.bixilon.minosoft.data.registries.registry.RegistryItem
import de.bixilon.minosoft.data.registries.registry.ResourceLocationDeserializer
import de.bixilon.minosoft.data.registries.versions.Registries
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.TintColorCalculator
import de.bixilon.minosoft.gui.rendering.chunk.models.renderable.BlockLikeRenderer
import de.bixilon.minosoft.gui.rendering.input.camera.RaycastHit
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import glm_.vec3.Vec3i
import kotlin.random.Random

open class Block(
    final override val resourceLocation: ResourceLocation,
    registries: Registries,
    data: JsonObject,
) : RegistryItem {
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
    open lateinit var properties: Map<BlockProperties, List<Any>>
    open val friction = data["friction"]?.asDouble ?: 0.6
    open val velocityMultiplier = data["velocity_multiplier"]?.asDouble ?: 1.0 // ToDo: They exist since ~1.15
    open val jumpVelocityMultiplier = data["jump_velocity_multiplier"]?.asDouble ?: 1.0

    override fun postInit(registries: Registries) {
        item = registries.itemRegistry[itemId]
        blockEntityType = registries.blockEntityTypeRegistry.getByBlock(this)
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

    companion object : ResourceLocationDeserializer<Block> {
        private val CONSTRUCTORS: Map<String, (resourceLocation: ResourceLocation, registries: Registries, data: JsonObject) -> Block> = mapOf(
            "FluidBlock" to { resourceLocation, registries, data -> FluidBlock(resourceLocation, registries, data) },
            "DoorBlock" to { resourceLocation, registries, data -> DoorBlock(resourceLocation, registries, data) },
            "LeverBlock" to { resourceLocation, registries, data -> LeverBlock(resourceLocation, registries, data) },
            "NoteBlock" to { resourceLocation, registries, data -> NoteBlock(resourceLocation, registries, data) },
            "RepeaterBlock" to { resourceLocation, registries, data -> RepeaterBlock(resourceLocation, registries, data) },
            "ComparatorBlock" to { resourceLocation, registries, data -> ComparatorBlock(resourceLocation, registries, data) },
            "CampfireBlock" to { resourceLocation, registries, data -> CampfireBlock(resourceLocation, registries, data) },
            "TorchBlock" to { resourceLocation, registries, data -> TorchBlock(resourceLocation, registries, data) },
            "SlimeBlock" to { resourceLocation, registries, data -> SlimeBlock(resourceLocation, registries, data) },
            "BedBlock" to { resourceLocation, registries, data -> BedBlock(resourceLocation, registries, data) },
            "BrewingStandBlock" to { resourceLocation, registries, data -> BrewingStandBlock(resourceLocation, registries, data) },
            "EnderChestBlock" to { resourceLocation, registries, data -> EnderChestBlock(resourceLocation, registries, data) },
            "NetherPortalBlock" to { resourceLocation, registries, data -> NetherPortalBlock(resourceLocation, registries, data) },
            "RedstoneTorchBlock" to { resourceLocation, registries, data -> RedstoneTorchBlock(resourceLocation, registries, data) },
        )

        override fun deserialize(registries: Registries?, resourceLocation: ResourceLocation, data: JsonObject): Block {
            check(registries != null) { "Registries is null!" }

            val block = CONSTRUCTORS[data["class"].asString]?.invoke(resourceLocation, registries, data) ?: Block(resourceLocation, registries, data)

            val properties: MutableMap<BlockProperties, MutableSet<Any>> = mutableMapOf()

            val states: MutableSet<BlockState> = mutableSetOf()
            for ((stateId, stateJson) in data["states"].asJsonObject.entrySet()) {
                check(stateJson is JsonObject) { "Not a state element!" }
                val state = BlockState.deserialize(block, registries, stateJson, registries.models)
                registries.blockStateIdMap[stateId.toInt()] = state
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
            block.defaultState = registries.blockStateIdMap[data["default_state"].asInt]!!
            block.properties = propertiesOut.toMap()
            return block
        }
    }
}
