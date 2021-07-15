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
package de.bixilon.minosoft.data.entities.entities.projectile

import de.bixilon.minosoft.data.entities.EntityMetaDataFields
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.entities.EntityMetaDataFunction
import de.bixilon.minosoft.data.inventory.ItemStack
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import glm_.vec3.Vec3d

class FireworkRocketEntity(connection: PlayConnection, entityType: EntityType, position: Vec3d, rotation: EntityRotation) : Projectile(connection, entityType, position, rotation) {

    @get:EntityMetaDataFunction(name = "Item")
    val fireworkItem: ItemStack?
        get() = entityMetaData.sets.getItemStack(EntityMetaDataFields.FIREWORK_ROCKET_ENTITY_ITEM)

    @get:EntityMetaDataFunction(name = "Attached entity id")
    override var attachedEntity: Int?
        get() = entityMetaData.sets.getInt(EntityMetaDataFields.FIREWORK_ROCKET_ENTITY_ATTACHED_ENTITY)
        set(attachedEntity) {
            super.attachedEntity = attachedEntity
        }

    @get:EntityMetaDataFunction(name = "Shot at angle")
    val isShotAtAngle: Boolean
        get() = entityMetaData.sets.getBoolean(EntityMetaDataFields.FIREWORK_ROCKET_ENTITY_SHOT_AT_ANGLE)

    companion object : EntityFactory<FireworkRocketEntity> {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("firework_rocket")

        override fun build(connection: PlayConnection, entityType: EntityType, position: Vec3d, rotation: EntityRotation): FireworkRocketEntity {
            return FireworkRocketEntity(connection, entityType, position, rotation)
        }
    }
}
