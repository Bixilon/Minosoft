/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.config.profile.profiles.block

import com.fasterxml.jackson.databind.JavaType
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.collections.CollectionUtil.synchronizedBiMapOf
import de.bixilon.kutil.collections.map.bi.AbstractMutableBiMap
import de.bixilon.kutil.watcher.map.bi.BiMapDataWatcher.Companion.watchedBiMap
import de.bixilon.minosoft.config.profile.GlobalProfileManager
import de.bixilon.minosoft.config.profile.ProfileManager
import de.bixilon.minosoft.modding.event.master.GlobalEventMaster
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.json.Jackson
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid
import java.util.concurrent.locks.ReentrantLock

object BlockProfileManager : ProfileManager<BlockProfile> {
    override val namespace = "minosoft:block".toResourceLocation()
    override val latestVersion get() = 1
    override val saveLock = ReentrantLock()
    override val profileClass = BlockProfile::class.java
    override val jacksonProfileType: JavaType = Jackson.MAPPER.typeFactory.constructType(profileClass)
    override val icon = FontAwesomeSolid.CUBES


    override var currentLoadingPath: String? = null
    override val profiles: AbstractMutableBiMap<String, BlockProfile> by watchedBiMap(synchronizedBiMapOf())

    override var selected: BlockProfile = null.unsafeCast()
        set(value) {
            field = value
            GlobalProfileManager.selectProfile(this, value)
            GlobalEventMaster.fire(BlockProfileSelectEvent(value))
        }

    override fun createProfile(name: String, description: String?): BlockProfile {
        currentLoadingPath = name
        val profile = BlockProfile(description ?: "Default block profile")
        currentLoadingPath = null
        profiles[name] = profile

        return profile
    }
}
