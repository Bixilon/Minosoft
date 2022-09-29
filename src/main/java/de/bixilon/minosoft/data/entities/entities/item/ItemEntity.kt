/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.entities.entities.item

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.data.EntityDataField
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.SynchronizedEntityData
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.gui.rendering.util.VecUtil.empty
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

class ItemEntity(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : Entity(connection, entityType, data, position, rotation) {

    @get:SynchronizedEntityData
    val item: ItemStack?
        get() = data.get(ITEM_DATA, null)


    override fun tick() {
        super.tick()

        when {
            // ToDo: Apply water and lava "bouncing"
            hasGravity -> applyGravity()
        }

        if (!onGround || !velocity.empty) {
            move(velocity)

            var movement = 0.98
            if (onGround) {
                movement = (connection.world[Vec3i(positionInfo.blockPosition.x, position.y - 1.0, position.z)]?.block?.friction ?: 1.0) * 0.98
            }
            velocity = velocity * Vec3d(movement, 0.98, movement)

            if (onGround && velocity.y < 0.0) {
                velocity.y *= -0.5
            }
        }
    }

    override fun onAttack(attacker: Entity): Boolean {
        return false
    }


    companion object : EntityFactory<ItemEntity> {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("item")
        private val ITEM_DATA = EntityDataField("ITEM_ITEM")

        override fun build(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation): ItemEntity {
            return ItemEntity(connection, entityType, data, position, rotation)
        }
    }
}
