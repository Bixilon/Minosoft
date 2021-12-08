package de.bixilon.minosoft.config.profile.profiles.block

import com.google.common.collect.HashBiMap
import de.bixilon.minosoft.config.profile.GlobalProfileManager
import de.bixilon.minosoft.config.profile.ProfileManager
import de.bixilon.minosoft.modding.event.master.GlobalEventMaster
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.KUtil.unsafeCast
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid
import java.util.concurrent.locks.ReentrantLock

object BlockProfileManager : ProfileManager<BlockProfile> {
    override val namespace = "minosoft:block".toResourceLocation()
    override val latestVersion = 1
    override val saveLock = ReentrantLock()
    override val profileClass = BlockProfile::class.java
    override val icon = FontAwesomeSolid.CUBES


    override var currentLoadingPath: String? = null
    override val profiles: HashBiMap<String, BlockProfile> = HashBiMap.create()

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
