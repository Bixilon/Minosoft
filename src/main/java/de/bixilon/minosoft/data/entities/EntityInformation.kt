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
import de.bixilon.minosoft.data.mappings.registry.Translatable

data class EntityInformation(
    val resourceLocation: ResourceLocation,
    override val translationKey: String?,
    val width: Float,
    val height: Float,
    val sizeFixed: Boolean,
    val fireImmune: Boolean,
) : Translatable {

    companion object {
        fun deserialize(resourceLocation: ResourceLocation, data: JsonObject): EntityInformation {
            return EntityInformation(
                resourceLocation = resourceLocation,
                translationKey = data["description_id"]?.asString,
                width = data["width"].asFloat,
                height = data["height"].asFloat,
                fireImmune = data["fire_immune"]?.asBoolean ?: false,
                sizeFixed = data["size_fixed"]?.asBoolean ?: false
            )
        }
    }
}
