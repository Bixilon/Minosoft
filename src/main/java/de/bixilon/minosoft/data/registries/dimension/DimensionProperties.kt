package de.bixilon.minosoft.data.registries.dimension

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.util.VecUtil
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.nullCast
import de.bixilon.minosoft.util.KUtil.toBoolean
import de.bixilon.minosoft.util.KUtil.toInt
import de.bixilon.minosoft.util.KUtil.unsafeCast
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.get

data class DimensionProperties(
    val piglinSafe: Boolean = false,
    val natural: Boolean = true,
    val ambientLight: Float = 0.0f,
    val infiniBurn: ResourceLocation = ResourceLocation("infiniburn_overworld"),
    val respawnAnchorWorks: Boolean = false,
    val hasSkyLight: Boolean = true,
    val bedWorks: Boolean = true,
    val skyProperties: ResourceLocation = ResourceLocation("overworld"),
    val hasRaids: Boolean = true,
    val logicalHeight: Int = 256,
    val coordinateScale: Double = 0.0,
    val minY: Int = 0,
    val hasCeiling: Boolean = false,
    val ultraWarm: Boolean = false,
    val height: Int = 256,
    val supports3DBiomes: Boolean = true,
) {
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
    val sections = highestSection - lowestSection

    val lightLevels = FloatArray(16)

    init {
        val ambientLight = 0.0f // ToDo: 0.1 in nether

        for (i in lightLevels.indices) {
            val asFloat = i / 15.0f

            lightLevels[i] = VecUtil.lerp(ambientLight, asFloat / (4.0f - 3.0f * asFloat), 1.0f)
        }
    }


    companion object {
        fun deserialize(data: Map<String, Any>): DimensionProperties {
            return DimensionProperties(
                piglinSafe = data["piglin_safe"]?.toBoolean() ?: false,
                natural = data["natural"]?.toBoolean() ?: false,
                ambientLight = data["ambient_light"]?.unsafeCast<Float>() ?: 0.0f,
                infiniBurn = ResourceLocation(data["infiniburn"].nullCast<String>() ?: "infiniburn_overworld"),
                respawnAnchorWorks = data["respawn_anchor_works"]?.toBoolean() ?: false,
                hasSkyLight = data["has_skylight", "has_sky_light"]?.toBoolean() ?: false,
                bedWorks = data["bed_works"]?.toBoolean() ?: false,
                skyProperties = ResourceLocation(data["effects"].nullCast<String>() ?: "overworld"),
                hasRaids = data["has_raids"]?.toBoolean() ?: false,
                logicalHeight = data["logical_height"]?.toInt() ?: 256,
                coordinateScale = data["coordinate_scale"].nullCast() ?: 0.0,
                minY = data["min_y"]?.toInt() ?: 0,
                hasCeiling = data["has_ceiling"]?.toBoolean() ?: false,
                ultraWarm = data["ultrawarm"]?.toBoolean() ?: false,
                height = data["height"]?.toInt() ?: 256,
                supports3DBiomes = data["supports_3d_biomes"]?.toBoolean() ?: true,
            )
        }
    }
}
