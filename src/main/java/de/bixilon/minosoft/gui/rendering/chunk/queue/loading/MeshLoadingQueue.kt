/*
 * Minosoft
 * Copyright (C) 2020-2026 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.chunk.queue.loading

import de.bixilon.kutil.concurrent.lock.Lock
import de.bixilon.kutil.concurrent.lock.LockUtil.locked
import de.bixilon.kutil.profiler.stack.StackedProfiler.Companion.invoke
import de.bixilon.kutil.time.TimeUtil.now
import de.bixilon.kutil.unit.Bytes.Companion.bytes
import de.bixilon.kutil.unit.Bytes.Companion.gigabytes
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.SectionPosition
import de.bixilon.minosoft.gui.rendering.chunk.ChunkRenderer
import de.bixilon.minosoft.gui.rendering.chunk.mesh.ChunkMeshes
import de.bixilon.minosoft.gui.rendering.chunk.util.ChunkRendererUtil.maxBusyTime

class MeshLoadingQueue(
    private val renderer: ChunkRenderer,
) {
    private val comparator = LoadingQueueComparator()
    private val meshes = ArrayDeque<ChunkMeshes>(100)
    private val positions: MutableSet<SectionPosition> = HashSet()
    private val lock = Lock.lock()


    val max = if (Runtime.getRuntime().maxMemory().bytes > 1.gigabytes) 120 else 60
    val size get() = meshes.size


    fun sort() = lock.locked {
        comparator.update(renderer.visibility.eyePosition)
        meshes.sortWith(comparator)
    }

    fun work() {
        if (meshes.isEmpty()) return
        val start = now()
        val maxTime = renderer.maxBusyTime

        var index = 0
        lock.locked {
            while (meshes.isNotEmpty()) {
                if (++index % BATCH_SIZE == 0 && now() - start >= maxTime) break

                val mesh = this.meshes.removeFirst()
                this.positions -= mesh.position

                renderer.context.profiler("load$index") { mesh.load() }

                renderer.loaded += mesh
            }
        }
    }


    operator fun plusAssign(mesh: ChunkMeshes) = lock.locked {
        this.meshes += mesh
        sort()

        this.positions += mesh.position
    }


    fun removeIf(requeue: Boolean, predicate: (position: SectionPosition) -> Boolean) = lock.locked {
        val iterator = meshes.iterator()
        while (iterator.hasNext()) {
            val mesh = iterator.next()
            if (!predicate.invoke(mesh.position)) continue

            iterator.remove()
            this.positions -= mesh.position

            mesh.drop()

            if (requeue) {
                renderer.invalidate(mesh.section)
            } else {
                renderer.cache -= mesh.position
            }
        }
    }

    operator fun minusAssign(position: ChunkPosition) = removeIf(false) { it.chunkPosition == position }

    operator fun minusAssign(position: SectionPosition) = lock.locked {
        if (!this.positions.remove(position)) return@locked

        val iterator = meshes.iterator()
        while (iterator.hasNext()) {
            val mesh = iterator.next()
            if (mesh.position != position) continue

            iterator.remove()

            mesh.drop()
            renderer.cache -= mesh.position
            break
        }
    }

    fun clear() = lock.locked {
        for (mesh in meshes) {
            mesh.drop()
            renderer.cache -= mesh.position
        }
        meshes.clear()
        positions.clear()
    }

    companion object {
        const val BATCH_SIZE = 5
    }
}
