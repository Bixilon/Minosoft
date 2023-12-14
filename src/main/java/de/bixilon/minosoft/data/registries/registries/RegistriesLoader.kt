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

package de.bixilon.minosoft.data.registries.registries

import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.minosoft.assets.properties.version.PreFlattening
import de.bixilon.minosoft.config.profile.profiles.resources.ResourcesProfile
import de.bixilon.minosoft.data.registries.fallback.FallbackRegistries
import de.bixilon.minosoft.protocol.versions.Version

object RegistriesLoader {

    fun load(profile: ResourcesProfile, version: Version, latch: AbstractLatch): Registries {
        val registries = if (!version.flattened) PreFlattening.loadRegistry(profile, version, latch) else PixLyzerUtil.loadRegistry(version, profile, latch) // TODO: prioritize pixlyzer and if it fails load meta

        registries.setDefaultParents(version)

        return registries
    }

    private fun Registries.setDefaultParents(version: Version) {
        equipmentSlot.parent = FallbackRegistries.EQUIPMENT_SLOTS.forVersion(version)
        entityDataTypes.parent = FallbackRegistries.ENTITY_DATA_TYPES.forVersion(version)
        titleActions.parent = FallbackRegistries.TITLE_ACTIONS.forVersion(version)
        entityAnimation.parent = FallbackRegistries.ENTITY_ANIMATION.forVersion(version)
        entityActions.parent = FallbackRegistries.ENTITY_ACTIONS.forVersion(version)
        entityObjectType.parent = FallbackRegistries.ENTITY_OBJECT.forVersion(version)
        messageType.parent = FallbackRegistries.MESSAGE_TYPES.forVersion(version)

        containerType.parent = FallbackRegistries.CONTAINER_TYPE.forVersion(version)
        gameEvent.parent = FallbackRegistries.GAME_EVENT.forVersion(version)
        worldEvent.parent = FallbackRegistries.WORLD_EVENT.forVersion(version)
        blockDataType.parent = FallbackRegistries.BLOCK_DATA_TYPE.forVersion(version)
        vibrationSource.parent = FallbackRegistries.VIBRATION_SOURCE.forVersion(version)
        catVariants.parent = FallbackRegistries.CAT_VARIANT.forVersion(version)
    }
}
