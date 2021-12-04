package de.bixilon.minosoft.config.profile.profiles.hud

import com.google.common.collect.HashBiMap
import de.bixilon.minosoft.config.profile.GlobalProfileManager
import de.bixilon.minosoft.config.profile.ProfileManager
import de.bixilon.minosoft.modding.event.master.GlobalEventMaster
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.KUtil.unsafeCast
import java.util.concurrent.locks.ReentrantLock

object HUDProfileManager : ProfileManager<HUDProfile> {
    override val namespace = "minosoft:hud".toResourceLocation()
    override val latestVersion = 1
    override val saveLock = ReentrantLock()
    override val profileClass = HUDProfile::class.java


    override var currentLoadingPath: String? = null
    override val profiles: HashBiMap<String, HUDProfile> = HashBiMap.create()

    override var selected: HUDProfile = null.unsafeCast()
        set(value) {
            field = value
            GlobalProfileManager.selectProfile(this, value)
            GlobalEventMaster.fireEvent(HUDProfileSelectEvent(value))
        }

    override fun createDefaultProfile(name: String): HUDProfile {
        currentLoadingPath = name
        val profile = HUDProfile("Default hud profile")
        currentLoadingPath = null
        profiles[name] = profile

        return profile
    }
}
