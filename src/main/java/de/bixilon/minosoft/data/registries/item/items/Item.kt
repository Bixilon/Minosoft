/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.registries.item.items

import de.bixilon.minosoft.data.Rarities
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.entities.entities.player.Hands
import de.bixilon.minosoft.data.language.LanguageUtil.translation
import de.bixilon.minosoft.data.language.translate.Translatable
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.registries.registry.RegistryItem
import de.bixilon.minosoft.gui.rendering.camera.target.targets.BlockTarget
import de.bixilon.minosoft.gui.rendering.camera.target.targets.EntityTarget
import de.bixilon.minosoft.gui.rendering.input.interaction.InteractionResults
import de.bixilon.minosoft.gui.rendering.models.baked.item.BakedItemModel
import de.bixilon.minosoft.gui.rendering.tint.TintProvider
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

abstract class Item(
    override val resourceLocation: ResourceLocation,
) : RegistryItem(), Translatable {
    @Deprecated("interface")
    open val rarity: Rarities get() = Rarities.COMMON

    @Deprecated("interface")
    open val maxStackSize: Int get() = 64

    @Deprecated("interface")
    open val maxDurability: Int get() = -1

    override val translationKey: ResourceLocation = resourceLocation.translation("item")

    open var model: BakedItemModel? = null
    var tintProvider: TintProvider? = null

    override fun toString(): String {
        return resourceLocation.toString()
    }

    open fun getMiningSpeedMultiplier(connection: PlayConnection, blockState: BlockState, stack: ItemStack): Float {
        return 1.0f
    }

    open fun interactBlock(connection: PlayConnection, target: BlockTarget, hand: Hands, stack: ItemStack): InteractionResults {
        return InteractionResults.PASS
    }

    open fun interactEntity(connection: PlayConnection, target: EntityTarget, hand: Hands, stack: ItemStack): InteractionResults {
        return InteractionResults.PASS
    }

    open fun interactEntityAt(connection: PlayConnection, target: EntityTarget, hand: Hands, stack: ItemStack): InteractionResults {
        return InteractionResults.PASS
    }

    open fun interactItem(connection: PlayConnection, hand: Hands, stack: ItemStack): InteractionResults {
        return InteractionResults.PASS
    }
}
