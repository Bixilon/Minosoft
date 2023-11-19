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

package de.bixilon.minosoft.config.profile.profiles.entity

import de.bixilon.minosoft.config.profile.ProfileLock
import de.bixilon.minosoft.config.profile.ProfileType
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.config.profile.profiles.entity.animal.AnimalC
import de.bixilon.minosoft.config.profile.profiles.entity.features.FeaturesC
import de.bixilon.minosoft.config.profile.profiles.entity.general.GeneralC
import de.bixilon.minosoft.config.profile.storage.ProfileStorage
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import org.kordamp.ikonli.Ikon
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid

/**
 * Profile for entity
 */
class EntityProfile(
    override val storage: ProfileStorage? = null,
) : Profile {
    override val lock = ProfileLock()

    val general = GeneralC(this)
    val features = FeaturesC(this)
    val animal = AnimalC(this)


    override fun toString(): String {
        return storage?.toString() ?: super.toString()
    }

    companion object : ProfileType<EntityProfile> {
        override val identifier = minosoft("entity")
        override val clazz = EntityProfile::class.java
        override val icon: Ikon get() = FontAwesomeSolid.SKULL

        override fun create(storage: ProfileStorage?) = EntityProfile(storage)
    }
}
