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

package de.bixilon.minosoft.config.profile.profiles.resources

import de.bixilon.minosoft.config.profile.ProfileLock
import de.bixilon.minosoft.config.profile.ProfileType
import de.bixilon.minosoft.config.profile.delegate.primitive.BooleanDelegate
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.config.profile.profiles.resources.assets.AssetsC
import de.bixilon.minosoft.config.profile.profiles.resources.source.SourceC
import de.bixilon.minosoft.config.profile.storage.ProfileStorage
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import org.kordamp.ikonli.Ikon
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid

/**
 * Profile for resources
 */
class ResourcesProfile(
    override var storage: ProfileStorage? = null,
) : Profile {
    override val lock = ProfileLock()

    val source = SourceC(this)
    val assets = AssetsC(this)

    /**
     * If set, all downloaded assets will be checked on load.
     * Checks their size and sha1 hash.
     * Deletes and re-downloads/regenerates the asset on mismatch
     */
    var verify by BooleanDelegate(this, true)


    override fun toString(): String {
        return storage?.toString() ?: super.toString()
    }

    companion object : ProfileType<ResourcesProfile> {
        override val identifier = minosoft("resources")
        override val clazz = ResourcesProfile::class.java
        override val icon: Ikon get() = FontAwesomeSolid.DOWNLOAD

        override fun create(storage: ProfileStorage?) = ResourcesProfile(storage)
    }
}
