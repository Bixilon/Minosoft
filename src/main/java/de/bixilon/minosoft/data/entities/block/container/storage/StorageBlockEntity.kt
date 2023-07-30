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

package de.bixilon.minosoft.data.entities.block.container.storage

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.data.entities.block.BlockActionEntity
import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.entities.block.container.ContainerBlockEntity
import de.bixilon.minosoft.gui.rendering.chunk.entities.BlockEntityRenderer
import de.bixilon.minosoft.gui.rendering.chunk.entities.renderer.storage.StorageBlockEntityRenderer
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

abstract class StorageBlockEntity(connection: PlayConnection) : ContainerBlockEntity(connection), BlockActionEntity {
    protected var blockEntityRenderer: StorageBlockEntityRenderer<*>? = null
    override var renderer: BlockEntityRenderer<out BlockEntity>?
        get() = blockEntityRenderer
        set(value) {
            blockEntityRenderer = value?.unsafeCast()
        }

    var playersLookingIntoStorage: Int = 0
        private set

    val closed: Boolean get() = playersLookingIntoStorage <= 0

    override fun setBlockActionData(data1: Byte, data2: Byte) {
        val closed = closed
        playersLookingIntoStorage = data2.toInt()

        if (this.closed == closed) {
            // state has not changed
            return
        }
        if (playersLookingIntoStorage <= 0) {
            blockEntityRenderer?.close()
        } else {
            blockEntityRenderer?.open()
        }
    }
}
