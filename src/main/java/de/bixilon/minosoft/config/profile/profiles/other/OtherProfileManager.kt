package de.bixilon.minosoft.config.profile.profiles.other

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

object OtherProfileManager : ProfileManager<OtherProfile> {
    override val namespace = "minosoft:other".toResourceLocation()
    override val latestVersion = 1
    override val saveLock = ReentrantLock()
    override val profileClass = OtherProfile::class.java
    override val icon = FontAwesomeSolid.RANDOM


    override var currentLoadingPath: String? = null
    override val profiles: AbstractMutableBiMap<String, OtherProfile> by watchedBiMap(synchronizedBiMapOf())

    override var selected: OtherProfile = null.unsafeCast()
        set(value) {
            field = value
            GlobalProfileManager.selectProfile(this, value)
            GlobalEventMaster.fireEvent(OtherProfileSelectEvent(value))
        }

    override fun createProfile(name: String, description: String?): OtherProfile {
        currentLoadingPath = name
        val profile = OtherProfile(description ?: "Default profile for various things")
        currentLoadingPath = null
        profiles[name] = profile

        return profile
    }
}
