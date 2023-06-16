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
import de.bixilon.minosoft.camera.target.targets.BlockTarget
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.entities.entities.player.Hands
import de.bixilon.minosoft.data.language.LanguageUtil.translation
import de.bixilon.minosoft.data.language.translate.Translatable
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.settings.BlockSettings
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.properties.LightedBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.hardness.HardnessBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.physics.PushingBlock
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.registry.RegistryItem
import de.bixilon.minosoft.gui.rendering.tint.TintProvider
import de.bixilon.minosoft.input.interaction.InteractionResults
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import java.util.*
import kotlin.reflect.jvm.javaField

abstract class Block(
    override val identifier: ResourceLocation,
    settings: BlockSettings,
) : RegistryItem(), LightedBlock, HardnessBlock, Translatable, PushingBlock {
    val properties: Map<BlockProperties, Array<Any>> = unsafeNull()
    val states: Set<BlockState> = unsafeNull()
    val defaultState: BlockState = unsafeNull()

    override val translationKey: ResourceLocation = identifier.translation("block")

    @Deprecated("Interface")
    var tintProvider: TintProvider? = null

    val soundGroup = settings.soundGroup

    override fun toString(): String {
        return identifier.toString()
    }

    @Deprecated("Interface")
    open fun getPlacementState(connection: PlayConnection, target: BlockTarget): BlockState? = defaultState

    @Deprecated("Interface")
    open fun onUse(connection: PlayConnection, target: BlockTarget, hand: Hands, itemStack: ItemStack?): InteractionResults {
        return InteractionResults.IGNORED
    }

    fun withProperties(vararg properties: Pair<BlockProperties, Any>): BlockState {
        return this.defaultState.withProperties(*properties)
    }

    fun withProperties(properties: Map<BlockProperties, Any>): BlockState {
        return this.defaultState.withProperties(properties)
    }


    @Deprecated("Interface")
    open fun randomTick(connection: PlayConnection, blockState: BlockState, blockPosition: Vec3i, random: Random) = Unit

    @Deprecated("Interface")
    open fun canCull(blockState: BlockState, other: BlockState): Boolean = true


    fun updateStates(states: Set<BlockState>, default: BlockState, properties: Map<BlockProperties, Array<Any>>) {
        PROPERTIES.set(this, properties)
        STATES.set(this, states)
        DEFAULT_STATE.set(this, default)
    }

    private companion object {
        val PROPERTIES = Block::properties.javaField!!.apply { isAccessible = true }
        val STATES = Block::states.javaField!!.apply { isAccessible = true }
        val DEFAULT_STATE = Block::defaultState.javaField!!.apply { isAccessible = true }
    }
}
