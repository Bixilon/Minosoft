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

package de.bixilon.minosoft.config.profile.profiles.eros

import de.bixilon.minosoft.config.profile.ProfileLock
import de.bixilon.minosoft.config.profile.ProfileType
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.config.profile.profiles.eros.general.GeneralC
import de.bixilon.minosoft.config.profile.profiles.eros.server.ServerC
import de.bixilon.minosoft.config.profile.profiles.eros.text.TextC
import de.bixilon.minosoft.config.profile.profiles.eros.theme.ThemeC
import de.bixilon.minosoft.config.profile.storage.ProfileStorage
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import org.kordamp.ikonli.Ikon
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid

/**
 * Profile for Eros
 */
class ErosProfile(
    override var storage: ProfileStorage? = null,
) : Profile {
    override val lock = ProfileLock()

    val general = GeneralC(this)
    val theme = ThemeC(this)
    val server = ServerC(this)
    val text = TextC(this)


    override fun toString(): String {
        return storage?.toString() ?: super.toString()
    }

    companion object : ProfileType<ErosProfile> {
        override val identifier = minosoft("eros")
        override val clazz = ErosProfile::class.java
        override val icon: Ikon get() = FontAwesomeSolid.WINDOW_RESTORE

        override fun create(storage: ProfileStorage?) = ErosProfile(storage)
    }
}
