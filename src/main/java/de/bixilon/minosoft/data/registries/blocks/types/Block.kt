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
import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.player.Hands
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.settings.BlockSettings
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.data.registries.registries.registry.RegistryItem
import de.bixilon.minosoft.data.registries.shapes.VoxelShape
import de.bixilon.minosoft.gui.rendering.camera.target.targets.BlockTarget
import de.bixilon.minosoft.gui.rendering.input.interaction.InteractionResults
import de.bixilon.minosoft.gui.rendering.tint.TintProvider
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import java.util.*

abstract class Block(
    override val identifier: ResourceLocation,
    settings: BlockSettings,
) : RegistryItem() {
    open lateinit var states: Set<BlockState>
        protected set
    open lateinit var defaultState: BlockState
        protected set
    val item: Item = unsafeNull()
    open lateinit var properties: Map<BlockProperties, List<Any>>
    var tintProvider: TintProvider? = null

    val soundGroup = settings.soundGroup

    open val hardness: Float get() = 2.0f

    init {
        this::item.inject(settings.item)
    }

    override fun toString(): String {
        return identifier.toString()
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
}
