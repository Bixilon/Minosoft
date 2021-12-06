package de.bixilon.minosoft.config.profile.profiles.connection

import com.google.common.collect.HashBiMap
import de.bixilon.minosoft.config.profile.GlobalProfileManager
import de.bixilon.minosoft.config.profile.ProfileManager
import de.bixilon.minosoft.modding.event.master.GlobalEventMaster
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.KUtil.unsafeCast
import java.util.concurrent.locks.ReentrantLock

object ConnectionProfileManager : ProfileManager<ConnectionProfile> {
    override val namespace = "minosoft:connection".toResourceLocation()
    override val latestVersion = 1
    override val saveLock = ReentrantLock()
    override val profileClass = ConnectionProfile::class.java


    override var currentLoadingPath: String? = null
    override val profiles: HashBiMap<String, ConnectionProfile> = HashBiMap.create()

    override var selected: ConnectionProfile = null.unsafeCast()
        set(value) {
            field = value
            GlobalProfileManager.selectProfile(this, value)
            GlobalEventMaster.fireEvent(ConnectionProfileSelectEvent(value))
        }

    override fun createProfile(name: String, description: String?): ConnectionProfile {
        currentLoadingPath = name
        val profile = ConnectionProfile(description ?: "Default connection profile")
        currentLoadingPath = null
        profiles[name] = profile

        return profile
    }
}
