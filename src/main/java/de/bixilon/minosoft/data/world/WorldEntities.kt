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

import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.util.KUtil.synchronizedMapOf
import java.util.*

class WorldEntities : Iterable<Entity> {
    private val idEntityMap: MutableMap<Int, Entity> = synchronizedMapOf()
    private val entityIdMap: MutableMap<Entity, Int> = synchronizedMapOf()
    private val entityUUIDMap: MutableMap<Entity, UUID> = synchronizedMapOf()
    private val uuidEntityMap: MutableMap<UUID, Entity> = synchronizedMapOf()


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
        return entityIdMap[entity]!!
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
        return idEntityMap.values.iterator()
    }
}
