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

package de.bixilon.minosoft.data.registries.item.items

import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.entities.entities.player.Hands
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.factory.PixLyzerItemFactory
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.gui.rendering.input.interaction.InteractionResults
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

open class ShieldItem(
    resourceLocation: ResourceLocation,
    registries: Registries,
    data: Map<String, Any>,
) : PixLyzerItem(resourceLocation, registries, data), UsableItem {
    override val maxUseTime: Int = Int.MAX_VALUE

    override fun interactItem(connection: PlayConnection, hand: Hands, stack: ItemStack): InteractionResults {
        connection.player.useItem(hand)
        return InteractionResults.CONSUME
    }

    companion object : PixLyzerItemFactory<ShieldItem> {

        override fun build(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>): ShieldItem {
            return ShieldItem(resourceLocation, registries, data)
        }
    }
}
