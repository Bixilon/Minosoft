package de.bixilon.minosoft.config.profile.profiles.controls

import com.google.common.collect.HashBiMap
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.config.profile.GlobalProfileManager
import de.bixilon.minosoft.config.profile.ProfileManager
import de.bixilon.minosoft.modding.event.master.GlobalEventMaster
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid
import java.util.concurrent.locks.ReentrantLock

object ControlsProfileManager : ProfileManager<ControlsProfile> {
    override val namespace = "minosoft:controls".toResourceLocation()
    override val latestVersion = 1
    override val saveLock = ReentrantLock()
    override val profileClass = ControlsProfile::class.java
    override val icon = FontAwesomeSolid.KEYBOARD


    override var currentLoadingPath: String? = null
    override val profiles: HashBiMap<String, ControlsProfile> = HashBiMap.create()

    override var selected: ControlsProfile = null.unsafeCast()
        set(value) {
            field = value
            GlobalProfileManager.selectProfile(this, value)
            GlobalEventMaster.fireEvent(ControlsProfileSelectEvent(value))
        }

    override fun createProfile(name: String, description: String?): ControlsProfile {
        currentLoadingPath = name
        val profile = ControlsProfile(description ?: "Default controls profile")
        currentLoadingPath = null
        profiles[name] = profile

        return profile
    }
}
