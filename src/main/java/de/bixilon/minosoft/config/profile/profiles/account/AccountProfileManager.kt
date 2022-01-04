package de.bixilon.minosoft.config.profile.profiles.account

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

object AccountProfileManager : ProfileManager<AccountProfile> {
    override val namespace = "minosoft:account".toResourceLocation()
    override val latestVersion = 1
    override val saveLock = ReentrantLock()
    override val profileClass = AccountProfile::class.java
    override val icon = FontAwesomeSolid.USER_CIRCLE


    override var currentLoadingPath: String? = null
    override val profiles: AbstractMutableBiMap<String, AccountProfile> by watchedBiMap(synchronizedBiMapOf())

    override var selected: AccountProfile = null.unsafeCast()
        set(value) {
            field = value
            GlobalProfileManager.selectProfile(this, value)
            GlobalEventMaster.fireEvent(AccountProfileSelectEvent(value))
        }

    override fun createProfile(name: String, description: String?): AccountProfile {
        currentLoadingPath = name
        val profile = AccountProfile(description ?: "Default account profile")
        currentLoadingPath = null
        profiles[name] = profile

        return profile
    }
}
