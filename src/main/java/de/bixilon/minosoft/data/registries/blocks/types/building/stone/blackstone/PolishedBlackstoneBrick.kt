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

package de.bixilon.minosoft.data.registries.blocks.types.building.stone.blackstone

import de.bixilon.minosoft.data.registries.blocks.factory.BlockFactory
import de.bixilon.minosoft.data.registries.blocks.settings.BlockSettings
import de.bixilon.minosoft.data.registries.blocks.types.building.RockBlock
import de.bixilon.minosoft.data.registries.blocks.types.building.SlabBlock
import de.bixilon.minosoft.data.registries.blocks.types.building.StairsBlock
import de.bixilon.minosoft.data.registries.blocks.types.building.stone.Stone
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.Registries

interface PolishedBlackstoneBrick : Stone {
    override val hardness get() = 1.5f

    open class Block(identifier: ResourceLocation = Companion.identifier, settings: BlockSettings) : RockBlock(identifier, settings), PolishedBlackstoneBrick {

        companion object : BlockFactory<Block> {
            override val identifier = minecraft("polished_blackstone_bricks")

            override fun build(registries: Registries, settings: BlockSettings) = Block(settings = settings)
        }
    }

    class Slab(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : SlabBlock.Stone(identifier, settings), PolishedBlackstoneBrick {
        override val hardness get() = 2.0f

        companion object : BlockFactory<Slab> {
            override val identifier = minecraft("polished_blackstone_brick_slab")

            override fun build(registries: Registries, settings: BlockSettings) = Slab(settings = settings)
        }
    }

    class Stairs(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : StairsBlock.Stone(identifier, settings), PolishedBlackstoneBrick {
        override val hardness get() = super<PolishedBlackstoneBrick>.hardness

        companion object : BlockFactory<Stairs> {
            override val identifier = minecraft("polished_blackstone_brick_stairs")

            override fun build(registries: Registries, settings: BlockSettings) = Stairs(settings = settings)
        }
    }
}
