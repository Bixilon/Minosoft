/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.registries

import de.bixilon.minosoft.data.registries.registry.RegistryItem
import de.bixilon.minosoft.data.registries.registry.ResourceLocationDeserializer
import de.bixilon.minosoft.data.registries.versions.Registries
import de.bixilon.minosoft.gui.rendering.util.VecUtil.lerp
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.nullCast
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.booleanCast
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.get

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
) : RegistryItem() {
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
    val lightLevels = FloatArray(16)

    init {
        val ambientLight = 0.0f // ToDo: 0.1 in nether

        for (i in lightLevels.indices) {
            val asFloat = i / 15.0f

            lightLevels[i] = lerp(ambientLight, asFloat / (4.0f - 3.0f * asFloat), 1.0f)
        }
    }


    override fun toString(): String {
        return resourceLocation.full
    }

    companion object : ResourceLocationDeserializer<Dimension> {
        override fun deserialize(registries: Registries?, resourceLocation: ResourceLocation, data: Map<String, Any>): Dimension {
            return Dimension(
                resourceLocation = resourceLocation,
                piglinSafe = data["piglin_safe"]?.booleanCast() ?: false,
                natural = data["natural"]?.booleanCast() ?: false,
                ambientLight = data["ambient_light"]?.nullCast<Float>() ?: 0.0f,
                infiniBurn = ResourceLocation(data["infiniburn"]?.nullCast<String>() ?: "infiniburn_overworld"),
                respawnAnchorWorks = data["respawn_anchor_works"]?.booleanCast() ?: false,
                hasSkyLight = data["has_skylight", "has_sky_light"]?.booleanCast() ?: false,
                bedWorks = data["bed_works"]?.booleanCast() ?: false,
                effects = ResourceLocation(data["effects"]?.nullCast<String>() ?: "overworld"),
                hasRaids = data["has_raids"]?.booleanCast() ?: false,
                logicalHeight = data["logical_height"]?.nullCast<Int>() ?: 256,
                coordinateScale = data["coordinate_scale"]?.nullCast() ?: 0.0,
                minY = data["min_y"]?.nullCast<Number>()?.toInt() ?: 0,
                hasCeiling = data["has_ceiling"]?.booleanCast() ?: false,
                ultraWarm = data["ultrawarm"]?.booleanCast() ?: false,
                height = data["height"]?.nullCast<Number>()?.toInt() ?: 256,
                supports3DBiomes = data["supports_3d_biomes"]?.booleanCast() ?: false,
            )
        }
    }
}
