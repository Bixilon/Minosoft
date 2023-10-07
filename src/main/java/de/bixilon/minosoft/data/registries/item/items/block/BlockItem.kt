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

package de.bixilon.minosoft.data.registries.item.items.block

import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.minosoft.camera.target.targets.BlockTarget
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.identified.Identified
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.data.registries.item.stack.StackableItem
import de.bixilon.minosoft.data.registries.registries.registry.RegistryItem
import de.bixilon.minosoft.gui.rendering.models.item.ItemRender
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import kotlin.reflect.jvm.javaField

abstract class BlockItem<T : Block>(identifier: ResourceLocation) : Item(identifier), StackableItem, PlaceableItem {
    val block: T = unsafeNull()
    protected abstract val _block: Identified
    override val translationKey: ResourceLocation get() = block.translationKey
    override var model: ItemRender?
        get() = super.model ?: block.model ?: block.states.default.model
        set(value) {
            super.model = value
        }

    init {
        BLOCK_FIELD.inject<RegistryItem>(_block)
    }

    override fun getPlacementState(connection: PlayConnection, target: BlockTarget, stack: ItemStack) = block.states.default


    private companion object {
        private val BLOCK_FIELD = BlockItem<*>::block.javaField!!
    }
}
