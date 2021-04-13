/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.mappings.inventory

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.mappings.registry.IdDeserializer
import de.bixilon.minosoft.data.mappings.registry.RegistryFakeEnumerable
import de.bixilon.minosoft.data.mappings.versions.VersionMapping

data class CreativeModeTab(
    override val name: String,
    val recipeFolderName: String,
    val backgroundSuffix: String,
    val canScroll: Boolean,
    val showTitle: Boolean,
) : RegistryFakeEnumerable {


    companion object : IdDeserializer<CreativeModeTab> {
        override fun deserialize(mappings: VersionMapping, data: JsonObject): CreativeModeTab {
            return CreativeModeTab(
                name = data["language_id"].asString,
                recipeFolderName = data["recipe_folder_name"].asString,
                backgroundSuffix = data["background_suffix"].asString,
                canScroll = data["can_scroll"].asBoolean,
                showTitle = data["show_title"].asBoolean,
            )
        }
    }
}
