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

package de.bixilon.minosoft.data.registries.item.items.tool.axe

import de.bixilon.kutil.cast.CollectionCast.toAnyMap
import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.json.JsonUtil.toJsonList
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.entities.entities.player.Hands
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.items.tool.InteractingToolItem
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.gui.rendering.camera.target.targets.BlockTarget
import de.bixilon.minosoft.gui.rendering.input.interaction.InteractionResults
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

abstract class AxeItem(identifier: ResourceLocation, registries: Registries, data: JsonObject) : InteractingToolItem(identifier) {
    override val tag: ResourceLocation get() = TAG
    override val mineable: Set<Block>? = data["diggable_blocks"]?.toJsonList()?.blocks(registries)

    @Deprecated("StrippableBLock")
    protected val strippable = data["strippables_blocks"]?.toAnyMap()?.blocks(registries)


    override fun interactBlock(connection: PlayConnection, target: BlockTarget, hand: Hands, stack: ItemStack): InteractionResults {
        if (!connection.profiles.controls.interaction.stripping) {
            return InteractionResults.CONSUME
        }

        return super.interact(connection, target.blockPosition, strippable?.get(target.blockState.block)?.withProperties(target.blockState.properties))
    }

    companion object {
        private val TAG = minecraft("mineable/axe")
    }
}
