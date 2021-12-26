package de.bixilon.minosoft.config.profile.profiles.resources

import com.google.common.collect.HashBiMap
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.config.profile.GlobalProfileManager
import de.bixilon.minosoft.config.profile.ProfileManager
import de.bixilon.minosoft.modding.event.master.GlobalEventMaster
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid
import java.util.concurrent.locks.ReentrantLock

object ResourcesProfileManager : ProfileManager<ResourcesProfile> {
    override val namespace = "minosoft:resources".toResourceLocation()
    override val latestVersion = 1
    override val saveLock = ReentrantLock()
    override val profileClass = ResourcesProfile::class.java
    override val icon = FontAwesomeSolid.DOWNLOAD


    override var currentLoadingPath: String? = null
    override val profiles: HashBiMap<String, ResourcesProfile> = HashBiMap.create()

    override var selected: ResourcesProfile = null.unsafeCast()
        set(value) {
            field = value
            GlobalProfileManager.selectProfile(this, value)
            GlobalEventMaster.fireEvent(ResourcesProfileSelectEvent(value))
        }

    override fun createProfile(name: String, description: String?): ResourcesProfile {
        currentLoadingPath = name
        val profile = ResourcesProfile(description ?: "Default resources profile")
        currentLoadingPath = null
        profiles[name] = profile

        return profile
    }
}
