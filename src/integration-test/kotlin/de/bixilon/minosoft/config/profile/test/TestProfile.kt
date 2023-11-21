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

package de.bixilon.minosoft.config.profile.test

import de.bixilon.kutil.concurrent.lock.Lock
import de.bixilon.minosoft.config.profile.ProfileLock
import de.bixilon.minosoft.config.profile.ProfileType
import de.bixilon.minosoft.config.profile.delegate.primitive.IntDelegate
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.config.profile.storage.ProfileStorage
import de.bixilon.minosoft.config.profile.test.config.ConfigC
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft

class TestProfile(
    override var storage: ProfileStorage? = null,
    override val lock: Lock = ProfileLock(),
) : Profile {
    val config = ConfigC(this)

    var key by IntDelegate(this, 1)


    companion object : ProfileType<TestProfile> {
        override val identifier = minosoft("test")
        override val clazz = TestProfile::class.java

        override fun create(storage: ProfileStorage?) = TestProfile(storage)
    }
}
