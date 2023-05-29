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

package de.bixilon.minosoft.config.profile

import com.fasterxml.jackson.databind.type.MapType
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.collections.CollectionUtil.lockMapOf
import de.bixilon.kutil.collections.map.LockMap
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.concurrent.schedule.RepeatedTask
import de.bixilon.kutil.concurrent.schedule.TaskScheduler
import de.bixilon.kutil.file.FileUtil
import de.bixilon.kutil.file.FileUtil.read
import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.config.profile.profiles.account.AccountProfileManager
import de.bixilon.minosoft.config.profile.profiles.audio.AudioProfileManager
import de.bixilon.minosoft.config.profile.profiles.block.BlockProfileManager
import de.bixilon.minosoft.config.profile.profiles.connection.ConnectionProfileManager
import de.bixilon.minosoft.config.profile.profiles.controls.ControlsProfileManager
import de.bixilon.minosoft.config.profile.profiles.entity.EntityProfileManager
import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager
import de.bixilon.minosoft.config.profile.profiles.gui.GUIProfileManager
import de.bixilon.minosoft.config.profile.profiles.other.OtherProfileManager
import de.bixilon.minosoft.config.profile.profiles.particle.ParticleProfileManager
import de.bixilon.minosoft.config.profile.profiles.rendering.RenderingProfileManager
import de.bixilon.minosoft.config.profile.profiles.resources.ResourcesProfileManager
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.eros.crash.ErosCrashReport.Companion.crash
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.KUtil.child
import de.bixilon.minosoft.util.json.Jackson
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

object GlobalProfileManager {
    private val SELECTED_PROFILES_PATH = RunConfiguration.CONFIG_DIRECTORY.resolve("selected_profiles.json").toFile()
    val DEFAULT_MANAGERS: Map<ResourceLocation, ProfileManager<out Profile>>
    private val SELECTED_PROFILES_TYPE: MapType = Jackson.MAPPER.typeFactory.constructMapType(HashMap::class.java, ResourceLocation::class.java, String::class.java)
    val CLASS_MAPPING: Map<Class<out Profile>, ProfileManager<*>>

    init {
        val map: MutableMap<ResourceLocation, ProfileManager<out Profile>> = mutableMapOf()
        val classMapping: MutableMap<Class<out Profile>, ProfileManager<*>> = mutableMapOf()
        val list = listOf(
            ErosProfileManager,
            ParticleProfileManager,
            AudioProfileManager,
            EntityProfileManager,
            ResourcesProfileManager,
            AccountProfileManager,
            RenderingProfileManager,
            BlockProfileManager,
            ConnectionProfileManager,
            GUIProfileManager,
            ControlsProfileManager,
            OtherProfileManager,
        )

        for (manager in list) {
            map.put(manager.namespace, manager)?.let { throw IllegalStateException("Duplicate profile namespace: ${manager.namespace}") }
            classMapping[manager.profileClass] = manager
        }

        this.DEFAULT_MANAGERS = map
        this.CLASS_MAPPING = classMapping
    }

    private var initialized = false
    private var loading = true
    private var selectedProfilesChanges = false
    private val selectedProfiles: LockMap<ResourceLocation, String> = lockMapOf()

    private fun loadSelectedProfiles() {
        selectedProfiles.lock.lock()
        try {
            if (!SELECTED_PROFILES_PATH.exists()) {
                return
            }

            this.selectedProfiles.unsafe.clear()
            this.selectedProfiles.unsafe.putAll(Jackson.MAPPER.readValue(SELECTED_PROFILES_PATH.read(), SELECTED_PROFILES_TYPE))
        } finally {
            selectedProfiles.lock.unlock()
        }
    }

    private fun saveSelectedProfiles() {
        if (loading || !selectedProfilesChanges) {
            return
        }
        selectedProfiles.lock.lock()
        DefaultThreadPool += {
            try {
                val data: Map<String, String> = Jackson.MAPPER.convertValue(selectedProfiles.unsafe, SELECTED_PROFILES_TYPE)
                val jsonString = Jackson.MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(data)

                FileUtil.safeSaveToFile(SELECTED_PROFILES_PATH, jsonString)
                selectedProfilesChanges = false
            } catch (exception: Exception) {
                exception.crash()
            } finally {
                selectedProfiles.lock.unlock()
            }
        }
    }

    @Synchronized
    fun initialize(latch: AbstractLatch?) {
        if (initialized) {
            throw IllegalStateException("Already initialized!")
        }
        Log.log(LogMessageType.PROFILES, LogLevels.VERBOSE) { "Loading profiles..." }
        loadSelectedProfiles()
        val innerLatch = latch.child(1)
        for ((namespace, manager) in DEFAULT_MANAGERS) {
            innerLatch.inc()
            DefaultThreadPool += { manager.load(selectedProfiles[namespace]);innerLatch.dec() }
        }
        innerLatch.dec()
        innerLatch.await()

        loading = false
        if (selectedProfilesChanges) {
            saveSelectedProfiles()
        }
        TaskScheduler += RepeatedTask(1000) {
            for (manager in DEFAULT_MANAGERS.values) {
                for (profile in manager.profiles.values) {
                    if (profile.saved) {
                        continue
                    }
                    val castedManager = manager.unsafeCast<ProfileManager<Profile>>()
                    castedManager.saveAsync(profile)
                }

            }
        }
        initialized = true
        Log.log(LogMessageType.PROFILES, LogLevels.INFO) { "Profiles loaded!" }
    }

    fun <T : Profile> selectProfile(profileManager: ProfileManager<T>, profile: T?) {
        if (profile == null) {
            selectedProfiles -= profileManager.namespace
        } else {
            val name = profile.name
            if (selectedProfiles.put(profileManager.namespace, name) == name) {
                return
            }
        }
        selectedProfilesChanges = true
        if (!loading) {
            saveSelectedProfiles()
        }
    }

    operator fun get(resourceLocation: ResourceLocation?): ProfileManager<out Profile>? = DEFAULT_MANAGERS[resourceLocation]
}
