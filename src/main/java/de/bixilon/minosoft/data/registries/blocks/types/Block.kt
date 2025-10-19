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
package de.bixilon.minosoft.data.registries.blocks.types

import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.kutil.reflection.ReflectionUtil.field
import de.bixilon.minosoft.data.language.LanguageUtil.translation
import de.bixilon.minosoft.data.language.translate.Translatable
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperty
import de.bixilon.minosoft.data.registries.blocks.properties.list.BlockPropertyList
import de.bixilon.minosoft.data.registries.blocks.properties.list.MapPropertyList
import de.bixilon.minosoft.data.registries.blocks.settings.BlockSettings
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.state.manager.BlockStateManager
import de.bixilon.minosoft.data.registries.blocks.state.manager.PropertyStateManager
import de.bixilon.minosoft.data.registries.blocks.state.manager.SimpleStateManager
import de.bixilon.minosoft.data.registries.blocks.types.properties.LightedBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.StatedBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.hardness.HardnessBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.physics.PushingBlock
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.registry.RegistryItem
import de.bixilon.minosoft.gui.rendering.models.block.state.render.BlockRender

abstract class Block(
    override val identifier: ResourceLocation,
    settings: BlockSettings,
) : RegistryItem(), LightedBlock, HardnessBlock, Translatable, PushingBlock, StatedBlock {
    override val properties: BlockPropertyList = MapPropertyList().apply { register(settings.version, this) }.shrink()
    override val states: BlockStateManager = unsafeNull()

    var model: BlockRender? = null

    override val translationKey: ResourceLocation = settings.translationKey ?: identifier.translation("block")

    val soundGroup = settings.soundGroup

    override fun toString(): String {
        return identifier.toString()
    }

    override fun updateStates(states: Set<BlockState>, default: BlockState, properties: Map<BlockProperty<*>, Array<Any>>) {
        val manager = when {
            states.size == 1 -> SimpleStateManager(default)
            else -> PropertyStateManager(properties, states, default)
        }
        STATES[this] = manager
    }

    private companion object {
        val STATES = Block::states.field
    }
}
