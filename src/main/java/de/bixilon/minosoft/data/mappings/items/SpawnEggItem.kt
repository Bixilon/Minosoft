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

package de.bixilon.minosoft.data.mappings.items

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.versions.VersionMapping
import de.bixilon.minosoft.data.text.RGBColor.Companion.asRGBColor

open class SpawnEggItem(
    resourceLocation: ResourceLocation,
    versionMapping: VersionMapping,
    data: JsonObject,
) : Item(resourceLocation, versionMapping, data) {
    val color1 = data["spawn_egg_color_1"]?.asInt?.asRGBColor()
    val color2 = data["spawn_egg_color_2"]?.asInt?.asRGBColor()
    val entityType = data["spawn_egg_entity_type"]?.asInt?.let { versionMapping.entityTypeRegistry[it] }
}
