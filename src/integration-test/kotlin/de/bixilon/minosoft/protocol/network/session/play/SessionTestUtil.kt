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

package de.bixilon.minosoft.protocol.network.session.play

import de.bixilon.kotlinglm.pow
import de.bixilon.kutil.observer.DataObserver
import de.bixilon.kutil.reflection.ReflectionUtil.field
import de.bixilon.minosoft.assets.minecraft.MinecraftPackFormat.packFormat
import de.bixilon.minosoft.assets.properties.manager.AssetsManagerProperties
import de.bixilon.minosoft.assets.properties.manager.pack.PackProperties
import de.bixilon.minosoft.assets.session.SessionAssetsManager
import de.bixilon.minosoft.camera.SessionCamera
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


object SessionTestUtil {
    private val profiles = createProfiles()

    init {
        reference()
    }

    private val LANGUAGE = PlaySession::language.field
    private val SEQUENCE = PlaySession::sequence.field
    private val ACCOUNT = PlaySession::account.field
    private val VERSION = PlaySession::version.field
    private val REGISTRIES = PlaySession::registries.field
    private val WORLD = PlaySession::world.field
    private val PLAYER = PlaySession::player.field
    private val CONNECTION = PlaySession::connection.field
    private val EVENTS = PlaySession::events.field
    private val PROFILES = PlaySession::profiles.field
    private val ASSETS_MANAGER = PlaySession::assetsManager.field
    private val STATE = PlaySession::state.field
    private val TAGS = PlaySession::tags.field
    private val LEGACY_TAGS = PlaySession::legacyTags.field
    private val CAMERA = PlaySession::camera.field

    private val language = LanguageManager()
    private val signature = OBJENESIS.newInstance(SignatureKeyManagement::class.java)


    fun createSession(worldSize: Int = 0, light: Boolean = false, version: String? = null): PlaySession {
        val session = OBJENESIS.newInstance(PlaySession::class.java)
        LANGUAGE.set(session, language)
        val version = if (version == null) IT.VERSION else Versions[version] ?: throw IllegalArgumentException("Can not find version: $version")
        SEQUENCE.set(session, AtomicInteger(1))
        ACCOUNT.set(session, TestAccount)
        VERSION.set(session, version)
        REGISTRIES.set(session, Registries())
        session.registries.updateFlattened(version.flattened)
        session.registries.parent = if (version == IT.VERSION) IT.REGISTRIES else ITUtil.loadRegistries(version)
        WORLD.set(session, createWorld(session, light, (worldSize * 2 + 1).pow(2)))
        PLAYER.set(session, LocalPlayerEntity(session.account, session, signature))
        session.player.startInit()
        CONNECTION.set(session, TestNetwork(session))
        EVENTS.set(session, EventMaster())
        PROFILES.set(session, profiles)
        ASSETS_MANAGER.set(session, SessionAssetsManager(AssetsManagerProperties(PackProperties(version.packFormat))))
        STATE.set(session, DataObserver(PlaySessionStates.PLAYING))
        TAGS.set(session, TagManager())
        LEGACY_TAGS.set(session, FALLBACK_TAGS)
        CAMERA.set(session, SessionCamera(session))
        session.camera.init()
        if (worldSize > 0) {
            session.world.initialize(worldSize) { DummyBiomeSource(null) }
        }
        return session
    }
}
