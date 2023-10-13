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

package de.bixilon.minosoft.data.registries.blocks.types.wood

import de.bixilon.minosoft.data.registries.blocks.factory.BlockFactory
import de.bixilon.minosoft.data.registries.blocks.settings.BlockSettings
import de.bixilon.minosoft.data.registries.blocks.types.building.door.DoorBlock
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.Registries

interface Acacia {

    class Leaves(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : LeavesBlock(identifier, settings), Acacia {

        companion object : BlockFactory<Leaves> {
            override val identifier = minecraft("acacia_leaves")

            override fun build(registries: Registries, settings: BlockSettings) = Leaves(settings = settings)
        }
    }

    class Door(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : DoorBlock.WoodenDoor(identifier, settings), Acacia {

        companion object : BlockFactory<Door> {
            override val identifier = minecraft("acacia_door")

            override fun build(registries: Registries, settings: BlockSettings) = Door(settings = settings)
        }
    }
}
