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

import de.bixilon.kutil.json.JsonUtil.toJsonObject
import de.bixilon.kutil.primitive.FloatUtil.toFloat
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.registries.registries.registry.RegistryItem
import de.bixilon.minosoft.data.registries.registries.registry.codec.IdentifierCodec
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.tint.TintManager.Companion.jsonTint
import de.bixilon.minosoft.gui.rendering.tint.tints.ColorMapTint

data class Biome(
    override val identifier: ResourceLocation,
    val temperature: Float,
    val downfall: Float,
    val skyColor: RGBColor? = null,
    val fogColor: RGBColor? = null,
    val waterColor: RGBColor? = null,
    val waterFogColor: RGBColor? = null,
    val precipitation: BiomePrecipitation? = null,
) : RegistryItem() {
    val grassModifier = GrassColorModifiers.BIOME_MAP[identifier]


    val temperatureIndex = ColorMapTint.getIndex(temperature)
    val downfallIndex = ColorMapTint.getIndex(downfall * temperature)


    companion object : IdentifierCodec<Biome> {

        override fun deserialize(registries: Registries?, identifier: ResourceLocation, data: Map<String, Any>): Biome {
            val effects = data["effects"].toJsonObject() // nbt data
            val skyColor = (data["sky_color"] ?: effects?.get("sky_color"))?.jsonTint()
            val fogColor = (data["fog_color"] ?: effects?.get("fog_color"))?.jsonTint()
            val waterColor = (data["water_color"] ?: effects?.get("water_color"))?.jsonTint()
            val waterFogColor = (data["water_fog_color"] ?: effects?.get("water_fog_color"))?.jsonTint()

            return Biome(
                identifier = identifier,
                temperature = data["temperature"]?.toFloat() ?: 0.0f,
                downfall = data["downfall"]?.toFloat() ?: 0.0f,
                skyColor = skyColor,
                fogColor = fogColor,
                waterColor = waterColor,
                waterFogColor = waterFogColor,
                precipitation = data["precipitation"]?.let { if (it is Int) BiomePrecipitation.getOrNull(it - 1) else if (it.toString().lowercase() == "none") null else BiomePrecipitation[it] },
            )
        }
    }
}
