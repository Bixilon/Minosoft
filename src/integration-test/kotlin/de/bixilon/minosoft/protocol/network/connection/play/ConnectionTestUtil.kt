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
import de.bixilon.minosoft.assets.connection.ConnectionAssetsManager
import de.bixilon.minosoft.assets.minecraft.MinecraftPackFormat.packFormat
import de.bixilon.minosoft.assets.properties.manager.AssetsManagerProperties
import de.bixilon.minosoft.assets.properties.manager.pack.PackProperties
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
import de.bixilon.minosoft.test.IT.OBJENESIS
import de.bixilon.minosoft.test.IT.reference
import de.bixilon.minosoft.test.ITUtil
import de.bixilon.minosoft.util.KUtil.startInit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.jvm.javaField


object ConnectionTestUtil {
    private val profiles = createProfiles()

    init {
        reference()
    }

    private val LANGUAGE = PlayConnection::language.javaField!!
    private val SEQUENCE = PlayConnection::sequence.javaField!!
    private val ACCOUNT = PlayConnection::account.javaField!!
    private val VERSION = PlayConnection::version.javaField!!
    private val REGISTRIES = PlayConnection::registries.javaField!!
    private val WORLD = PlayConnection::world.javaField!!
    private val PLAYER = PlayConnection::player.javaField!!
    private val NETWORK = PlayConnection::network.javaField!!
    private val EVENTS = PlayConnection::events.javaField!!
    private val PROFILES = PlayConnection::profiles.javaField!!
    private val ASSETS_MANAGER = PlayConnection::assetsManager.javaField!!
    private val STATE = PlayConnection::state.javaField!!
    private val TAGS = PlayConnection::tags.javaField!!
    private val LEGACY_TAGS = PlayConnection::legacyTags.javaField!!
    private val CAMERA = PlayConnection::camera.javaField!!

    private val language = LanguageList(mutableListOf())
    private val signature = OBJENESIS.newInstance(SignatureKeyManagement::class.java)


    fun createConnection(worldSize: Int = 0, light: Boolean = false, version: String? = null): PlayConnection {
        val connection = OBJENESIS.newInstance(PlayConnection::class.java)
        LANGUAGE.forceSet(connection, language)
        val version = if (version == null) IT.VERSION else Versions[version] ?: throw IllegalArgumentException("Can not find version: $version")
        SEQUENCE.forceSet(connection, AtomicInteger(1))
        ACCOUNT.forceSet(connection, TestAccount)
        VERSION.forceSet(connection, version)
        REGISTRIES.forceSet(connection, Registries())
        connection.registries.updateFlattened(version.flattened)
        connection.registries.parent = if (version == IT.VERSION) IT.REGISTRIES else ITUtil.loadRegistries(version)
        WORLD.forceSet(connection, createWorld(connection, light, (worldSize * 2 + 1).pow(2)))
        PLAYER.forceSet(connection, LocalPlayerEntity(connection.account, connection, signature))
        connection.player.startInit()
        NETWORK.forceSet(connection, TestNetwork())
        EVENTS.forceSet(connection, EventMaster())
        PROFILES.forceSet(connection, profiles)
        ASSETS_MANAGER.forceSet(connection, ConnectionAssetsManager(AssetsManagerProperties(PackProperties(version.packFormat))))
        STATE.forceSet(connection, DataObserver(PlayConnectionStates.PLAYING))
        TAGS.forceSet(connection, TagManager())
        LEGACY_TAGS.forceSet(connection, FALLBACK_TAGS)
        CAMERA.forceSet(connection, ConnectionCamera(connection))
        connection.camera.init()
        if (worldSize > 0) {
            connection.world.initialize(worldSize)
        }
        return connection
    }
}
