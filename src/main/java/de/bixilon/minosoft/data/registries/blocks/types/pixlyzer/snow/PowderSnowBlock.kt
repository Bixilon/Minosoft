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

package de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.snow

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.container.equipment.EquipmentSlots
import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.LivingEntity
import de.bixilon.minosoft.data.entities.entities.item.FallingBlockEntity
import de.bixilon.minosoft.data.registries.blocks.factory.BlockFactory
import de.bixilon.minosoft.data.registries.blocks.handler.entity.EntityCollisionHandler
import de.bixilon.minosoft.data.registries.blocks.settings.BlockSettings
import de.bixilon.minosoft.data.registries.blocks.shapes.collision.context.CollisionContext
import de.bixilon.minosoft.data.registries.blocks.shapes.collision.context.EntityCollisionContext
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.collision.CollidableBlock
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.items.armor.materials.LeatherArmor
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.data.registries.shapes.voxel.AbstractVoxelShape
import de.bixilon.minosoft.data.registries.shapes.voxel.VoxelShape
import de.bixilon.minosoft.physics.entities.EntityPhysics
import de.bixilon.minosoft.tags.entity.MinecraftEntityTags.isIn

open class PowderSnowBlock(identifier: ResourceLocation = PowderSnowBlock.identifier, settings: BlockSettings) : Block(identifier, settings), EntityCollisionHandler, CollidableBlock {
    override val hardness: Float get() = 0.25f

    override fun onEntityCollision(entity: Entity, physics: EntityPhysics<*>, position: Vec3i, state: BlockState) {
        if (entity is LivingEntity && physics.positionInfo.block?.block !is PowderSnowBlock) {
            return
        }
        physics.slowMovement(state, Vec3d(0.9f, 1.5, 0.9f))
    }

    override fun getCollisionShape(context: CollisionContext, blockPosition: Vec3i, state: BlockState, blockEntity: BlockEntity?): AbstractVoxelShape? {
        if (context !is EntityCollisionContext) {
            return null
        }
        if (context.physics.fallDistance > 2.5f) {
            return FALLING_SHAPE
        }
        if (context.entity is FallingBlockEntity || (context.entity.canWalkOnPowderSnow() && context.isAbove(1.0, blockPosition) && !context.descending)) {
            return AbstractVoxelShape.FULL
        }
        return null
    }


    companion object : BlockFactory<PowderSnowBlock> {
        override val identifier = minecraft("powder_snow")
        private val FALLING_SHAPE = VoxelShape(AABB(Vec3d(0.0, 0.0, 0.0), Vec3d(1.0, 0.9f, 1.0)))
        private val TAG = minecraft("powder_snow_walkable_mobs")


        fun Entity.canWalkOnPowderSnow(): Boolean {
            if (this.isIn(TAG)) {
                return true
            }
            if (this !is LivingEntity) return false

            return this.equipment[EquipmentSlots.FEET]?.item?.item is LeatherArmor.LeatherBoots
        }

        override fun build(registries: Registries, settings: BlockSettings) = PowderSnowBlock(settings = settings)
    }
}
