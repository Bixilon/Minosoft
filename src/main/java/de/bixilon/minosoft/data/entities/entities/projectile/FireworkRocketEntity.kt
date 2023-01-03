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
package de.bixilon.minosoft.data.entities.entities.projectile

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.data.EntityDataField
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.SynchronizedEntityData
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil

class FireworkRocketEntity(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : Projectile(connection, entityType, data, position, rotation) {

    @get:SynchronizedEntityData
    val fireworkItem: ItemStack?
        get() = data.get(ITEM_DATA, null)

    @get:SynchronizedEntityData
    override var _attachedEntity: Int?
        get() = data.get(ATTACHED_ENTITY_DATA, null)
        set(attachedEntity) {}

    val attachedEntity: Entity?
        get() = connection.world.entities[_attachedEntity]

    @get:SynchronizedEntityData
    val isShotAtAngle: Boolean
        get() = data.getBoolean(SHOT_AT_ANGLE_DATA, false)

    override fun onAttack(attacker: Entity): Boolean = false

    companion object : EntityFactory<FireworkRocketEntity> {
        override val identifier: ResourceLocation = KUtil.minecraft("firework_rocket")
        private val ITEM_DATA = EntityDataField("FIREWORK_ROCKET_ENTITY_ITEM")
        private val ATTACHED_ENTITY_DATA = EntityDataField("FIREWORK_ROCKET_ENTITY_ATTACHED_ENTITY")
        private val SHOT_AT_ANGLE_DATA = EntityDataField("FIREWORK_ROCKET_ENTITY_SHOT_AT_ANGLE")


        override fun build(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation): FireworkRocketEntity {
            return FireworkRocketEntity(connection, entityType, data, position, rotation)
        }
    }
}
