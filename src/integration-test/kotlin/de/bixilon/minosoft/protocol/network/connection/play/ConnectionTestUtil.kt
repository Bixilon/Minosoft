/*
 * Minosoft
 * Copyright (C) 2020-2024 Moritz Zwerger
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
import de.bixilon.kutil.reflection.ReflectionUtil.field
import de.bixilon.kutil.reflection.ReflectionUtil.jvmField
import de.bixilon.minosoft.assets.connection.ConnectionAssetsManager
import de.bixilon.minosoft.assets.minecraft.MinecraftPackFormat.packFormat
import de.bixilon.minosoft.assets.properties.manager.AssetsManagerProperties
import de.bixilon.minosoft.assets.properties.manager.pack.PackProperties
import de.bixilon.minosoft.camera.ConnectionCamera
import de.bixilon.minosoft.config.profile.ProfileTestUtil.createProfiles
import de.bixilon.minosoft.data.accounts.types.test.TestAccount
import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity
import de.bixilon.minosoft.data.entities.entities.player.local.SignatureKeyManagement
import de.bixilon.minosoft.data.language.manager.LanguageManager
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.world.WorldTestUtil.createWorld
import de.bixilon.minosoft.data.world.WorldTestUtil.initialize
import de.bixilon.minosoft.data.world.biome.source.DummyBiomeSource
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


object ConnectionTestUtil {
    private val profiles = createProfiles()

    init {
        reference()
    }

    private val LANGUAGE = PlayConnection::language.jvmField.field
    private val SEQUENCE = PlayConnection::sequence.jvmField.field
    private val ACCOUNT = PlayConnection::account.jvmField.field
    private val VERSION = PlayConnection::version.jvmField.field
    private val REGISTRIES = PlayConnection::registries.jvmField.field
    private val WORLD = PlayConnection::world.jvmField.field
    private val PLAYER = PlayConnection::player.jvmField.field
    private val NETWORK = PlayConnection::network.jvmField.field
    private val EVENTS = PlayConnection::events.jvmField.field
    private val PROFILES = PlayConnection::profiles.jvmField.field
    private val ASSETS_MANAGER = PlayConnection::assetsManager.jvmField.field
    private val STATE = PlayConnection::state.jvmField.field
    private val TAGS = PlayConnection::tags.jvmField.field
    private val LEGACY_TAGS = PlayConnection::legacyTags.jvmField.field
    private val CAMERA = PlayConnection::camera.jvmField.field

    private val language = LanguageManager()
    private val signature = OBJENESIS.newInstance(SignatureKeyManagement::class.java)


    fun createConnection(worldSize: Int = 0, light: Boolean = false, version: String? = null): PlayConnection {
        val connection = OBJENESIS.newInstance(PlayConnection::class.java)
        LANGUAGE.set(connection, language)
        val version = if (version == null) IT.VERSION else Versions[version] ?: throw IllegalArgumentException("Can not find version: $version")
        SEQUENCE.set(connection, AtomicInteger(1))
        ACCOUNT.set(connection, TestAccount)
        VERSION.set(connection, version)
        REGISTRIES.set(connection, Registries())
        connection.registries.updateFlattened(version.flattened)
        connection.registries.parent = if (version == IT.VERSION) IT.REGISTRIES else ITUtil.loadRegistries(version)
        WORLD.set(connection, createWorld(connection, light, (worldSize * 2 + 1).pow(2)))
        PLAYER.set(connection, LocalPlayerEntity(connection.account, connection, signature))
        connection.player.startInit()
        NETWORK.set(connection, TestNetwork(connection))
        EVENTS.set(connection, EventMaster())
        PROFILES.set(connection, profiles)
        ASSETS_MANAGER.set(connection, ConnectionAssetsManager(AssetsManagerProperties(PackProperties(version.packFormat))))
        STATE.set(connection, DataObserver(PlayConnectionStates.PLAYING))
        TAGS.set(connection, TagManager())
        LEGACY_TAGS.set(connection, FALLBACK_TAGS)
        CAMERA.set(connection, ConnectionCamera(connection))
        connection.camera.init()
        if (worldSize > 0) {
            connection.world.initialize(worldSize) { DummyBiomeSource(null) }
        }
        return connection
    }
}
