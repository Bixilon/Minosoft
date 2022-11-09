/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.registries.dimension

import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.math.interpolation.FloatInterpolation.interpolateLinear
import de.bixilon.kutil.primitive.BooleanUtil.toBoolean
import de.bixilon.kutil.primitive.FloatUtil.toFloat
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.registries.dimension.sky.DefaultSkyProperties
import de.bixilon.minosoft.data.registries.dimension.sky.OverworldSkyProperties
import de.bixilon.minosoft.data.registries.dimension.sky.SkyProperties
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.get

data class DimensionProperties(
    //   val piglinSafe: Boolean = false,
    //   val natural: Boolean = true,
    val ambientLight: Float = 0.0f,
    //   val respawnAnchorWorks: Boolean = false,
    val hasSkyLight: Boolean = true,
    //   val bedWorks: Boolean = true,
    val sky: SkyProperties = OverworldSkyProperties,
    //   val hasRaids: Boolean = true,
    val logicalHeight: Int = DEFAULT_HEIGHT,
    //   val coordinateScale: Double = 0.0,
    val minY: Int = 0,
    //   val hasCeiling: Boolean = false,
    val ultraWarm: Boolean = false,
    val dataHeight: Int = DEFAULT_HEIGHT,
    val supports3DBiomes: Boolean = true,
) {
    val maxY = dataHeight + minY - 1
    val sections = dataHeight / ProtocolDefinition.SECTION_HEIGHT_Y
    val minSection = minY shr 4
    val maxSection = minSection + sections

    val brightness = FloatArray(16)

    init {
        check(maxSection > minSection) { "Upper section can not be lower that the lower section ($minSection > $maxSection)" }
        check(minSection in ProtocolDefinition.CHUNK_MIN_SECTION..ProtocolDefinition.CHUNK_MAX_SECTION) { "Minimum section out of bounds: $minSection" }
        check(maxSection in ProtocolDefinition.CHUNK_MIN_SECTION..ProtocolDefinition.CHUNK_MAX_SECTION) { "Maximum section out of bounds: $minSection" }

        for (level in brightness.indices) {
            val fraction = level / ProtocolDefinition.MAX_LIGHT_LEVEL.toFloat()
            brightness[level] = interpolateLinear(ambientLight, fraction / (4.0f - 3.0f * fraction), 1.0f)
        }
    }


    companion object {
        const val DEFAULT_HEIGHT = 256
        const val DEFAULT_MAX_Y = DEFAULT_HEIGHT - 1

        fun deserialize(data: Map<String, Any>): DimensionProperties {
            return DimensionProperties(
                //piglinSafe = data["piglin_safe"]?.toBoolean() ?: false,
                //natural = data["natural"]?.toBoolean() ?: false,
                ambientLight = data["ambient_light"]?.toFloat() ?: 0.0f,
                //infiniBurn = ResourceLocation(data["infiniburn"].nullCast<String>() ?: "infiniburn_overworld"),
                //respawnAnchorWorks = data["respawn_anchor_works"]?.toBoolean() ?: false,
                hasSkyLight = data["has_skylight", "has_sky_light"]?.toBoolean() ?: false,
                //bedWorks = data["bed_works"]?.toBoolean() ?: false,
                sky = data["effects"].nullCast<String>()?.let { DefaultSkyProperties[it.toResourceLocation()] } ?: OverworldSkyProperties,
                //hasRaids = data["has_raids"]?.toBoolean() ?: false,
                logicalHeight = data["logical_height"]?.toInt() ?: DEFAULT_MAX_Y,
                //coordinateScale = data["coordinate_scale"].nullCast() ?: 0.0,
                minY = data["min_y"]?.toInt() ?: 0,
                //hasCeiling = data["has_ceiling"]?.toBoolean() ?: false,
                ultraWarm = data["ultrawarm"]?.toBoolean() ?: false,
                dataHeight = data["height"]?.toInt() ?: DEFAULT_MAX_Y,
                supports3DBiomes = data["supports_3d_biomes"]?.toBoolean() ?: true,
            )
        }
    }
}
