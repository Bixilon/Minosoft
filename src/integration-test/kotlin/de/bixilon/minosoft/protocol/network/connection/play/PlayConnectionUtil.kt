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

package de.bixilon.minosoft.protocol.network.connection.play

import de.bixilon.kutil.collections.CollectionUtil.lockMapOf
import de.bixilon.kutil.watcher.DataWatcher.Companion.watched
import de.bixilon.minosoft.IT
import de.bixilon.minosoft.IT.reference
import de.bixilon.minosoft.data.accounts.types.offline.OfflineAccount
import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity
import de.bixilon.minosoft.data.registries.dimension.DimensionProperties
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.border.WorldBorder
import de.bixilon.minosoft.modding.event.master.EventMaster
import de.bixilon.minosoft.protocol.network.network.client.test.TestNetwork
import de.bixilon.minosoft.util.KUtil.forceSet
import org.objenesis.ObjenesisStd


object PlayConnectionUtil {
    private val OBJENESIS = ObjenesisStd()

    init {
        reference()
    }

    fun createWorld(): World {
        val world = OBJENESIS.newInstance(World::class.java)
        world::chunks.forceSet(lockMapOf())
        world::border.forceSet(WorldBorder())
        world::dimension.forceSet(watched(DimensionProperties()))

        return world
    }

    fun createConnection(): PlayConnection {
        val connection = OBJENESIS.newInstance(PlayConnection::class.java)
        connection::account.forceSet(OfflineAccount("dummy"))
        connection::version.forceSet(IT.VERSION)
        connection::registries.forceSet(Registries())
        connection.registries.parentRegistries = IT.VERSION.registries
        connection::world.forceSet(createWorld())

        connection::network.forceSet(TestNetwork())
        connection::player.forceSet(LocalPlayerEntity(connection.account, connection, null))
        connection::events.forceSet(EventMaster())

        return connection
    }
}
