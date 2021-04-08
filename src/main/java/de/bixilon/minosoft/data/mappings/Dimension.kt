/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.mappings

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.mappings.registry.RegistryItem
import de.bixilon.minosoft.data.mappings.registry.ResourceLocationDeserializer
import de.bixilon.minosoft.data.mappings.versions.VersionMapping
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.nbt.tag.CompoundTag

data class Dimension(
    override val resourceLocation: ResourceLocation,
    val piglinSafe: Boolean = false,
    val natural: Boolean = true,
    val ambientLight: Float = 0.0f,
    val infiniBurn: ResourceLocation = ResourceLocation("infiniburn_overworld"),
    val respawnAnchorWorks: Boolean = false,
    val hasSkyLight: Boolean = true,
    val bedWorks: Boolean = true,
    val effects: ResourceLocation = ResourceLocation("overworld"),
    val hasRaids: Boolean = true,
    val logicalHeight: Int = 256,
    val coordinateScale: Double = 0.0,
    val minY: Int = 0,
    val hasCeiling: Boolean = false,
    val ultraWarm: Boolean = false,
    val height: Int = 256,
    val supports3DBiomes: Boolean = true,
) : RegistryItem {
    val lowestSection = if (minY < 0) {
        (minY + 1) / ProtocolDefinition.SECTION_HEIGHT_Y - 1
    } else {
        minY / ProtocolDefinition.SECTION_HEIGHT_Y
    }
    val highestSection = if (height < 0) {
        (height + 1) / ProtocolDefinition.SECTION_HEIGHT_Y - 1
    } else {
        height / ProtocolDefinition.SECTION_HEIGHT_Y
    }

    override fun toString(): String {
        return resourceLocation.full
    }

    companion object : ResourceLocationDeserializer<Dimension> {
        fun deserialize(resourceLocation: ResourceLocation, nbt: CompoundTag): Dimension {
            return Dimension(
                resourceLocation = resourceLocation,
                piglinSafe = nbt.getBoolean("piglin_safe") == true,
                natural = nbt.getBoolean("natural") == true,
                ambientLight = nbt.getFloatTag("ambient_light")?.value ?: 0.0f,
                infiniBurn = ResourceLocation(nbt.getStringTag("infiniburn")?.value ?: "infiniburn_overworld"),
                respawnAnchorWorks = nbt.getBoolean("respawn_anchor_works") == true,
                hasSkyLight = nbt.getBoolean("has_skylight") == true,
                bedWorks = nbt.getBoolean("bed_works") == true,
                effects = ResourceLocation(nbt.getStringTag("effects")?.value ?: "overworld"),
                hasRaids = nbt.getBoolean("has_raids") == true,
                logicalHeight = nbt.getNumberTag("logical_height")?.asInt ?: 256,
                coordinateScale = nbt.getDoubleTag("coordinate_scale")?.value ?: 0.0,
                minY = nbt.getNumberTag("min_y")?.asInt ?: 0,
                hasCeiling = nbt.getBoolean("has_ceiling") == true,
                ultraWarm = nbt.getBoolean("ultrawarm") == true,
                height = nbt.getNumberTag("height")?.asInt ?: 256,
            )
        }

        override fun deserialize(mappings: VersionMapping?, resourceLocation: ResourceLocation, data: JsonObject): Dimension {
            return Dimension(
                resourceLocation = resourceLocation,
                piglinSafe = data.get("piglin_safe")?.asBoolean == true,
                natural = data.get("natural")?.asBoolean == true,
                ambientLight = data.get("ambient_light")?.asFloat ?: 0.0f,
                infiniBurn = ResourceLocation(data.get("ambient_light")?.asString ?: "infiniburn_overworld"),
                respawnAnchorWorks = data.get("respawn_anchor_works")?.asBoolean == true,
                hasSkyLight = data.get("has_sky_light")?.asBoolean == true,
                bedWorks = data.get("bed_works")?.asBoolean == true,
                effects = ResourceLocation(data.get("effects")?.asString ?: "overworld"),
                hasRaids = data.get("has_raids")?.asBoolean == true,
                logicalHeight = data.get("logical_height")?.asInt ?: 256,
                coordinateScale = data.get("coordinate_scale")?.asDouble ?: 0.0,
                minY = data.get("min_y")?.asInt ?: 0,
                hasCeiling = data.get("has_ceiling")?.asBoolean == true,
                ultraWarm = data.get("ultrawarm")?.asBoolean == true,
                height = data.get("height")?.asInt ?: 256,
                supports3DBiomes = data.get("supports_3d_biomes")?.asBoolean ?: false,
            )
        }
    }
}
