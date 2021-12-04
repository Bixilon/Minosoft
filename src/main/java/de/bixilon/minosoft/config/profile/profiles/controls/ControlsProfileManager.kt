package de.bixilon.minosoft.config.profile.profiles.controls

import com.google.common.collect.HashBiMap
import de.bixilon.minosoft.config.profile.GlobalProfileManager
import de.bixilon.minosoft.config.profile.ProfileManager
import de.bixilon.minosoft.modding.event.master.GlobalEventMaster
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.KUtil.unsafeCast
import java.util.concurrent.locks.ReentrantLock

object ControlsProfileManager : ProfileManager<ControlsProfile> {
    override val namespace = "minosoft:controls".toResourceLocation()
    override val latestVersion = 1
    override val saveLock = ReentrantLock()
    override val profileClass = ControlsProfile::class.java


    override var currentLoadingPath: String? = null
    override val profiles: HashBiMap<String, ControlsProfile> = HashBiMap.create()

    override var selected: ControlsProfile = null.unsafeCast()
        set(value) {
            field = value
            GlobalProfileManager.selectProfile(this, value)
            GlobalEventMaster.fireEvent(ControlsProfileSelectEvent(value))
        }

    override fun createDefaultProfile(name: String): ControlsProfile {
        currentLoadingPath = name
        val profile = ControlsProfile("Default controls profile")
        currentLoadingPath = null
        profiles[name] = profile

        return profile
    }
}
