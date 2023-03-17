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

package de.bixilon.minosoft.data.registries.item.items.block.climbing

import de.bixilon.kutil.json.JsonObject
import de.bixilon.minosoft.data.registries.blocks.types.climbing.ScaffoldingBlock
import de.bixilon.minosoft.data.registries.identified.Identified
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.factory.ItemFactory
import de.bixilon.minosoft.data.registries.item.items.block.BlockItem
import de.bixilon.minosoft.data.registries.item.stack.StackableItem
import de.bixilon.minosoft.data.registries.registries.Registries

interface ClimbingItems {

    open class ScaffoldingItem(identifier: ResourceLocation = this.identifier) : BlockItem<ScaffoldingBlock>(identifier), StackableItem {
        override val _block: Identified get() = ScaffoldingBlock

        companion object : ItemFactory<ScaffoldingItem> {
            override val identifier = minecraft("scaffolding")

            override fun build(registries: Registries, data: JsonObject) = ScaffoldingItem()
        }
    }
}
