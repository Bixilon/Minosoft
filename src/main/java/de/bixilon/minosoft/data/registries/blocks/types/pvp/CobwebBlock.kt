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

package de.bixilon.minosoft.data.registries.blocks.types.pvp

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.registries.blocks.factory.BlockFactory
import de.bixilon.minosoft.data.registries.blocks.handler.entity.EntityCollisionHandler
import de.bixilon.minosoft.data.registries.blocks.settings.BlockSettings
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.blocks.types.properties.item.BlockWithItem
import de.bixilon.minosoft.data.registries.blocks.types.properties.physics.CustomDiggingBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.outline.FullOutlinedBlock
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.data.registries.item.items.tool.properties.requirement.ToolRequirement
import de.bixilon.minosoft.data.registries.item.items.tool.shears.ShearsItem
import de.bixilon.minosoft.data.registries.item.items.tool.sword.SwordItem
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.physics.entities.EntityPhysics
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

open class CobwebBlock(identifier: ResourceLocation = Companion.identifier, settings: BlockSettings) : Block(identifier, settings), EntityCollisionHandler, FullOutlinedBlock, ToolRequirement, CustomDiggingBlock, BlockWithItem<Item> {
    override val item: Item = this::item.inject(identifier)
    override val hardness: Float get() = 4.0f


    override fun isCorrectTool(item: Item): Boolean {
        return item is SwordItem || item is ShearsItem
    }

    override fun getMiningSpeed(connection: PlayConnection, state: BlockState, stack: ItemStack, speed: Float): Float {
        if (stack.item.item is SwordItem || stack.item.item is ShearsItem) {
            return 15.0f
        }
        return 1.0f
    }

    override fun onEntityCollision(entity: Entity, physics: EntityPhysics<*>, position: Vec3i, state: BlockState) {
        physics.slowMovement(state, SLOW)
    }

    companion object : BlockFactory<CobwebBlock> {
        override val identifier = minecraft("cobweb")
        val SLOW = Vec3d(0.25, 0.05f, 0.25)

        override fun build(registries: Registries, settings: BlockSettings) = CobwebBlock(settings = settings)
    }
}
