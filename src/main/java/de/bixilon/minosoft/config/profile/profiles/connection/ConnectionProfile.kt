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

package de.bixilon.minosoft.config.profile.profiles.connection

import de.bixilon.minosoft.config.profile.ProfileLock
import de.bixilon.minosoft.config.profile.ProfileType
import de.bixilon.minosoft.config.profile.delegate.primitive.BooleanDelegate
import de.bixilon.minosoft.config.profile.delegate.types.EnumDelegate
import de.bixilon.minosoft.config.profile.delegate.types.LanguageDelegate
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.config.profile.profiles.connection.signature.SignatureC
import de.bixilon.minosoft.config.profile.profiles.connection.skin.SkinC
import de.bixilon.minosoft.config.profile.storage.ProfileStorage
import de.bixilon.minosoft.data.entities.entities.player.Arms
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import org.kordamp.ikonli.Ikon
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid

/**
 * Profile for connection
 */
class ConnectionProfile(
    override val storage: ProfileStorage? = null,
) : Profile {
    override val lock = ProfileLock()

    /**
     * Language for language files.
     * Will be sent to the server
     * If unset (null), uses eros language
     */
    var language: String? by LanguageDelegate(this, null)

    /**
     * If false, the server should not list us the ping player list
     * Will be sent to the server
     */
    var playerListing by BooleanDelegate(this, true)

    /**
     * Main arm to use
     */
    var mainArm by EnumDelegate(this, Arms.RIGHT, Arms)

    val skin = SkinC(this)
    val signature = SignatureC(this)

    var autoRespawn by BooleanDelegate(this, false)

    /**
     * If set, the client will respond with "vanilla" as brand and not "minosoft"
     */
    var fakeBrand by BooleanDelegate(this, false)


    override fun toString(): String {
        return storage?.toString() ?: super.toString()
    }

    companion object : ProfileType<ConnectionProfile> {
        override val identifier = minosoft("connection")
        override val clazz = ConnectionProfile::class.java
        override val icon: Ikon get() = FontAwesomeSolid.NETWORK_WIRED

        override fun create(storage: ProfileStorage?) = ConnectionProfile(storage)
    }
}
