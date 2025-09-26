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
package de.bixilon.minosoft.data.entities.entities.animal

import de.bixilon.kmath.vec.vec3.d.Vec3d
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.data.EntityDataField
import de.bixilon.minosoft.data.entities.entities.SynchronizedEntityData
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.protocol.network.session.play.PlaySession

class Bee(session: PlaySession, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : Animal(session, entityType, data, position, rotation) {

    private fun getBeeFlag(bitMask: Int): Boolean {
        return data.getBitMask(FLAGS_DATA, bitMask, 0x00)
    }

    @get:SynchronizedEntityData
    val isAngry: Boolean
        get() = getBeeFlag(0x02)

    @get:SynchronizedEntityData
    val hasStung: Boolean
        get() = getBeeFlag(0x04)

    @get:SynchronizedEntityData
    val hasNectar: Boolean
        get() = getBeeFlag(0x08)

    @get:SynchronizedEntityData
    val remainingAngerTimer: Int
        get() = data.get(REMAINING_ANGER_DATA, 0)


    companion object : EntityFactory<Bee> {
        override val identifier: ResourceLocation = minecraft("bee")
        private val FLAGS_DATA = EntityDataField("BEE_FLAGS")
        private val REMAINING_ANGER_DATA = EntityDataField("BEE_REMAINING_ANGER_TIME")

        override fun build(session: PlaySession, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation): Bee {
            return Bee(session, entityType, data, position, rotation)
        }
    }
}
