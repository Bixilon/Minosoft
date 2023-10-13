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

package de.bixilon.minosoft.data.registries.blocks.types.bee

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.registries.blocks.factory.BlockFactory
import de.bixilon.minosoft.data.registries.blocks.handler.entity.EntityCollisionHandler
import de.bixilon.minosoft.data.registries.blocks.settings.BlockSettings
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.state.builder.BlockStateBuilder
import de.bixilon.minosoft.data.registries.blocks.state.builder.BlockStateSettings
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.blocks.types.properties.hardness.InstantBreakableBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.item.BlockWithItem
import de.bixilon.minosoft.data.registries.blocks.types.properties.physics.JumpBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.physics.VelocityBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.collision.fixed.StatelessCollidable
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.outline.FullOutlinedBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.transparency.TranslucentBlock
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.data.registries.shapes.voxel.AbstractVoxelShape
import de.bixilon.minosoft.data.registries.shapes.voxel.VoxelShape
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.physics.PhysicsConstants
import de.bixilon.minosoft.physics.entities.EntityPhysics
import de.bixilon.minosoft.protocol.versions.Version
import kotlin.math.abs

open class HoneyBlock(identifier: ResourceLocation = Companion.identifier, settings: BlockSettings) : Block(identifier, settings), EntityCollisionHandler, JumpBlock, VelocityBlock, BeeBlock, TranslucentBlock, InstantBreakableBlock, StatelessCollidable, FullOutlinedBlock, BlockWithItem<Item>, BlockStateBuilder {
    override val item: Item = this::item.inject(identifier)
    override val velocity: Float get() = 0.4f
    override val jumpBoost: Float get() = 0.5f
    override val collisionShape: AbstractVoxelShape get() = COLLISION_BOX

    override fun buildState(version: Version, settings: BlockStateSettings) = BlockState(this, settings)

    private fun isSliding(position: BlockPosition, physics: EntityPhysics<*>): Boolean {
        if (physics.onGround) {
            return false
        }
        if (physics.position.y > position.y + MAX_Y - 1.0E-7) {
            return false
        }
        if (physics.velocity.y >= -PhysicsConstants.GRAVITY) {
            return false
        }
        val x = abs(position.x + 0.5 - physics.position.x) + 1.0E-7
        val z = abs(position.z + 0.5 - physics.position.z) + 1.0E-7

        val width = MAX_Y - 0.5 + physics.entity.dimensions.x / 2.0
        return x > width || z > width
    }

    private fun slide(physics: EntityPhysics<*>) {
        val velocity = physics.velocity
        val speed = if (velocity.y < -0.13) (-SLIDE_GRAVITY) / velocity.y else 1.0
        physics.velocity = Vec3d(velocity.x * speed, -SLIDE_GRAVITY, velocity.z * speed)
        physics.fallDistance = 0.0f
    }

    override fun onEntityCollision(entity: Entity, physics: EntityPhysics<*>, position: Vec3i, state: BlockState) {
        if (!isSliding(position, physics)) {
            return
        }

        slide(physics)
    }

    companion object : BlockFactory<HoneyBlock> {
        override val identifier = minecraft("honey_block")
        private val COLLISION_BOX = VoxelShape(AABB(0.0625, 0.0, 0.0625, 0.9375, 0.9375, 0.9375))
        const val MAX_Y = 0.9375
        const val SLIDE_GRAVITY = 0.05

        override fun build(registries: Registries, settings: BlockSettings) = HoneyBlock(settings = settings)
    }
}

