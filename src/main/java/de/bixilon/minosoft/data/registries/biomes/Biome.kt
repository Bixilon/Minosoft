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
package de.bixilon.minosoft.data.registries.biomes

import de.bixilon.kotlinglm.func.common.clamp
import de.bixilon.kutil.json.JsonUtil.toJsonObject
import de.bixilon.kutil.primitive.FloatUtil.toFloat
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.registries.registries.registry.RegistryItem
import de.bixilon.minosoft.data.registries.registries.registry.codec.ResourceLocationCodec
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.tint.TintManager.Companion.jsonTint
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition

data class Biome(
    override val identifier: ResourceLocation,
    val temperature: Float,
    val downfall: Float,
    val skyColor: RGBColor?,
    val fogColor: RGBColor? = null,
    val waterColor: RGBColor?,
    val waterFogColor: RGBColor?,
    val precipitation: BiomePrecipitation,
) : RegistryItem() {
    val grassColorModifier = GrassColorModifiers.BIOME_MAP[identifier] ?: GrassColorModifiers.NONE
    val temperatureColorMapCoordinate = getColorMapCoordinate(temperature)
    val downfallColorMapCoordinate = getColorMapCoordinate(downfall * temperature)
    val colorMapPixelIndex = downfallColorMapCoordinate shl 8 or temperatureColorMapCoordinate


    fun getClampedTemperature(height: Int): Int {
        return getColorMapCoordinate((temperature + ((height - ProtocolDefinition.SEA_LEVEL_HEIGHT).clamp(1, Int.MAX_VALUE) * ProtocolDefinition.HEIGHT_SEA_LEVEL_MODIFIER)).clamp(0.0f, 1.0f))
    }

    override fun toString(): String {
        return identifier.toString()
    }

    companion object : ResourceLocationCodec<Biome> {

        private fun getColorMapCoordinate(value: Float): Int {
            return ((1.0 - value.clamp(0.0f, 1.0f)) * RenderConstants.COLORMAP_SIZE).toInt()
        }

        override fun deserialize(registries: Registries?, resourceLocation: ResourceLocation, data: Map<String, Any>): Biome {
            check(registries != null) { "Registries is null!" }
            val effects = data["effects"].toJsonObject() // nbt data
            val skyColor = (data["sky_color"] ?: effects?.get("sky_color"))?.jsonTint()
            val fogColor = (data["fog_color"] ?: effects?.get("fog_color"))?.jsonTint()
            val waterColor = (data["water_color"] ?: effects?.get("water_color"))?.jsonTint()
            val waterFogColor = (data["water_fog_color"] ?: effects?.get("water_fog_color"))?.jsonTint()

            return Biome(
                identifier = resourceLocation,
                temperature = data["temperature"]?.toFloat() ?: 0.0f,
                downfall = data["downfall"]?.toFloat() ?: 0.0f,
                skyColor = skyColor,
                fogColor = fogColor,
                waterColor = waterColor,
                waterFogColor = waterFogColor,
                precipitation = data["precipitation"]?.let { BiomePrecipitation[it] } ?: BiomePrecipitation.NONE,
            )
        }
    }
}
