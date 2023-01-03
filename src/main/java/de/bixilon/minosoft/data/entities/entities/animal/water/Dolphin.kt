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
package de.bixilon.minosoft.data.entities.entities.animal.water

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.data.EntityDataField
import de.bixilon.minosoft.data.entities.entities.SynchronizedEntityData
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

class Dolphin(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : WaterAnimal(connection, entityType, data, position, rotation) {

    @get:SynchronizedEntityData
    val treasurePosition: Vec3i?
        get() = data.get(TREASURE_POSITION_DATA, null)

    @get:SynchronizedEntityData
    val hasFish: Boolean
        get() = data.getBoolean(HAS_FISH_DATA, false)

    @get:SynchronizedEntityData
    val moistnessLevel: Int
        get() = data.get(MOISTNESS_LEVEL_DATA, 2400)


    companion object : EntityFactory<Dolphin> {
        override val identifier: ResourceLocation = ResourceLocation("dolphin")
        private val TREASURE_POSITION_DATA = EntityDataField("DOLPHIN_TREASURE_POSITION")
        private val HAS_FISH_DATA = EntityDataField("DOLPHIN_HAS_FISH")
        private val MOISTNESS_LEVEL_DATA = EntityDataField("DOLPHIN_MOISTNESS_LEVEL")


        override fun build(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation): Dolphin {
            return Dolphin(connection, entityType, data, position, rotation)
        }
    }
}
