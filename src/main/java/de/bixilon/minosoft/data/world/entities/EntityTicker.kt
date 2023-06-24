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

package de.bixilon.minosoft.data.world.entities

import de.bixilon.kutil.collections.iterator.async.QueuedIterator
import de.bixilon.kutil.concurrent.pool.ThreadPool
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.LivingEntity
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity

class EntityTicker(val entities: WorldEntities) {
    private val iterator = QueuedIterator(entities.entities.spliterator(), priority = ThreadPool.HIGH, queueSize = 1000)

    private fun tickEntity(entity: Entity) {
        if (!entity.tryTick()) {
            // not ticked
            return
        }

        for (passenger in entity.attachment.passengers) {
            tickPassenger(passenger)
        }
    }

    private fun tickPassenger(entity: Entity) {
        if (entity is LivingEntity && entity.health == 0.0) {
            entity.attachment.vehicle = null
            return
        }
        if (entity !is PlayerEntity) return

        entity.tickRiding()

        for (passenger in entity.attachment.passengers) {
            tickPassenger(passenger)
        }
    }


    fun tick() {
        iterator.reuse(entities.entities.spliterator())
        iterator.iterate {
            if (it.attachment.vehicle != null) {
                return@iterate
            }
            tickEntity(it)
        }
    }
}
