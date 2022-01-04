package de.bixilon.minosoft.config.profile.profiles.particle

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

object ParticleProfileManager : ProfileManager<ParticleProfile> {
    override val namespace = "minosoft:particle".toResourceLocation()
    override val latestVersion = 1
    override val saveLock = ReentrantLock()
    override val profileClass = ParticleProfile::class.java
    override val icon = FontAwesomeSolid.BIRTHDAY_CAKE


    override var currentLoadingPath: String? = null
    override val profiles: AbstractMutableBiMap<String, ParticleProfile> by watchedBiMap(synchronizedBiMapOf())

    override var selected: ParticleProfile = null.unsafeCast()
        set(value) {
            field = value
            GlobalProfileManager.selectProfile(this, value)
            GlobalEventMaster.fireEvent(ParticleProfileSelectEvent(value))
        }

    override fun createProfile(name: String, description: String?): ParticleProfile {
        currentLoadingPath = name
        val profile = ParticleProfile(description ?: "Default particle profile")
        currentLoadingPath = null
        profiles[name] = profile

        return profile
    }
}
