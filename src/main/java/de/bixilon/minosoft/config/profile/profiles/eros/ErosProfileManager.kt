package de.bixilon.minosoft.config.profile.profiles.eros

import com.google.common.collect.HashBiMap
import de.bixilon.minosoft.config.profile.GlobalProfileManager
import de.bixilon.minosoft.config.profile.ProfileManager
import de.bixilon.minosoft.config.profile.util.ProfileDelegate
import de.bixilon.minosoft.modding.event.master.GlobalEventMaster
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.KUtil.unsafeCast
import de.bixilon.minosoft.util.json.jackson.Jackson
import java.util.concurrent.locks.ReentrantLock

object ErosProfileManager : ProfileManager<ErosProfile> {
    override val namespace = "minosoft:eros".toResourceLocation()
    override val latestVersion = 1
    override val saveLock = ReentrantLock()


    private var currentLoadingPath: String? = null
    override val profiles: HashBiMap<String, ErosProfile> = HashBiMap.create()

    override var selected: ErosProfile = null.unsafeCast()
        set(value) {
            field = value
            GlobalProfileManager.selectProfile(this, value)
            GlobalEventMaster.fireEvent(ErosProfileSelectEvent(value))
        }

    override fun selectDefault() {
        selected = profiles[ProfileManager.DEFAULT_PROFILE_NAME] ?: createDefaultProfile()
    }

    override fun createDefaultProfile(): ErosProfile {
        currentLoadingPath = ProfileManager.DEFAULT_PROFILE_NAME
        val profile = ErosProfile("Default eros profile")
        currentLoadingPath = null
        profiles[ProfileManager.DEFAULT_PROFILE_NAME] = profile

        return profile
    }

    override fun load(name: String, data: MutableMap<String, Any?>?): ErosProfile {
        currentLoadingPath = name
        val profile: ErosProfile = if (data == null) {
            ErosProfile()
        } else {
            Jackson.MAPPER.convertValue(data, ErosProfile::class.java)
        }
        profile.saved = true
        profiles[name] = profile
        currentLoadingPath = null
        return profile
    }

    override fun serialize(profile: ErosProfile): Map<String, Any?> {
        return Jackson.MAPPER.convertValue(profile, Jackson.JSON_MAP_TYPE)
    }

    override fun <V> delegate(value: V, checkEquals: Boolean): ProfileDelegate<V> {
        return ProfileDelegate(value, checkEquals, this, currentLoadingPath ?: throw IllegalAccessException("Delegate can only be created while loading or creating profiles!"))
    }
}
