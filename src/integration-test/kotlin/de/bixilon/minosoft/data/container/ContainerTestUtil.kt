/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.container

import de.bixilon.minosoft.data.container.types.PlayerInventory
import de.bixilon.minosoft.data.container.types.UnknownContainer
import de.bixilon.minosoft.data.container.types.generic.Generic9x3Container
import de.bixilon.minosoft.data.container.types.processing.smelting.FurnaceContainer
import de.bixilon.minosoft.data.registries.containers.ContainerFactory
import de.bixilon.minosoft.data.registries.containers.ContainerType
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.network.session.play.SessionTestUtil.createSession
import de.bixilon.minosoft.test.IT


object ContainerTestUtil {
    private val container = ContainerType(minosoft("test"), factory = GenericContainerFactory)
    private val chest = IT.REGISTRIES.containerType[Generic9x3Container]!!
    private val furnace = IT.REGISTRIES.containerType[FurnaceContainer]!!


    fun createInventory(session: PlaySession = createSession()): Container {
        val inventory = PlayerInventory(session.player.items, session)
        session.player.items.containers[0] = inventory
        return inventory
    }

    fun createContainer(session: PlaySession = createSession()): Container {
        val container = UnknownContainer(session, this.container, id = 9)
        session.player.items.containers[9] = container
        return container
    }

    fun createChest(session: PlaySession = createSession()): Generic9x3Container {
        val container = Generic9x3Container(session, this.chest, null, id = 9)
        session.player.items.containers[9] = container
        return container
    }

    fun createFurnace(session: PlaySession = createSession()): FurnaceContainer {
        val container = FurnaceContainer(session, this.furnace, null, id = 9)
        session.player.items.containers[9] = container
        return container
    }

    private object GenericContainerFactory : ContainerFactory<Container> {
        override val identifier = minosoft("test")

        override fun build(session: PlaySession, type: ContainerType, title: ChatComponent?, slots: Int, id: Int): Container {
            return UnknownContainer(session, type, title, id)
        }
    }
}
