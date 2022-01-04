package de.bixilon.minosoft.config.profile.profiles.rendering

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

object RenderingProfileManager : ProfileManager<RenderingProfile> {
    override val namespace = "minosoft:rendering".toResourceLocation()
    override val latestVersion = 1
    override val saveLock = ReentrantLock()
    override val profileClass = RenderingProfile::class.java
    override val icon = FontAwesomeSolid.VECTOR_SQUARE


    override var currentLoadingPath: String? = null
    override val profiles: AbstractMutableBiMap<String, RenderingProfile> by watchedBiMap(synchronizedBiMapOf())

    override var selected: RenderingProfile = null.unsafeCast()
        set(value) {
            field = value
            GlobalProfileManager.selectProfile(this, value)
            GlobalEventMaster.fireEvent(RenderingProfileSelectEvent(value))
        }

    override fun createProfile(name: String, description: String?): RenderingProfile {
        currentLoadingPath = name
        val profile = RenderingProfile(description ?: "Default rendering profile")
        currentLoadingPath = null
        profiles[name] = profile

        return profile
    }
}
