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
package de.bixilon.minosoft.data.registries.inventory

import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.registries.registries.registry.IdDeserializer
import de.bixilon.minosoft.data.registries.registries.registry.RegistryFakeEnumerable
import de.bixilon.minosoft.util.KUtil.toBoolean
import de.bixilon.minosoft.util.KUtil.unsafeCast

data class CreativeModeTab(
    override val name: String,
    val recipeFolderName: String,
    val backgroundSuffix: String,
    val canScroll: Boolean,
    val showTitle: Boolean,
) : RegistryFakeEnumerable {


    companion object : IdDeserializer<CreativeModeTab> {
        override fun deserialize(registries: Registries, data: Map<String, Any>): CreativeModeTab {
            return CreativeModeTab(
                name = data["language_id"].unsafeCast(),
                recipeFolderName = data["recipe_folder_name"].unsafeCast(),
                backgroundSuffix = data["background_suffix"].unsafeCast(),
                canScroll = data["can_scroll"]!!.toBoolean(),
                showTitle = data["show_title"]!!.toBoolean(),
            )
        }
    }
}
