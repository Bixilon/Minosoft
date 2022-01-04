package de.bixilon.minosoft.config.profile.profiles.block

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

object BlockProfileManager : ProfileManager<BlockProfile> {
    override val namespace = "minosoft:block".toResourceLocation()
    override val latestVersion = 1
    override val saveLock = ReentrantLock()
    override val profileClass = BlockProfile::class.java
    override val icon = FontAwesomeSolid.CUBES


    override var currentLoadingPath: String? = null
    override val profiles: AbstractMutableBiMap<String, BlockProfile> by watchedBiMap(synchronizedBiMapOf())

    override var selected: BlockProfile = null.unsafeCast()
        set(value) {
            field = value
            GlobalProfileManager.selectProfile(this, value)
            GlobalEventMaster.fireEvent(BlockProfileSelectEvent(value))
        }

    override fun createProfile(name: String, description: String?): BlockProfile {
        currentLoadingPath = name
        val profile = BlockProfile(description ?: "Default block profile")
        currentLoadingPath = null
        profiles[name] = profile

        return profile
    }
}
