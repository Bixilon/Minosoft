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

package de.bixilon.minosoft.data.registries.blocks.types.building.brick

import de.bixilon.minosoft.data.registries.blocks.factory.BlockFactory
import de.bixilon.minosoft.data.registries.blocks.settings.BlockSettings
import de.bixilon.minosoft.data.registries.blocks.types.building.RockBlock
import de.bixilon.minosoft.data.registries.blocks.types.building.SlabBlock
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.Registries

interface RedNetherBrick {

    open class Block(identifier: ResourceLocation = Companion.identifier, settings: BlockSettings) : RockBlock(identifier, settings), RedNetherBrick {

        companion object : BlockFactory<Block> {
            override val identifier = minecraft("red_nether_bricks")

            override fun build(registries: Registries, settings: BlockSettings) = Block(settings = settings)
        }
    }

    class Slab(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : SlabBlock.AbstractStoneSlab(identifier, settings), RedNetherBrick {

        companion object : BlockFactory<Slab> {
            override val identifier = minecraft("red_nether_brick_slab")

            override fun build(registries: Registries, settings: BlockSettings) = Slab(settings = settings)
        }
    }
}
