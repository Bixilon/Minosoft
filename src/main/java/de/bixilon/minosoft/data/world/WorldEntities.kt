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

package de.bixilon.minosoft.data.world

import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity
import de.bixilon.minosoft.util.KUtil.synchronizedMapOf
import de.bixilon.minosoft.util.KUtil.toSynchronizedMap
import glm_.vec3.Vec3d
import java.util.*

class WorldEntities : Iterable<Entity> {
    private val idEntityMap: MutableMap<Int, Entity> = synchronizedMapOf()
    private val entityIdMap: MutableMap<Entity, Int> = synchronizedMapOf()
    private val entityUUIDMap: MutableMap<Entity, UUID> = synchronizedMapOf()
    private val uuidEntityMap: MutableMap<UUID, Entity> = synchronizedMapOf()

    val size: Int
        get() = idEntityMap.size


    fun add(entityId: Int?, entityUUID: UUID?, entity: Entity) {
        check(entityId != null || entityUUID != null) { "Entity id and UUID is null!" }
        entityId?.let {
            idEntityMap[it] = entity
            entityIdMap[entity] = it
        }
        entityUUID?.let {
            uuidEntityMap[it] = entity
            entityUUIDMap[entity] = it
        }
    }

    operator fun get(id: Int): Entity? {
        return idEntityMap[id]
    }

    fun getId(entity: Entity): Int? {
        return entityIdMap[entity]
    }

    operator fun get(uuid: UUID): Entity? {
        return uuidEntityMap[uuid]
    }

    fun getUUID(entity: Entity): UUID? {
        return entityUUIDMap[entity]
    }

    fun remove(entity: Entity) {
        entityIdMap[entity]?.let {
            idEntityMap.remove(it)
        }
        entityIdMap.remove(entity)
        entityUUIDMap[entity]?.let {
            uuidEntityMap.remove(it)
        }
        entityUUIDMap.remove(entity)
    }

    fun remove(entityId: Int) {
        idEntityMap[entityId]?.let { remove(it) }
    }

    fun remove(entityUUID: UUID) {
        uuidEntityMap[entityUUID]?.let { remove(it) }
    }

    override fun iterator(): Iterator<Entity> {
        return idEntityMap.toSynchronizedMap().values.iterator()
    }

    fun getInRadius(position: Vec3d, distance: Double, check: (Entity) -> Boolean): List<Entity> {
        // ToDo: Improve performance
        val ret: MutableList<Entity> = mutableListOf()
        val entities = idEntityMap.toSynchronizedMap().values

        for (entity in entities) {
            if ((entity.position - position).length() > distance) {
                continue
            }
            if (check(entity)) {
                ret += entity
            }
        }
        return ret.toList()
    }

    fun getClosestInRadius(position: Vec3d, distance: Double, check: (Entity) -> Boolean): Entity? {
        val entities = getInRadius(position, distance, check)
        var closestDistance = Double.MAX_VALUE
        var closestEntity: Entity? = null

        for (entity in entities) {
            val currentDistance = (entity.position - position).length()
            if (currentDistance < closestDistance) {
                closestDistance = currentDistance
                closestEntity = entity
            }
        }

        return closestEntity
    }

    companion object {
        val CHECK_CLOSEST_PLAYER: (Entity) -> Boolean = check@{
            if (it !is PlayerEntity) {
                return@check false
            }
            if (it.gamemode == Gamemodes.SPECTATOR) {
                return@check false
            }
            return@check true
        }
    }
}
