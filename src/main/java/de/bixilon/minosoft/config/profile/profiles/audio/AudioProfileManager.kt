package de.bixilon.minosoft.config.profile.profiles.audio

import com.google.common.collect.HashBiMap
import de.bixilon.minosoft.config.profile.GlobalProfileManager
import de.bixilon.minosoft.config.profile.ProfileManager
import de.bixilon.minosoft.config.profile.util.ProfileDelegate
import de.bixilon.minosoft.modding.event.master.GlobalEventMaster
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.KUtil.unsafeCast
import java.util.concurrent.locks.ReentrantLock

object AudioProfileManager : ProfileManager<AudioProfile> {
    override val namespace = "minosoft:audio".toResourceLocation()
    override val latestVersion = 1
    override val saveLock = ReentrantLock()
    override val profileClass = AudioProfile::class.java


    override var currentLoadingPath: String? = null
    override val profiles: HashBiMap<String, AudioProfile> = HashBiMap.create()

    override var selected: AudioProfile = null.unsafeCast()
        set(value) {
            field = value
            GlobalProfileManager.selectProfile(this, value)
            GlobalEventMaster.fireEvent(AudioProfileSelectEvent(value))
        }

    override fun createDefaultProfile(name: String): AudioProfile {
        currentLoadingPath = name
        val profile = AudioProfile("Default audio profile")
        currentLoadingPath = null
        profiles[name] = profile

        return profile
    }

    override fun <V> delegate(value: V, checkEquals: Boolean, verify: ((V) -> Unit)?): ProfileDelegate<V> {
        return ProfileDelegate(value, checkEquals, this, currentLoadingPath ?: throw IllegalAccessException("Delegate can only be created while loading or creating profiles!"), verify)
    }
}
