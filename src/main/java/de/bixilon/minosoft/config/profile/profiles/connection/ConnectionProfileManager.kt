package de.bixilon.minosoft.config.profile.profiles.connection

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.collections.CollectionUtil.synchronizedBiMapOf
import de.bixilon.kutil.collections.map.bi.AbstractMutableBiMap
import de.bixilon.kutil.watcher.map.bi.BiMapDataWatcher.Companion.watchedBiMap
import de.bixilon.minosoft.config.profile.GlobalProfileManager
import de.bixilon.minosoft.config.profile.ProfileManager
import de.bixilon.minosoft.modding.event.master.GlobalEventMaster
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid
import java.util.concurrent.locks.ReentrantLock

object ConnectionProfileManager : ProfileManager<ConnectionProfile> {
    override val namespace = "minosoft:connection".toResourceLocation()
    override val latestVersion = 1
    override val saveLock = ReentrantLock()
    override val profileClass = ConnectionProfile::class.java
    override val icon = FontAwesomeSolid.NETWORK_WIRED


    override var currentLoadingPath: String? = null
    override val profiles: AbstractMutableBiMap<String, ConnectionProfile> by watchedBiMap(synchronizedBiMapOf())

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
