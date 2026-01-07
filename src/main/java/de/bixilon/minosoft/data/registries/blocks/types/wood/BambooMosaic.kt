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
import de.bixilon.minosoft.data.registries.blocks.types.building.SlabBlock
import de.bixilon.minosoft.data.registries.blocks.types.building.StairsBlock
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.Registries

// TODO: They
interface BambooMosaic {


    class Slab(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : SlabBlock.WoodSlab(identifier, settings), BambooMosaic {

        companion object : BlockFactory<Slab> {
            override val identifier = minecraft("bamboo_mosaic_slab")

            override fun build(registries: Registries, settings: BlockSettings) = Slab(settings = settings)
        }
    }

    class Wood(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : PlanksBlock(identifier, settings), BambooMosaic {

        companion object : BlockFactory<Wood> {
            override val identifier = minecraft("bamboo_mosaic")

            override fun build(registries: Registries, settings: BlockSettings) = Wood(settings = settings)
        }
    }

    class Stairs(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : StairsBlock.Wooden(identifier, settings), BambooMosaic {

        companion object : BlockFactory<Stairs> {
            override val identifier = minecraft("bamboo_mosaic_stairs")

            override fun build(registries: Registries, settings: BlockSettings) = Stairs(settings = settings)
        }
    }
}
