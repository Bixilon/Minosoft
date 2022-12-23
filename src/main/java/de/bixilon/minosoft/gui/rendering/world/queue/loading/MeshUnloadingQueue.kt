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

package de.bixilon.minosoft.gui.rendering.world.queue.loading

import de.bixilon.kutil.concurrent.lock.simple.SimpleLock
import de.bixilon.kutil.time.TimeUtil
import de.bixilon.minosoft.gui.rendering.world.WorldRenderer
import de.bixilon.minosoft.gui.rendering.world.mesh.WorldMesh
import de.bixilon.minosoft.gui.rendering.world.queue.QueuePosition
import de.bixilon.minosoft.gui.rendering.world.util.WorldRendererUtil.maxBusyTime

class MeshUnloadingQueue(
    private val renderer: WorldRenderer,
) {
    private val meshes: MutableList<WorldMesh> = mutableListOf() // prepared meshes, that can be loaded in the (next) frame
    private val positions: MutableSet<QueuePosition> = mutableSetOf()
    private val lock = SimpleLock()


    private fun forceWork() {
        if (meshes.isEmpty()) {
            return
        }

        val time = TimeUtil.millis()
        val maxTime = renderer.maxBusyTime

        while (meshes.isNotEmpty() && (TimeUtil.millis() - time < maxTime)) {
            val mesh = meshes.removeAt(0)
            this.positions -= QueuePosition(mesh)
            renderer.visible.removeMesh(mesh)
            mesh.unload()
        }
    }

    fun work() {
        lock()
        try {
            forceWork()
        } finally {
            unlock()
        }
    }

    fun forceQueue(mesh: WorldMesh, lock: Boolean = true) {
        if (lock) lock()

        if (mesh.chunkPosition == renderer.connection.player.positionInfo.chunkPosition) { // TODO: camera
            this.meshes.add(0, mesh)
        } else {
            this.meshes += mesh
        }
        this.positions += QueuePosition(mesh)

        if (lock) unlock()
    }

    fun queue(mesh: WorldMesh, lock: Boolean = true) {
        if (lock) lock()
        if (QueuePosition(mesh) in this.positions) {
            // already queued
            // TODO: maybe camera chunk position changed?
            unlock()
            return
        }
        forceQueue(mesh, false)
        if (lock) unlock()
    }

    fun forceQueue(meshes: Collection<WorldMesh>, lock: Boolean = true) {
        if (lock) lock()
        for (mesh in meshes) {
            forceQueue(mesh, false)
        }
        if (lock) unlock()
    }

    fun queue(meshes: Collection<WorldMesh>) {
        lock()
        for (mesh in meshes) {
            queue(mesh, false)
        }
        unlock()
    }

    fun lock() {
        renderer.lock.acquire()
        this.lock.lock()
    }

    fun unlock() {
        this.lock.unlock()
        renderer.lock.release()
    }
}
