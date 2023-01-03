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

package de.bixilon.minosoft.data.container

import de.bixilon.minosoft.data.container.types.generic.Generic9x3Container
import de.bixilon.minosoft.data.container.types.processing.smelting.FurnaceContainer
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.containers.ContainerFactory
import de.bixilon.minosoft.data.registries.containers.ContainerType
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil.createConnection
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.test.IT
import de.bixilon.minosoft.test.IT.reference
import de.bixilon.minosoft.util.KUtil.minosoft


object ContainerTestUtil {
    private val container = ContainerType(minosoft("test"), factory = GenericContainerFactory)
    private val chest = IT.REGISTRIES.containerType[Generic9x3Container]!!
    private val furnace = IT.REGISTRIES.containerType[FurnaceContainer]!!


    init {
        reference()
    }

    fun createContainer(connection: PlayConnection = createConnection()): Container {
        val container = Container(connection, this.container)
        connection.player.containers[9] = container
        return container
    }

    fun createChest(connection: PlayConnection = createConnection()): Generic9x3Container {
        val container = Generic9x3Container(connection, this.chest, null)
        connection.player.containers[9] = container
        return container
    }

    fun createFurnace(connection: PlayConnection = createConnection()): FurnaceContainer {
        val container = FurnaceContainer(connection, this.furnace, null)
        connection.player.containers[9] = container
        return container
    }

    private object GenericContainerFactory : ContainerFactory<Container> {
        override val identifier: ResourceLocation = minosoft("test")

        override fun build(connection: PlayConnection, type: ContainerType, title: ChatComponent?): Container {
            return Container(connection, type, title)
        }
    }
}
