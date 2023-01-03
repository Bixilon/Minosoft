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

import de.bixilon.kutil.json.JsonUtil.toJsonObject
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.kutil.string.StringUtil.formatPlaceholder
import de.bixilon.minosoft.assets.properties.version.AssetsVersionProperties
import de.bixilon.minosoft.assets.util.FileAssetsUtil
import de.bixilon.minosoft.assets.util.FileUtil
import de.bixilon.minosoft.assets.util.FileUtil.readMBFMap
import de.bixilon.minosoft.config.profile.profiles.resources.ResourcesProfile
import de.bixilon.minosoft.data.registries.DefaultRegistries
import de.bixilon.minosoft.protocol.versions.Version
import java.io.ByteArrayInputStream
import java.io.File

object RegistriesLoader {

    fun load(profile: ResourcesProfile, version: Version, latch: CountUpAndDownLatch): Registries {
        val registries = Registries()
        if (!version.flattened) {
            // ToDo: Pre flattening support
            throw PreFlatteningLoadingError()
        }
        val pixlyzerHash = AssetsVersionProperties[version]?.pixlyzerHash ?: throw IllegalStateException("$version has no pixlyzer data available!")

        val pixlyzerData = getPixlyzerData(profile.source.pixlyzer, pixlyzerHash)

        registries.load(version, pixlyzerData, latch)

        registries.equipmentSlot.parent = DefaultRegistries.EQUIPMENT_SLOTS_REGISTRY.forVersion(version)
        registries.handEquipmentSlot.parent = DefaultRegistries.HAND_EQUIPMENT_SLOTS_REGISTRY.forVersion(version)
        registries.armorEquipmentSlot.parent = DefaultRegistries.ARMOR_EQUIPMENT_SLOTS_REGISTRY.forVersion(version)
        registries.armorStandEquipmentSlot.parent = DefaultRegistries.ARMOR_STAND_EQUIPMENT_SLOTS_REGISTRY.forVersion(version)
        registries.entityDataTypes.parent = DefaultRegistries.ENTITY_DATA_TYPES_REGISTRY.forVersion(version)
        registries.titleActions.parent = DefaultRegistries.TITLE_ACTIONS_REGISTRY.forVersion(version)
        registries.entityAnimation.parent = DefaultRegistries.ENTITY_ANIMATION_REGISTRY.forVersion(version)
        registries.entityActions.parent = DefaultRegistries.ENTITY_ACTIONS_REGISTRY.forVersion(version)
        registries.messageType.parent = DefaultRegistries.MESSAGE_TYPES_REGISTRY.forVersion(version)

        registries.containerType.parent = DefaultRegistries.CONTAINER_TYPE_REGISTRY.forVersion(version)
        registries.gameEvent.parent = DefaultRegistries.GAME_EVENT_REGISTRY.forVersion(version)
        registries.worldEvent.parent = DefaultRegistries.WORLD_EVENT_REGISTRY.forVersion(version)
        registries.blockDataType.parent = DefaultRegistries.BLOCK_DATA_TYPE_REGISTRY.forVersion(version)
        registries.catVariants.parent = DefaultRegistries.CAT_VARIANT_REGISTRY.forVersion(version)

        return registries
    }

    private fun getPixlyzerData(url: String, hash: String): Map<String, Any> {
        val path = FileAssetsUtil.getPath(hash)
        val file = File(path)
        if (file.exists()) {
            // ToDo: Verify
            return FileUtil.readFile(file, false).readMBFMap().toJsonObject() ?: throw IllegalStateException("Could not read pixlyzer data!")
        }

        val savedHash = FileAssetsUtil.downloadAndGetAsset(url.formatPlaceholder(
            "hashPrefix" to hash.substring(0, 2),
            "fullHash" to hash,
        ), false, hashType = FileAssetsUtil.HashTypes.SHA1)
        if (savedHash.first != hash) {
            throw IllegalStateException("Data mismatch, expected $hash, got ${savedHash.first}")
        }

        return ByteArrayInputStream(savedHash.second).readMBFMap().toJsonObject() ?: throw IllegalStateException("Invalid pixlyzer data!")
    }
}
