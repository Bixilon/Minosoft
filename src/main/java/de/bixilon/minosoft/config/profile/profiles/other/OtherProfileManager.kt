/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.config.profile.profiles.other

import com.fasterxml.jackson.databind.JavaType
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.collections.CollectionUtil.synchronizedBiMapOf
import de.bixilon.kutil.collections.map.bi.AbstractMutableBiMap
import de.bixilon.kutil.observer.map.bi.BiMapObserver.Companion.observedBiMap
import de.bixilon.minosoft.config.profile.GlobalProfileManager
import de.bixilon.minosoft.config.profile.ProfileManager
import de.bixilon.minosoft.modding.event.master.GlobalEventMaster
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.json.Jackson
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid
import java.util.concurrent.locks.ReentrantLock

object OtherProfileManager : ProfileManager<OtherProfile> {
    override val mapper = Jackson.MAPPER.copy()
    override val namespace = "minosoft:other".toResourceLocation()
    override val latestVersion get() = 3
    override val saveLock = ReentrantLock()
    override val profileClass = OtherProfile::class.java
    override val jacksonProfileType: JavaType = Jackson.MAPPER.typeFactory.constructType(profileClass)
    override val icon = FontAwesomeSolid.RANDOM

    override val profiles: AbstractMutableBiMap<String, OtherProfile> by observedBiMap(synchronizedBiMapOf())

    override var selected: OtherProfile = null.unsafeCast()
        set(value) {
            field = value
            GlobalProfileManager.selectProfile(this, value)
            GlobalEventMaster.fire(OtherProfileSelectEvent(value))
        }

    override fun createProfile(description: String?) = OtherProfile(description ?: "Default profile for various things")

    override fun migrate(from: Int, data: MutableMap<String, Any?>) {
        when (from) {
            1 -> OtherProfileMigration.migrate1(data)
            2 -> OtherProfileMigration.migrate2(data)
        }
    }
}
