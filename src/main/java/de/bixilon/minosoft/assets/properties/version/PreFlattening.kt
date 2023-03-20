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

package de.bixilon.minosoft.assets.properties.version

import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.minosoft.config.profile.profiles.resources.ResourcesProfile
import de.bixilon.minosoft.data.entities.entities.player.RemotePlayerEntity
import de.bixilon.minosoft.data.registries.dimension.Dimension
import de.bixilon.minosoft.data.registries.dimension.DimensionProperties
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.protocol.versions.Version
import de.bixilon.minosoft.util.KUtil.toResourceLocation

object PreFlattening {
    const val VERSION = "1.12.2"

    fun loadRegistry(profile: ResourcesProfile, version: Version, latch: CountUpAndDownLatch): Registries {
        val registries = Registries()
        registries.loadEntities(version)
        registries.loadDimensions()

        return registries
    }

    @Deprecated("test only")
    private fun Registries.loadEntities(version: Version) {
        entityType[RemotePlayerEntity] = EntityType(RemotePlayerEntity.identifier, null, 1.0f, 1.0f, false, false, mutableMapOf(), RemotePlayerEntity, null)
    }

    @Deprecated("test only")
    private fun Registries.loadDimensions() {
        dimension[0] = Dimension("minecraft:overworld".toResourceLocation(), DimensionProperties())
    }
}
