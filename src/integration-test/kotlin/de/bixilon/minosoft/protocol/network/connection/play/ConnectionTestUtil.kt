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

import de.bixilon.kutil.observer.DataObserver
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.assets.TestAssetsManager
import de.bixilon.minosoft.config.profile.ProfileTestUtil.createProfiles
import de.bixilon.minosoft.data.accounts.types.offline.OfflineAccount
import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.world.WorldTestUtil.createWorld
import de.bixilon.minosoft.data.world.WorldTestUtil.initialize
import de.bixilon.minosoft.modding.event.master.EventMaster
import de.bixilon.minosoft.protocol.network.network.client.test.TestNetwork
import de.bixilon.minosoft.test.IT
import de.bixilon.minosoft.test.IT.reference


object ConnectionTestUtil {

    init {
        reference()
    }

    fun createConnection(worldSize: Int = 0): PlayConnection {
        val connection = IT.OBJENESIS.newInstance(PlayConnection::class.java)
        connection::account.forceSet(OfflineAccount("dummy"))
        connection::version.forceSet(IT.VERSION)
        connection::registries.forceSet(Registries())
        connection.registries.parentRegistries = IT.VERSION.registries
        connection::world.forceSet(createWorld(connection))
        connection::player.forceSet(LocalPlayerEntity(connection.account, connection, null))
        connection::network.forceSet(TestNetwork())
        connection::events.forceSet(EventMaster())
        connection::profiles.forceSet(createProfiles())
        connection::assetsManager.forceSet(TestAssetsManager)
        connection::state.forceSet(DataObserver(PlayConnectionStates.PLAYING))

        if (worldSize > 0) {
            connection.world.initialize(worldSize)
        }

        return connection
    }
}
