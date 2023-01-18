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

package de.bixilon.minosoft.data.registries.item.items.tools.pickaxe

import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.factory.PixLyzerItemFactory
import de.bixilon.minosoft.data.registries.item.items.tools.MiningToolItem
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.util.KUtil.toResourceLocation

open class PickaxeItem(
    resourceLocation: ResourceLocation,
    registries: Registries,
    data: Map<String, Any>,
) : MiningToolItem(resourceLocation, registries, data) {
    override val diggableTag: ResourceLocation = PICKAXE_MINEABLE_TAG

    companion object : PixLyzerItemFactory<PickaxeItem> {
        val PICKAXE_MINEABLE_TAG = "minecraft:mineable/pickaxe".toResourceLocation()

        override fun build(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>): PickaxeItem {
            return PickaxeItem(resourceLocation, registries, data)
        }
    }
}
