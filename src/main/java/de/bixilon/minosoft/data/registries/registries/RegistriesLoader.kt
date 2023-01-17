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

import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.minosoft.config.profile.profiles.resources.ResourcesProfile
import de.bixilon.minosoft.data.registries.DefaultRegistries
import de.bixilon.minosoft.protocol.versions.Version

object RegistriesLoader {

    fun load(profile: ResourcesProfile, version: Version, latch: CountUpAndDownLatch): Registries {
        if (!version.flattened) {
            // ToDo: Pre flattening support
            throw PreFlatteningLoadingError()
        }
        val registries = PixLyzerUtil.loadRegistry(version, profile, latch)

        registries.setDefaultParents(version)


        return registries
    }

    private fun Registries.setDefaultParents(version: Version) {
        equipmentSlot.parent = DefaultRegistries.EQUIPMENT_SLOTS_REGISTRY.forVersion(version)
        handEquipmentSlot.parent = DefaultRegistries.HAND_EQUIPMENT_SLOTS_REGISTRY.forVersion(version)
        armorEquipmentSlot.parent = DefaultRegistries.ARMOR_EQUIPMENT_SLOTS_REGISTRY.forVersion(version)
        armorStandEquipmentSlot.parent = DefaultRegistries.ARMOR_STAND_EQUIPMENT_SLOTS_REGISTRY.forVersion(version)
        entityDataTypes.parent = DefaultRegistries.ENTITY_DATA_TYPES_REGISTRY.forVersion(version)
        titleActions.parent = DefaultRegistries.TITLE_ACTIONS_REGISTRY.forVersion(version)
        entityAnimation.parent = DefaultRegistries.ENTITY_ANIMATION_REGISTRY.forVersion(version)
        entityActions.parent = DefaultRegistries.ENTITY_ACTIONS_REGISTRY.forVersion(version)
        messageType.parent = DefaultRegistries.MESSAGE_TYPES_REGISTRY.forVersion(version)

        containerType.parent = DefaultRegistries.CONTAINER_TYPE_REGISTRY.forVersion(version)
        gameEvent.parent = DefaultRegistries.GAME_EVENT_REGISTRY.forVersion(version)
        worldEvent.parent = DefaultRegistries.WORLD_EVENT_REGISTRY.forVersion(version)
        blockDataType.parent = DefaultRegistries.BLOCK_DATA_TYPE_REGISTRY.forVersion(version)
        catVariants.parent = DefaultRegistries.CAT_VARIANT_REGISTRY.forVersion(version)
    }
}
