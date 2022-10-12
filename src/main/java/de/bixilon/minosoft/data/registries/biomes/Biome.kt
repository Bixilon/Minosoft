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
package de.bixilon.minosoft.data.registries.biomes

import de.bixilon.kotlinglm.func.common.clamp
import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.primitive.FloatUtil.toFloat
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.registries.registries.registry.RegistryItem
import de.bixilon.minosoft.data.registries.registries.registry.codec.ResourceLocationCodec
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.data.text.formatting.color.RGBColor.Companion.asRGBColor
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.tint.TintManager
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import java.util.*

data class Biome(
    override val resourceLocation: ResourceLocation,
    val depth: Float,
    val scale: Float,
    val temperature: Float,
    val downfall: Float,
    val waterColor: RGBColor?,
    val waterFogColor: RGBColor?,
    val category: BiomeCategory,
    val precipitation: BiomePrecipitation,
    val skyColor: RGBColor,
    val descriptionId: String?,
    val grassColorModifier: GrassColorModifiers = GrassColorModifiers.NONE,
) : RegistryItem() {
    val temperatureColorMapCoordinate = getColorMapCoordinate(temperature)
    val downfallColorMapCoordinate = getColorMapCoordinate(downfall * temperature)
    val colorMapPixelIndex = downfallColorMapCoordinate shl 8 or temperatureColorMapCoordinate


    fun getClampedTemperature(height: Int): Int {
        return getColorMapCoordinate((temperature + ((height - ProtocolDefinition.SEA_LEVEL_HEIGHT).clamp(1, Int.MAX_VALUE) * ProtocolDefinition.HEIGHT_SEA_LEVEL_MODIFIER)).clamp(0.0f, 1.0f))
    }

    override fun toString(): String {
        return resourceLocation.full
    }

    companion object : ResourceLocationCodec<Biome> {

        private fun getColorMapCoordinate(value: Float): Int {
            return ((1.0 - value.clamp(0.0f, 1.0f)) * RenderConstants.COLORMAP_SIZE).toInt()
        }

        override fun deserialize(registries: Registries?, resourceLocation: ResourceLocation, data: Map<String, Any>): Biome {
            check(registries != null) { "Registries is null!" }
            return Biome(
                resourceLocation = resourceLocation,
                depth = data["depth"]?.toFloat() ?: 0.0f,
                scale = data["scale"]?.toFloat() ?: 0.0f,
                temperature = data["temperature"]?.toFloat() ?: 0.0f,
                downfall = data["downfall"]?.toFloat() ?: 0.0f,
                waterColor = TintManager.getJsonColor(data["water_color"]?.toInt() ?: 0),
                waterFogColor = TintManager.getJsonColor(data["water_fog_color"]?.toInt() ?: 0),
                category = registries.biomeCategoryRegistry[data["category"]?.toInt() ?: -1] ?: DEFAULT_CATEGORY,
                precipitation = registries.biomePrecipitationRegistry[data["precipitation"]?.toInt() ?: -1] ?: DEFAULT_PRECIPITATION,
                skyColor = data["sky_color"]?.toInt()?.asRGBColor() ?: RenderConstants.GRASS_FAILOVER_COLOR,
                descriptionId = data["water_fog_color"].nullCast(),
                grassColorModifier = data["grass_color_modifier"].nullCast<String>()?.uppercase(Locale.getDefault())?.let { GrassColorModifiers.valueOf(it) } ?: when (resourceLocation) {
                    ResourceLocation("minecraft:swamp"), ResourceLocation("minecraft:swamp_hills") -> GrassColorModifiers.SWAMP
                    ResourceLocation("minecraft:dark_forest"), ResourceLocation("minecraft:dark_forest_hills") -> GrassColorModifiers.DARK_FOREST
                    else -> GrassColorModifiers.NONE
                }
            )
        }

        private val DEFAULT_PRECIPITATION = BiomePrecipitation("NONE")
        private val DEFAULT_CATEGORY = BiomeCategory("NONE")

    }

    enum class GrassColorModifiers {
        NONE,
        DARK_FOREST,
        SWAMP,
        ;
    }
}
