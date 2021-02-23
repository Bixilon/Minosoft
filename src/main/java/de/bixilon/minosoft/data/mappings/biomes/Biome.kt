/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.mappings.biomes

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.mappings.IdentifierDeserializer
import de.bixilon.minosoft.data.mappings.ModIdentifier
import de.bixilon.minosoft.data.mappings.RegistryItem
import de.bixilon.minosoft.data.mappings.versions.VersionMapping
import de.bixilon.minosoft.data.text.RGBColor

data class Biome(
    val identifier: ModIdentifier,
    val depth: Float,
    val scale: Float,
    val temperature: Float,
    val downfall: Float,
    val water_color: RGBColor,
    val water_fog_color: RGBColor,
    val category: BiomeCategory,
    val precipation: BiomePrecipation,
    val skyColor: RGBColor,
    val foliageColor: RGBColor?,
    val descriptionId: String?,
) : RegistryItem {

    override fun toString(): String {
        return identifier.toString()
    }

    companion object : IdentifierDeserializer<Biome> {
        override fun deserialize(mappings: VersionMapping, identifier: ModIdentifier, data: JsonObject): Biome {
            return Biome(
                identifier = identifier,
                depth = data["depth"]?.asFloat ?: 0f,
                scale = data["scale"]?.asFloat ?: 0f,
                temperature = data["temperature"]?.asFloat ?: 0f,
                downfall = data["downfall"]?.asFloat ?: 0f,
                water_color = RGBColor(data["water_color"]?.asInt ?: 0),
                water_fog_color = RGBColor(data["water_fog_color"]?.asInt ?: 0),
                category = mappings.biomeCategoryRegistry.get(data["category"]?.asInt ?: -1) ?: DEFAULT_CATEGORY,
                precipation = mappings.biomePrecipationRegistry.get(data["precipitation"]?.asInt ?: -1) ?: DEFAULT_PRECIPATION,
                skyColor = RGBColor(data["sky_color"]?.asInt ?: 0),
                foliageColor = RGBColor(data["foliage_color"]?.asInt ?: 0),
                descriptionId = data["water_fog_color"]?.asString,
            )
        }

        private val DEFAULT_PRECIPATION = BiomePrecipation("NONE")
        private val DEFAULT_CATEGORY = BiomeCategory("NONE")

    }
}
