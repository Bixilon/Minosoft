/*
 * Minosoft
 * Copyright (C) 2020-2026 Moritz Zwerger
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
import de.bixilon.minosoft.data.registries.blocks.types.building.DoorBlock
import de.bixilon.minosoft.data.registries.blocks.types.building.FenceBlock
import de.bixilon.minosoft.data.registries.blocks.types.building.SlabBlock
import de.bixilon.minosoft.data.registries.blocks.types.building.StairsBlock
import de.bixilon.minosoft.data.registries.blocks.types.legacy.FlatteningRenamedModel
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.Registries

interface Oak {

    class Leaves(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : LeavesBlock(identifier, settings), Oak {

        companion object : BlockFactory<Leaves> {
            override val identifier = minecraft("oak_leaves")

            override fun build(registries: Registries, settings: BlockSettings) = Leaves(settings = settings)
        }
    }

    class Door(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : DoorBlock.WoodenDoor(identifier, settings), Oak, FlatteningRenamedModel {
        override val legacyModelName get() = minecraft("wooden_door")

        companion object : BlockFactory<Door> {
            override val identifier = minecraft("oak_door")

            override fun build(registries: Registries, settings: BlockSettings) = Door(settings = settings)
        }
    }

    class Slab(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : SlabBlock.WoodSlab(identifier, settings), Oak {

        companion object : BlockFactory<Slab> {
            override val identifier = minecraft("oak_slab")

            override fun build(registries: Registries, settings: BlockSettings) = Slab(settings = settings)
        }
    }

    class Planks(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : PlanksBlock(identifier, settings), Oak {

        companion object : BlockFactory<Planks> {
            override val identifier = minecraft("oak_planks") // was this planks before?

            override fun build(registries: Registries, settings: BlockSettings) = Planks(settings = settings)
        }
    }

    class Fence(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : FenceBlock(identifier, settings), Oak, FlatteningRenamedModel {
        override val legacyModelName get() = FLATTENING_MODEL

        companion object : BlockFactory<Fence> {
            private val FLATTENING_MODEL = minecraft("fence")
            override val identifier = minecraft("oak_fence")

            override fun build(registries: Registries, settings: BlockSettings) = Fence(settings = settings)
        }
    }

    class Stairs(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : StairsBlock.Wooden(identifier, settings), Oak {

        companion object : BlockFactory<Stairs> {
            override val identifier = minecraft("oak_stairs")

            override fun build(registries: Registries, settings: BlockSettings) = Stairs(settings = settings)
        }
    }
}
