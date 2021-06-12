/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
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

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.registry.RegistryItem
import de.bixilon.minosoft.data.registries.registry.ResourceLocationDeserializer
import de.bixilon.minosoft.data.registries.versions.Registries
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.data.text.RGBColor.Companion.asColor
import de.bixilon.minosoft.data.text.RGBColor.Companion.asRGBColor
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.TintColorCalculator
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.MMath
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
    val foliageColorOverride: RGBColor?,
    val grassColorOverride: RGBColor?,
    val descriptionId: String?,
    val grassColorModifier: GrassColorModifiers = GrassColorModifiers.NONE,
) : RegistryItem {
    val temperatureColorMapCoordinate = getColorMapCoordinate(temperature)
    val downfallColorMapCoordinate = getColorMapCoordinate(downfall * temperature)

    private fun getColorMapCoordinate(value: Float): Int {
        return ((1.0 - MMath.clamp(value, 0.0f, 1.0f)) * RenderConstants.COLORMAP_SIZE).toInt()
    }

    fun getClampedTemperature(height: Int): Int {
        return getColorMapCoordinate(MMath.clamp(temperature + (MMath.clamp(height - ProtocolDefinition.SEA_LEVEL_HEIGHT, 1, Int.MAX_VALUE) * ProtocolDefinition.HEIGHT_SEA_LEVEL_MODIFIER), 0.0f, 1.0f))
    }

    override fun toString(): String {
        return resourceLocation.full
    }

    companion object : ResourceLocationDeserializer<Biome> {
        private val TODO_SWAMP_COLOR = "#6A7039".asColor()
        override fun deserialize(registries: Registries?, resourceLocation: ResourceLocation, data: JsonObject): Biome {
            check(registries != null) { "Registries is null!" }
            return Biome(
                resourceLocation = resourceLocation,
                depth = data["depth"]?.asFloat ?: 0.0f,
                scale = data["scale"]?.asFloat ?: 0.0f,
                temperature = data["temperature"]?.asFloat ?: 0.0f,
                downfall = data["downfall"]?.asFloat ?: 0.0f,
                waterColor = TintColorCalculator.getJsonColor(data["water_color"]?.asInt ?: 0),
                waterFogColor = TintColorCalculator.getJsonColor(data["water_fog_color"]?.asInt ?: 0),
                category = registries.biomeCategoryRegistry[data["category"]?.asInt ?: -1] ?: DEFAULT_CATEGORY,
                precipitation = registries.biomePrecipitationRegistry[data["precipitation"]?.asInt ?: -1] ?: DEFAULT_PRECIPITATION,
                skyColor = data["sky_color"]?.asInt?.asRGBColor() ?: RenderConstants.GRASS_FAILOVER_COLOR,
                foliageColorOverride = TintColorCalculator.getJsonColor(data["foliage_color_override"]?.asInt ?: 0),
                grassColorOverride = TintColorCalculator.getJsonColor(data["grass_color_override"]?.asInt ?: 0),
                descriptionId = data["water_fog_color"]?.asString,
                grassColorModifier = data["grass_color_modifier"]?.asString?.uppercase(Locale.getDefault())?.let { GrassColorModifiers.valueOf(it) } ?: when (resourceLocation) {
                    ResourceLocation("minecraft:swamp"), ResourceLocation("minecraft:swamp_hills") -> GrassColorModifiers.SWAMP
                    ResourceLocation("minecraft:dark_forest"), ResourceLocation("minecraft:dark_forest_hills") -> GrassColorModifiers.DARK_FOREST
                    else -> GrassColorModifiers.NONE
                }
            )
        }

        private val DEFAULT_PRECIPITATION = BiomePrecipitation("NONE")
        private val DEFAULT_CATEGORY = BiomeCategory("NONE")

    }

    enum class GrassColorModifiers(val modifier: (color: RGBColor) -> RGBColor) {
        NONE({ color: RGBColor -> color }),
        DARK_FOREST({ color: RGBColor -> color }), // ToDo: This rgb 2634762 should be added to this?
        SWAMP({
            // ToDo: Minecraft uses PerlinSimplexNoise here
            TODO_SWAMP_COLOR
        }),
    }
}
