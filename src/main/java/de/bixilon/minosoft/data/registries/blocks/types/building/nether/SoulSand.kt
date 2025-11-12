/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.registries.blocks.types.building.nether

import de.bixilon.minosoft.data.registries.blocks.factory.BlockFactory
import de.bixilon.minosoft.data.registries.blocks.light.OpaqueProperty
import de.bixilon.minosoft.data.registries.blocks.settings.BlockSettings
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.blocks.types.properties.item.BlockWithItem
import de.bixilon.minosoft.data.registries.blocks.types.properties.physics.VelocityBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.collision.CollidableBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.outline.OutlinedBlock
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.data.registries.item.items.tool.shovel.ShovelRequirement
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB

open class SoulSand(identifier: ResourceLocation = Companion.identifier, settings: BlockSettings) : Block(identifier, settings), ShovelRequirement, CollidableBlock, OutlinedBlock, BlockWithItem<Item>, VelocityBlock {
    override val item: Item = this::item.inject(identifier)
    override val hardness get() = 0.5f
    override val velocity get() = 0.4f

    override val lightProperties get() = OpaqueProperty
    override val outlineShape get() = AABB.BLOCK

    override val collisionShape get() = COLLISION_SHAPE

    companion object : BlockFactory<SoulSand> {
        override val identifier = minecraft("soul_sand")
        private val COLLISION_SHAPE = AABB(0.0, 0.0, 0.0, 1.0, 0.875, 1.0)

        override fun build(registries: Registries, settings: BlockSettings) = SoulSand(settings = settings)
    }
}
