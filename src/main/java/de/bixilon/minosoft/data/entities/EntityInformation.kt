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
package de.bixilon.minosoft.data.entities

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.mappings.ResourceLocation

data class EntityInformation(
    val resourceLocation: ResourceLocation,
    val descriptionId: String?,
    val width: Float,
    val height: Float,
    val size_fixed: Boolean,
    val fireImmune: Boolean,
) {

    companion object {
        fun deserialize(resourceLocation: ResourceLocation, data: JsonObject): EntityInformation {
            return EntityInformation(
                resourceLocation = resourceLocation,
                descriptionId = data["description_id"]?.asString,
                width = data["width"].asFloat,
                height = data["height"].asFloat,
                fireImmune = data["fire_immune"]?.asBoolean ?: false,
                size_fixed = data["site_fixed"]?.asBoolean ?: false
            )
        }
    }
}
