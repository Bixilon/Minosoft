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

import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.nbt.tag.CompoundTag

class Dimension(
    fullIdentifier: String,
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
) : ModIdentifier(fullIdentifier) {
    val lowestSection = if (minY < 0) {
        (minY + 1) / ProtocolDefinition.SECTION_HEIGHT_Y - 1
    } else {
        minY / ProtocolDefinition.SECTION_HEIGHT_Y
    }

    companion object {
        fun deserialize(identifier: ModIdentifier, nbt: CompoundTag): Dimension {
            return Dimension(
                fullIdentifier = identifier.fullIdentifier,
                piglinSafe = nbt.getBoolean("piglin_safe"),
                natural = nbt.getBoolean("natural"),
                ambientLight = nbt.getFloatTag("ambient_light").value,
                infiniBurn = ModIdentifier(nbt.getStringTag("infiniburn").value),
                respawnAnchorWorks = nbt.getBoolean("respawn_anchor_works"),
                hasSkyLight = nbt.getBoolean("has_skylight"),
                bedWorks = nbt.getBoolean("bed_works"),
                effects = ModIdentifier(nbt.getStringTag("infiniburn").value),
                hasRaids = nbt.getBoolean("has_raids"),
                logicalHeight = nbt.getNumberTag("logical_height").asInt,
                coordinateScale = nbt.getDoubleTag("coordinate_scale").value,
                minY = nbt.getNumberTag("min_y").asInt,
                hasCeiling = nbt.getBoolean("has_ceiling"),
                ultrawarm = nbt.getBoolean("ultrawarm"),
                height = nbt.getNumberTag("height").asInt,
            )
        }
    }
}
