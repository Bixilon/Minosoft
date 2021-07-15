/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
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

import de.bixilon.minosoft.data.entities.EntityMetaDataFields
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.EntityMetaDataFunction
import de.bixilon.minosoft.data.inventory.ItemStack
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.gui.rendering.util.VecUtil.empty
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import glm_.vec3.Vec3d
import glm_.vec3.Vec3i

class ItemEntity(connection: PlayConnection, entityType: EntityType, position: Vec3d, rotation: EntityRotation) : Entity(connection, entityType, position, rotation) {

    @get:EntityMetaDataFunction(name = "Item")
    val item: ItemStack?
        get() = entityMetaData.sets.getItemStack(EntityMetaDataFields.ITEM_ITEM)


    override fun realTick() {
        super.realTick()

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


    companion object : EntityFactory<ItemEntity> {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("item")

        override fun build(connection: PlayConnection, entityType: EntityType, position: Vec3d, rotation: EntityRotation): ItemEntity {
            return ItemEntity(connection, entityType, position, rotation)
        }
    }
}