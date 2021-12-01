package de.bixilon.minosoft.config.config2

import de.bixilon.minosoft.config.config2.config.Profile
import de.bixilon.minosoft.config.config2.config.eros.ErosProfileManager
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.eros.crash.ErosCrashReport.Companion.crash
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.KUtil.lockMapOf
import de.bixilon.minosoft.util.Util
import de.bixilon.minosoft.util.collections.LockMap
import de.bixilon.minosoft.util.json.jackson.Jackson
import de.bixilon.minosoft.util.task.pool.DefaultThreadPool
import java.io.File

object GlobalProfileManager {
    val DEFAULT_MANAGERS: List<ProfileManager<*>> = listOf(
        ErosProfileManager,
    )
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
            this.selectedProfiles.original.putAll(Jackson.MAPPER.convertValue(Util.readFile(file.path), Jackson.JSON_MAP_TYPE))
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
                val data: Map<String, String> = Jackson.MAPPER.convertValue(selectedProfiles.original, Jackson.JSON_MAP_TYPE)
                val jsonString = Jackson.MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(data)

                KUtil.safeSaveToFile(File(RunConfiguration.HOME_DIRECTORY + "config/selected_profiles.json"), jsonString)
            } catch (exception: Exception) {
                exception.crash()
            } finally {
                selectedProfiles.lock.unlock()
            }
        }
    }

    fun load() {
        loadSelectedProfiles()
        for (manager in DEFAULT_MANAGERS) {
            manager.load(selectedProfiles[manager.namespace])
        }
        loading = false
        if (selectedProfilesChanges) {
            saveSelectedProfiles()
        }
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
}
