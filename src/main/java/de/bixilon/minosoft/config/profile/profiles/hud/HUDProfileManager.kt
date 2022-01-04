package de.bixilon.minosoft.config.profile.profiles.hud

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

object HUDProfileManager : ProfileManager<HUDProfile> {
    override val namespace = "minosoft:hud".toResourceLocation()
    override val latestVersion = 1
    override val saveLock = ReentrantLock()
    override val profileClass = HUDProfile::class.java
    override val icon = FontAwesomeSolid.TACHOMETER_ALT


    override var currentLoadingPath: String? = null
    override val profiles: AbstractMutableBiMap<String, HUDProfile> by watchedBiMap(synchronizedBiMapOf())

    override var selected: HUDProfile = null.unsafeCast()
        set(value) {
            field = value
            GlobalProfileManager.selectProfile(this, value)
            GlobalEventMaster.fireEvent(HUDProfileSelectEvent(value))
        }

    override fun createProfile(name: String, description: String?): HUDProfile {
        currentLoadingPath = name
        val profile = HUDProfile(description ?: "Default hud profile")
        currentLoadingPath = null
        profiles[name] = profile

        return profile
    }
}
