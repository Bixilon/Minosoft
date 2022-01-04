package de.bixilon.minosoft.config.profile.profiles.entity

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

object EntityProfileManager : ProfileManager<EntityProfile> {
    override val namespace = "minosoft:entity".toResourceLocation()
    override val latestVersion = 1
    override val saveLock = ReentrantLock()
    override val profileClass = EntityProfile::class.java
    override val icon = FontAwesomeSolid.SKULL


    override var currentLoadingPath: String? = null
    override val profiles: AbstractMutableBiMap<String, EntityProfile> by watchedBiMap(synchronizedBiMapOf())

    override var selected: EntityProfile = null.unsafeCast()
        set(value) {
            field = value
            GlobalProfileManager.selectProfile(this, value)
            GlobalEventMaster.fireEvent(EntityProfileSelectEvent(value))
        }

    override fun createProfile(name: String, description: String?): EntityProfile {
        currentLoadingPath = name
        val profile = EntityProfile(description ?: "Default entity profile")
        currentLoadingPath = null
        profiles[name] = profile

        return profile
    }
}
