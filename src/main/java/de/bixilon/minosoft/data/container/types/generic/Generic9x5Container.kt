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

package de.bixilon.minosoft.data.container.types.generic

import de.bixilon.minosoft.data.registries.containers.ContainerFactory
import de.bixilon.minosoft.data.registries.containers.ContainerType
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class Generic9x5Container(connection: PlayConnection, type: ContainerType, title: ChatComponent?) : GenericContainer(5, connection, type, title) {

    companion object : ContainerFactory<Generic9x5Container> {
        override val identifier: ResourceLocation = "minecraft:generic_9x5".toResourceLocation()

        override fun build(connection: PlayConnection, type: ContainerType, title: ChatComponent?): Generic9x5Container {
            return Generic9x5Container(connection, type, title)
        }
    }
}
