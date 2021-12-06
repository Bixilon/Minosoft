package de.bixilon.minosoft.config.profile

import com.fasterxml.jackson.databind.type.MapType
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.config.profile.profiles.account.AccountProfileManager
import de.bixilon.minosoft.config.profile.profiles.audio.AudioProfileManager
import de.bixilon.minosoft.config.profile.profiles.block.BlockProfileManager
import de.bixilon.minosoft.config.profile.profiles.connection.ConnectionProfileManager
import de.bixilon.minosoft.config.profile.profiles.controls.ControlsProfileManager
import de.bixilon.minosoft.config.profile.profiles.entity.EntityProfileManager
import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager
import de.bixilon.minosoft.config.profile.profiles.hud.HUDProfileManager
import de.bixilon.minosoft.config.profile.profiles.other.OtherProfileManager
import de.bixilon.minosoft.config.profile.profiles.particle.ParticleProfileManager
import de.bixilon.minosoft.config.profile.profiles.rendering.RenderingProfileManager
import de.bixilon.minosoft.config.profile.profiles.resources.ResourcesProfileManager
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.eros.crash.ErosCrashReport.Companion.crash
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.KUtil.lockMapOf
import de.bixilon.minosoft.util.KUtil.unsafeCast
import de.bixilon.minosoft.util.Util
import de.bixilon.minosoft.util.collections.LockMap
import de.bixilon.minosoft.util.json.Jackson
import de.bixilon.minosoft.util.task.pool.DefaultThreadPool
import de.bixilon.minosoft.util.task.time.TimeWorker
import de.bixilon.minosoft.util.task.time.TimeWorkerTask
import java.io.File

object GlobalProfileManager {
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
            HUDProfileManager,
            ControlsProfileManager,
            OtherProfileManager,
        )

        for (manager in list) {
            map.put(manager.namespace, manager)?.let { throw IllegalStateException("Duplicate profile namespace: ${manager.namespace}") }
            classMapping[manager.profileClass] = manager
        }

        this.DEFAULT_MANAGERS = map.toMap()
        this.CLASS_MAPPING = classMapping.toMap()
    }

    private var initialized = false
    private var loading = true
    private var selectedProfilesChanges = false
    private val selectedProfiles: LockMap<ResourceLocation, String> = lockMapOf()

    private fun loadSelectedProfiles() {
        selectedProfiles.lock.lock()
        try {
            val file = File(RunConfiguration.HOME_DIRECTORY + "config/selected_profiles.json")
            if (!file.exists()) {
                return
            }

            this.selectedProfiles.original.clear()
            this.selectedProfiles.original.putAll(Jackson.MAPPER.readValue(Util.readFile(file.path), SELECTED_PROFILES_TYPE))
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
                val data: Map<String, String> = Jackson.MAPPER.convertValue(selectedProfiles.original, SELECTED_PROFILES_TYPE)
                val jsonString = Jackson.MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(data)

                KUtil.safeSaveToFile(File(RunConfiguration.HOME_DIRECTORY + "config/selected_profiles.json"), jsonString)
                selectedProfilesChanges = false
            } catch (exception: Exception) {
                exception.crash()
            } finally {
                selectedProfiles.lock.unlock()
            }
        }
    }

    @Synchronized
    fun initialize() {
        if (initialized) {
            throw IllegalStateException("Already initialized!")
        }
        loadSelectedProfiles()
        for ((namespace, manager) in DEFAULT_MANAGERS) {
            manager.load(selectedProfiles[namespace])
        }
        loading = false
        if (selectedProfilesChanges) {
            saveSelectedProfiles()
        }
        TimeWorker += TimeWorkerTask(1000) {
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
    }

    fun <T : Profile> selectProfile(profileManager: ProfileManager<T>, profile: T?) {
        if (profile == null) {
            selectedProfiles -= profileManager.namespace
        } else {
            val name = profileManager.getName(profile)
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
