/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
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

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kutil.concurrent.lock.simple.SimpleLock
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity
import de.bixilon.minosoft.data.registries.VoxelShape
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import java.util.*

class WorldEntities : Iterable<Entity> {
    private val idEntityMap: Int2ObjectOpenHashMap<Entity> = Int2ObjectOpenHashMap()
    private val entityIdMap: Object2IntOpenHashMap<Entity> = Object2IntOpenHashMap()
    private val entityUUIDMap: MutableMap<Entity, UUID> = mutableMapOf()
    private val uuidEntityMap: MutableMap<UUID, Entity> = mutableMapOf()
    private val entities: MutableSet<Entity> = mutableSetOf()

    val lock = SimpleLock()

    val size: Int
        get() = entities.size


    fun add(entityId: Int?, entityUUID: UUID?, entity: Entity) {
        check(entityId != null || entityUUID != null) { "Entity id and UUID is null!" }
        try {
            lock.lock()
            entities += entity
            if (entityId != null) {
                idEntityMap[entityId] = entity
                entityIdMap[entity] = entityId
            }
            if (entityUUID != null) {
                uuidEntityMap[entityUUID] = entity
                entityUUIDMap[entity] = entityUUID
            }
        } finally {
            lock.unlock()
        }
    }

    operator fun get(id: Int): Entity? {
        try {
            lock.acquire()
            return idEntityMap[id]
        } finally {
            lock.release()
        }
    }

    fun getId(entity: Entity): Int? {
        try {
            lock.acquire()
            return entityIdMap[entity]
        } finally {
            lock.release()
        }
    }

    operator fun get(uuid: UUID): Entity? {
        try {
            lock.acquire()
            return uuidEntityMap[uuid]
        } finally {
            lock.release()
        }
    }

    fun getUUID(entity: Entity): UUID? {
        try {
            lock.acquire()
            return entityUUIDMap[entity]
        } finally {
            lock.release()
        }
    }

    fun remove(entity: Entity) {
        lock.lock()
        if (!entities.remove(entity)) {
            lock.unlock()
            return
        }
        val id = entityIdMap.remove(entity)
        if (id != null) {
            idEntityMap.remove(id)
        }
        val uuid = entityUUIDMap.remove(entity)
        if (uuid != null) {
            uuidEntityMap.remove(uuid)
        }
        lock.unlock()
    }

    fun remove(entityId: Int) {
        lock.lock()
        val entity = idEntityMap.remove(entityId)
        if (entity == null) {
            lock.unlock()
            return
        }
        entities.remove(entity)
        entityIdMap.remove(entity)
        val uuid = entityUUIDMap.remove(entity)
        if (uuid != null) {
            uuidEntityMap.remove(uuid)
        }
        lock.unlock()
    }

    override fun iterator(): Iterator<Entity> {
        return entities.iterator()
    }

    fun getInRadius(position: Vec3d, distance: Double, check: (Entity) -> Boolean): List<Entity> {
        // ToDo: Improve performance
        val entities: MutableList<Entity> = mutableListOf()
        lock.acquire()

        for (entity in this) {
            if ((entity.position - position).length() > distance) {
                continue
            }
            if (check(entity)) {
                entities += entity
            }
        }
        lock.release()
        return entities
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

    fun isEntityIn(shape: VoxelShape): Boolean {
        try {
            lock.acquire()
            for (entity in this) {
                if (entity.isInvisible) {
                    continue
                }
                val aabb = entity.aabb

                if (shape.intersect(aabb)) {
                    return true
                }
            }
        } finally {
            lock.release()
        }
        return false
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
