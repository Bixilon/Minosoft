/*
 * Minosoft
 * Copyright (C) 2020-2024 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.entities.entities.animal.horse

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.data.EntityDataField
import de.bixilon.minosoft.data.entities.entities.LivingEntity
import de.bixilon.minosoft.data.entities.entities.SynchronizedEntityData
import de.bixilon.minosoft.data.entities.entities.animal.Animal
import de.bixilon.minosoft.data.entities.entities.properties.riding.Saddleable
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.physics.entities.vehicle.horse.AbstractHorsePhysics
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import java.util.*

abstract class AbstractHorse(session: PlaySession, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : Animal(session, entityType, data, position, rotation), Saddleable {
    override val primaryPassenger: LivingEntity? get() = if (isSaddled) attachment.passengers.firstOrNull().nullCast() else null

    private fun getAbstractHorseFlag(bitMask: Int): Boolean {
        return data.getBitMask(FLAGS_DATA, bitMask, 0x00)
    }

    @get:SynchronizedEntityData
    val isTame: Boolean
        get() = getAbstractHorseFlag(0x02)

    @get:SynchronizedEntityData
    override val isSaddled: Boolean
        get() = getAbstractHorseFlag(0x04)

    @get:SynchronizedEntityData
    val hasBred: Boolean
        get() = getAbstractHorseFlag(0x08)

    @get:SynchronizedEntityData
    val isEating: Boolean
        get() = getAbstractHorseFlag(0x10)

    @get:SynchronizedEntityData
    val isRearing: Boolean
        get() = getAbstractHorseFlag(0x20)

    @get:SynchronizedEntityData
    val isMouthOpen: Boolean
        get() = getAbstractHorseFlag(0x40)

    @get:SynchronizedEntityData
    val owner: UUID?
        get() = data.get(OWNER_DATA, null)

    override fun physics(): AbstractHorsePhysics<*> = super.physics().unsafeCast()

    companion object {
        val FLAGS_DATA = EntityDataField("ABSTRACT_HORSE_FLAGS")
        private val OWNER_DATA = EntityDataField("ABSTRACT_HORSE_OWNER_UUID")
    }
}
