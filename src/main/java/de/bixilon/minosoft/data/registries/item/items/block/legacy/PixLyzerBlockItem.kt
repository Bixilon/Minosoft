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

package de.bixilon.minosoft.data.registries.item.items.block.legacy

import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.minosoft.camera.target.targets.BlockTarget
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.factory.clazz.MultiClassFactory
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.factory.PixLyzerItemFactory
import de.bixilon.minosoft.data.registries.item.items.block.PlaceableItem
import de.bixilon.minosoft.data.registries.item.items.pixlyzer.PixLyzerItem
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.registries.registries.registry.RegistryItem
import de.bixilon.minosoft.gui.rendering.models.item.ItemRender
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import kotlin.reflect.jvm.javaField

open class PixLyzerBlockItem(
    resourceLocation: ResourceLocation,
    registries: Registries,
    data: Map<String, Any>,
) : PixLyzerItem(resourceLocation, registries, data), PlaceableItem {
    val block: Block = unsafeNull()

    override var model: ItemRender?
        get() = super.model ?: block.model ?: block.states.default.model
        set(value) {
            super.model = value
        }

    init {
        BLOCK_FIELD.inject<RegistryItem>(data["block"])
    }

    override fun getPlacementState(connection: PlayConnection, target: BlockTarget, stack: ItemStack): BlockState {
        return block.states.default
    }

    companion object : PixLyzerItemFactory<PixLyzerBlockItem>, MultiClassFactory<PixLyzerBlockItem> {
        override val ALIASES = setOf("BlockItem", "AliasedBlockItem")
        private val BLOCK_FIELD = PixLyzerBlockItem::block.javaField!!

        override fun build(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>): PixLyzerBlockItem {
            return PixLyzerBlockItem(resourceLocation, registries, data)
        }
    }
}
