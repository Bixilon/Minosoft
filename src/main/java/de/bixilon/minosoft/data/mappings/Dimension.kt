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
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.nbt.tag.CompoundTag

class Dimension(
    val identifier: ModIdentifier,
    val piglinSafe: Boolean = false,
    val natural: Boolean = true,
    val ambientLight: Float = 0.0f,
    val infiniBurn: ModIdentifier = ModIdentifier("infiniburn_overworld"),
    val respawnAnchorWorks: Boolean = false,
    val hasSkyLight: Boolean = true,
    val bedWorks: Boolean = true,
    val effects: ModIdentifier = ModIdentifier("overworld"),
    val hasRaids: Boolean = true,
    val logicalHeight: Int = 256,
    val coordinateScale: Double = 0.0,
    val minY: Int = 0,
    val hasCeiling: Boolean = false,
    val ultrawarm: Boolean = false,
    val height: Int = 256,
) {
    val lowestSection = if (minY < 0) {
        (minY + 1) / ProtocolDefinition.SECTION_HEIGHT_Y - 1
    } else {
        minY / ProtocolDefinition.SECTION_HEIGHT_Y
    }

    override fun toString(): String {
        return identifier.toString()
    }

    companion object {
        fun deserialize(identifier: ModIdentifier, nbt: CompoundTag): Dimension {
            return Dimension(
                identifier = identifier,
                piglinSafe = nbt.getBoolean("piglin_safe"),
                natural = nbt.getBoolean("natural"),
                ambientLight = nbt.getFloatTag("ambient_light").value,
                infiniBurn = ModIdentifier(nbt.getStringTag("infiniburn").value),
                respawnAnchorWorks = nbt.getBoolean("respawn_anchor_works"),
                hasSkyLight = nbt.getBoolean("has_skylight"),
                bedWorks = nbt.getBoolean("bed_works"),
                effects = ModIdentifier(nbt.getStringTag("effects").value),
                hasRaids = nbt.getBoolean("has_raids"),
                logicalHeight = nbt.getNumberTag("logical_height").asInt,
                coordinateScale = nbt.getDoubleTag("coordinate_scale").value,
                minY = nbt.getNumberTag("min_y").asInt,
                hasCeiling = nbt.getBoolean("has_ceiling"),
                ultrawarm = nbt.getBoolean("ultrawarm"),
                height = nbt.getNumberTag("height").asInt,
            )
        }

        fun deserialize(identifier: ModIdentifier, json: JsonObject): Dimension {
            return Dimension(
                identifier = identifier,
                piglinSafe = json.get("piglin_safe")?.asBoolean == true,
                natural = json.get("natural")?.asBoolean == true,
                ambientLight = json.get("ambient_light")?.asFloat ?: 0f,
                infiniBurn = ModIdentifier(json.get("ambient_light")?.asString ?: "infiniburn_overworld"),
                respawnAnchorWorks = json.get("respawn_anchor_works")?.asBoolean == true,
                hasSkyLight = json.get("has_skylight")?.asBoolean == true,
                bedWorks = json.get("bed_works")?.asBoolean == true,
                effects = ModIdentifier(json.get("effects")?.asString ?: "overworld"),
                hasRaids = json.get("has_raids")?.asBoolean == true,
                logicalHeight = json.get("logical_height")?.asInt ?: 256,
                coordinateScale = json.get("coordinate_scale")?.asDouble ?: 0.0,
                minY = json.get("min_y")?.asInt ?: 0,
                hasCeiling = json.get("has_ceiling")?.asBoolean == true,
                ultrawarm = json.get("ultrawarm")?.asBoolean == true,
                height = json.get("height")?.asInt ?: 256,
            )
        }
    }
}
