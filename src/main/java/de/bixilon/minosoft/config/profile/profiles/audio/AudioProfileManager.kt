package de.bixilon.minosoft.config.profile.profiles.audio

import com.google.common.collect.HashBiMap
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.config.profile.GlobalProfileManager
import de.bixilon.minosoft.config.profile.ProfileManager
import de.bixilon.minosoft.modding.event.master.GlobalEventMaster
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import org.kordamp.ikonli.Ikon
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid
import java.util.concurrent.locks.ReentrantLock

object AudioProfileManager : ProfileManager<AudioProfile> {
    override val namespace = "minosoft:audio".toResourceLocation()
    override val latestVersion = 1
    override val saveLock = ReentrantLock()
    override val profileClass = AudioProfile::class.java
    override val icon: Ikon = FontAwesomeSolid.HEADPHONES


    override var currentLoadingPath: String? = null
    override val profiles: HashBiMap<String, AudioProfile> = HashBiMap.create()

    override var selected: AudioProfile = null.unsafeCast()
        set(value) {
            field = value
            GlobalProfileManager.selectProfile(this, value)
            GlobalEventMaster.fireEvent(AudioProfileSelectEvent(value))
        }

    override fun createProfile(name: String, description: String?): AudioProfile {
        currentLoadingPath = name
        val profile = AudioProfile(description ?: "Default audio profile")
        currentLoadingPath = null
        profiles[name] = profile

        return profile
    }
}
