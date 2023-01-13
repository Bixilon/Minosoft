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

package de.bixilon.minosoft.data.container.actions

import de.bixilon.kutil.collections.CollectionUtil.synchronizedBiMapOf
import de.bixilon.kutil.collections.map.bi.SynchronizedBiMap
import de.bixilon.minosoft.data.container.Container
import java.util.concurrent.atomic.AtomicInteger

class ContainerActions(private val container: Container) {
    private var id = AtomicInteger()
    private val actions: SynchronizedBiMap<Int, ContainerAction> = synchronizedBiMapOf()


    fun createId(action: ContainerAction): Int {
        val id = id.getAndIncrement()
        actions[id] = action
        return id
    }

    fun invoke(action: ContainerAction) {
        action.invoke(container.connection, container.id ?: return, container)
    }

    fun acknowledge(actionId: Int) {
        actions.remove(actionId)
    }

    fun revert(actionId: Int) {
        actions.remove(actionId)?.let { revert(it) }
    }

    fun revert(action: ContainerAction) {
        action.revert(container.connection, container.id ?: return, container)
    }
}
