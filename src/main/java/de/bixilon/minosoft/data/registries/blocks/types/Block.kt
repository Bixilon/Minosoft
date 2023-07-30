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

import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.minosoft.data.language.LanguageUtil.translation
import de.bixilon.minosoft.data.language.translate.Translatable
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.settings.BlockSettings
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.state.manager.BlockStateManager
import de.bixilon.minosoft.data.registries.blocks.state.manager.PropertyStateManager
import de.bixilon.minosoft.data.registries.blocks.state.manager.SimpleStateManager
import de.bixilon.minosoft.data.registries.blocks.types.properties.LightedBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.hardness.HardnessBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.physics.PushingBlock
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.registry.RegistryItem
import de.bixilon.minosoft.gui.rendering.models.block.state.render.BlockRender
import de.bixilon.minosoft.gui.rendering.tint.TintProvider
import kotlin.reflect.jvm.javaField

abstract class Block(
    override val identifier: ResourceLocation,
    settings: BlockSettings,
) : RegistryItem(), LightedBlock, HardnessBlock, Translatable, PushingBlock {
    val states: BlockStateManager = unsafeNull()

    var model: BlockRender? = null

    override val translationKey: ResourceLocation = identifier.translation("block")

    @Deprecated("Interface")
    var tintProvider: TintProvider? = null

    val soundGroup = settings.soundGroup

    override fun toString(): String {
        return identifier.toString()
    }


    fun updateStates(states: Set<BlockState>, default: BlockState, properties: Map<BlockProperties, Array<Any>>) {
        val manager = when {
            states.size == 1 -> SimpleStateManager(default)
            else -> PropertyStateManager(properties, states, default)
        }
        STATES.set(this, manager)
    }

    private companion object {
        val STATES = Block::states.javaField!!.apply { isAccessible = true }
    }
}
