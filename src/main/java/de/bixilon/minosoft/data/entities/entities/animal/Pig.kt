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
package de.bixilon.minosoft.data.entities.entities.animal

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.data.EntityDataField
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.SynchronizedEntityData
import de.bixilon.minosoft.data.entities.entities.properties.riding.ItemRideable
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.items.fishing.rod.OnAStickItem
import de.bixilon.minosoft.physics.entities.living.animal.PigPhysics
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

class Pig(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : Animal(connection, entityType, data, position, rotation), ItemRideable {

    override val primaryPassenger: Entity?
        get() = super<ItemRideable>.primaryPassenger

    @get:SynchronizedEntityData
    override val isSaddled: Boolean
        get() = data.getBoolean(HAS_SADDLE_DATA, false)

    @get:SynchronizedEntityData
    val boostTime: Int
        get() = data.get(BOOST_TIME_DATA, 0)


    override fun isSteerableWith(stack: ItemStack): Boolean {
        return stack.item.item is OnAStickItem.CarrotOnAStickItem
    }

    override fun createPhysics() = PigPhysics(this)

    companion object : EntityFactory<Pig> {
        override val identifier: ResourceLocation = minecraft("pig")
        val HAS_SADDLE_DATA = EntityDataField("PIG_HAS_SADDLE")
        private val BOOST_TIME_DATA = EntityDataField("PIG_BOOST_TIME")


        override fun build(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation): Pig {
            return Pig(connection, entityType, data, position, rotation)
        }
    }
}
