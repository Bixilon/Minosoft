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
package de.bixilon.minosoft.data.entities.entities

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kutil.primitive.BooleanUtil.toBoolean
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.data.EntityDataField
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

abstract class AgeableMob(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : PathfinderMob(connection, entityType, data, position, rotation) {

    @get:SynchronizedEntityData
    open val isBaby: Boolean by data(BABY, false) { it.toBoolean() }


    companion object {
        val BABY = EntityDataField("AGEABLE_IS_BABY")
    }
}
