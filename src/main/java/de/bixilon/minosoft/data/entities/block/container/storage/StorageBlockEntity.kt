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

package de.bixilon.minosoft.data.entities.block.container.storage

import de.bixilon.minosoft.data.entities.block.BlockActionEntity
import de.bixilon.minosoft.data.entities.block.container.ContainerBlockEntity
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

abstract class StorageBlockEntity(connection: PlayConnection) : ContainerBlockEntity(connection), BlockActionEntity {

    var viewing: Int = 0
        private set

    val closed: Boolean get() = viewing <= 0

    override fun setBlockActionData(type: Int, data: Int) {
        when (type) {
            1 -> setViewing(data and 0xFF)
        }
    }

    protected fun setViewing(viewing: Int) {
        if (this.viewing == viewing) return
        val previous = this.viewing
        this.viewing = viewing

        when {
            viewing == 0 -> onClose()
            previous == 0 -> onOpen()
            else -> onViewingChange(viewing)
        }
    }

    protected open fun onViewingChange(viewing: Int) = Unit

    protected open fun onOpen() = Unit

    protected open fun onClose() = Unit
}
