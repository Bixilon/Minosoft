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

package de.bixilon.minosoft.gui.rendering.chunk.mesher

import de.bixilon.kutil.concurrent.pool.runnable.InterruptableRunnable
import de.bixilon.minosoft.gui.rendering.chunk.ChunkRenderer
import de.bixilon.minosoft.gui.rendering.chunk.WorldQueueItem
import de.bixilon.minosoft.gui.rendering.chunk.mesh.ChunkMeshes
import de.bixilon.minosoft.gui.rendering.chunk.mesh.ChunkMeshesBuilder
import de.bixilon.minosoft.gui.rendering.chunk.queue.meshing.tasks.MeshPrepareTask
import de.bixilon.minosoft.gui.rendering.chunk.util.ChunkRendererUtil.smallMesh

class ChunkMesher(
    private val renderer: ChunkRenderer,
) {
    private val solid = SolidSectionMesher(renderer.context)
    private val fluid = FluidSectionMesher(renderer.context)

    private fun mesh(item: WorldQueueItem): ChunkMeshes? {
        if (item.section.blocks.isEmpty) {
            return null
        }
        val neighbours = item.chunk.neighbours
        val sectionNeighbours = item.section.neighbours
        if (!neighbours.complete || sectionNeighbours == null) {
            return null
        }
        val mesh = ChunkMeshesBuilder(renderer.context, item.section.smallMesh)
        try {
            solid.mesh(item.position, item.chunk, item.section, neighbours.neighbours, sectionNeighbours, mesh)

            if (item.section.blocks.hasFluid) {
                fluid.mesh(item.position, item.chunk, item.section, mesh)
            }
        } catch (exception: Exception) {
            mesh.drop()
            throw exception
        }

        return mesh.build(item.position)
    }

    private fun mesh(item: WorldQueueItem, runnable: InterruptableRunnable) {
        val mesh = mesh(item)
        runnable.interruptable = false
        if (mesh == null) {
            return renderer.unload(item)
        }
        if (Thread.interrupted()) throw InterruptedException()
        mesh.preload()
        item.mesh = mesh
        renderer.loadingQueue.queue(mesh)
    }

    fun tryMesh(item: WorldQueueItem, task: MeshPrepareTask, runnable: InterruptableRunnable) {
        try {
            mesh(item, runnable)
        } catch (ignored: InterruptedException) {
            renderer.meshingQueue.queue(item)
        } finally {
            task.runnable.interruptable = false
            Thread.interrupted() // clear interrupted flag
            renderer.meshingQueue.tasks -= task
            renderer.meshingQueue.work()
        }
    }
}
