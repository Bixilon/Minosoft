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

package de.bixilon.minosoft.data.registries.blocks.types.entity.storage

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.camera.target.targets.BlockTarget
import de.bixilon.minosoft.config.DebugOptions
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.entities.block.container.storage.ChestBlockEntity
import de.bixilon.minosoft.data.entities.block.container.storage.StorageBlockEntity
import de.bixilon.minosoft.data.entities.entities.player.Hands
import de.bixilon.minosoft.data.registries.blocks.types.entity.BlockWithEntity
import de.bixilon.minosoft.data.registries.blocks.types.properties.InteractBlockHandler
import de.bixilon.minosoft.input.interaction.InteractionResults
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

interface StorageBlock<T : StorageBlockEntity> : BlockWithEntity<T>, InteractBlockHandler {

    override fun interact(connection: PlayConnection, target: BlockTarget, hand: Hands, stack: ItemStack?): InteractionResults {
        if (!DebugOptions.FORCE_CHEST_ANIMATION) return super.interact(connection, target, hand, stack)

        val entity = target.entity.unsafeCast<ChestBlockEntity>()
        entity.setBlockActionData(0, if (entity.viewing > 0) 0 else 1)
        return InteractionResults.SUCCESS
    }
}
