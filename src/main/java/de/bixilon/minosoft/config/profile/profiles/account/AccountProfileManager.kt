package de.bixilon.minosoft.config.profile.profiles.account

import com.google.common.collect.HashBiMap
import de.bixilon.minosoft.config.profile.GlobalProfileManager
import de.bixilon.minosoft.config.profile.ProfileManager
import de.bixilon.minosoft.modding.event.master.GlobalEventMaster
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.KUtil.unsafeCast
import java.util.concurrent.locks.ReentrantLock

object AccountProfileManager : ProfileManager<AccountProfile> {
    override val namespace = "minosoft:account".toResourceLocation()
    override val latestVersion = 1
    override val saveLock = ReentrantLock()
    override val profileClass = AccountProfile::class.java


    override var currentLoadingPath: String? = null
    override val profiles: HashBiMap<String, AccountProfile> = HashBiMap.create()

    override var selected: AccountProfile = null.unsafeCast()
        set(value) {
            field = value
            GlobalProfileManager.selectProfile(this, value)
            GlobalEventMaster.fireEvent(AccountProfileSelectEvent(value))
        }

    override fun createDefaultProfile(name: String): AccountProfile {
        currentLoadingPath = name
        val profile = AccountProfile("Default account profile")
        currentLoadingPath = null
        profiles[name] = profile

        return profile
    }
}
