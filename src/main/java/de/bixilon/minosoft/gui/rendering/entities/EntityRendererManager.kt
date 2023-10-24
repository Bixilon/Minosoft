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

package de.bixilon.minosoft.gui.rendering.entities

import de.bixilon.kutil.collections.iterator.async.ConcurrentIterator
import de.bixilon.kutil.collections.map.LockMap
import de.bixilon.kutil.concurrent.lock.simple.SimpleLock
import de.bixilon.kutil.concurrent.pool.ThreadPool
import de.bixilon.kutil.observer.set.SetObserver.Companion.observeSet
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.gui.rendering.entities.renderer.EntityRenderer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class EntityRendererManager(val renderer: EntitiesRenderer) : Iterable<EntityRenderer<*>> {
    val lock = SimpleLock()
    private val renderers: LockMap<Entity, EntityRenderer<*>> = LockMap(HashMap(), lock)


    val size: Int get() = renderers.size

    fun init() {
        renderer.connection.world.entities::entities.observeSet(this) {
            for (entity in it.adds) {
                this += entity
            }
            for (entity in it.removes) {
                this -= entity
            }
        }
    }

    operator fun plusAssign(entity: Entity) = add(entity)
    fun add(entity: Entity) {
        try {
            renderers.lock.lock()
            val renderer = entity.createRenderer(this.renderer) ?: return
            entity.renderer?.let { onReplace(it) }
            this.renderers.unsafe.put(entity, renderer)?.let { onReplace(it) }
        } finally {
            renderers.lock.unlock()
        }
    }

    @Deprecated("That should never ever happen!")
    private fun onReplace(renderer: EntityRenderer<*>) {
        Log.log(LogMessageType.RENDERING, LogLevels.WARN) { "Entity renderer of ${renderer.entity} just got replaced???" }
        unload(renderer)
    }

    operator fun minusAssign(entity: Entity) = remove(entity)
    fun remove(entity: Entity) {
        renderers.lock.lock()
        val previous = renderers.unsafe.remove(entity)
        entity.renderer = null
        renderers.lock.unlock()
        previous?.let { unload(it) }
    }


    fun unload(renderer: EntityRenderer<*>) {
        this.renderer.queue += { renderer.unload() }
    }

    override fun iterator(): Iterator<EntityRenderer<*>> {
        return renderers.unsafe.values.iterator()
    }

    fun iterate(executor: ((EntityRenderer<*>) -> Unit)) {
        lock.acquire()
        ConcurrentIterator(renderers.unsafe.values.spliterator(), priority = ThreadPool.HIGHER).iterate(executor)
        lock.release()
    }
}
