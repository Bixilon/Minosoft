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

package de.bixilon.minosoft.protocol.network.connection.play

import de.bixilon.kotlinglm.pow
import de.bixilon.kutil.observer.DataObserver
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.assets.TestAssetsManager
import de.bixilon.minosoft.camera.ConnectionCamera
import de.bixilon.minosoft.config.profile.ProfileTestUtil.createProfiles
import de.bixilon.minosoft.data.accounts.types.test.TestAccount
import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity
import de.bixilon.minosoft.data.entities.entities.player.local.SignatureKeyManagement
import de.bixilon.minosoft.data.language.lang.LanguageList
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.world.WorldTestUtil.createWorld
import de.bixilon.minosoft.data.world.WorldTestUtil.initialize
import de.bixilon.minosoft.modding.event.master.EventMaster
import de.bixilon.minosoft.protocol.network.network.client.test.TestNetwork
import de.bixilon.minosoft.protocol.versions.Versions
import de.bixilon.minosoft.tags.TagManager
import de.bixilon.minosoft.test.IT
import de.bixilon.minosoft.test.IT.FALLBACK_TAGS
import de.bixilon.minosoft.test.IT.reference
import de.bixilon.minosoft.test.ITUtil
import de.bixilon.minosoft.util.KUtil.startInit
import java.util.concurrent.atomic.AtomicInteger


object ConnectionTestUtil {
    private val profiles = createProfiles()

    init {
        reference()
    }


    fun createConnection(worldSize: Int = 0, light: Boolean = false, version: String? = null): PlayConnection {
        val connection = IT.OBJENESIS.newInstance(PlayConnection::class.java)
        connection::language.forceSet(LanguageList(mutableListOf()))
        val version = if (version == null) IT.VERSION else Versions[version] ?: throw IllegalArgumentException("Can not find version: $version")
        connection::sequence.forceSet(AtomicInteger(1))
        connection::account.forceSet(TestAccount)
        connection::version.forceSet(version)
        connection::registries.forceSet(Registries())
        connection.registries.updateFlattened(version.flattened)
        connection.registries.parent = if (version == IT.VERSION) IT.REGISTRIES else ITUtil.loadRegistries(version)
        connection::world.forceSet(createWorld(connection, light, (worldSize * 2 + 1).pow(2)))
        connection::player.forceSet(LocalPlayerEntity(connection.account, connection, SignatureKeyManagement(connection, TestAccount)))
        connection.player.startInit()
        connection::network.forceSet(TestNetwork())
        connection::events.forceSet(EventMaster())
        connection::profiles.forceSet(profiles)
        connection::assetsManager.forceSet(TestAssetsManager)
        connection::state.forceSet(DataObserver(PlayConnectionStates.PLAYING))
        connection::tags.forceSet(TagManager())
        connection::legacyTags.forceSet(FALLBACK_TAGS)
        connection::camera.forceSet(ConnectionCamera(connection))
        connection.camera.init()
        if (worldSize > 0) {
            connection.world.initialize(worldSize)
        }
        return connection
    }
}
