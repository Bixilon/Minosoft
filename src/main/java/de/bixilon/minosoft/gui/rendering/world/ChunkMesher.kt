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

package de.bixilon.minosoft.gui.rendering.world

import de.bixilon.kutil.concurrent.pool.ThreadPoolRunnable
import de.bixilon.minosoft.gui.rendering.world.mesh.WorldMesh
import de.bixilon.minosoft.gui.rendering.world.preparer.FluidSectionPreparer
import de.bixilon.minosoft.gui.rendering.world.preparer.SolidSectionPreparer
import de.bixilon.minosoft.gui.rendering.world.queue.meshing.tasks.MeshPrepareTask
import de.bixilon.minosoft.gui.rendering.world.util.WorldRendererUtil.smallMesh
import de.bixilon.minosoft.util.chunk.ChunkUtil

class ChunkMesher(
    private val renderer: WorldRenderer,
) {
    private val solidSectionPreparer: SolidSectionPreparer = SolidSectionPreparer(renderer.context)
    private val fluidSectionPreparer: FluidSectionPreparer = FluidSectionPreparer(renderer.context)

    private fun mesh(item: WorldQueueItem): WorldMesh? {
        if (item.section.blocks.isEmpty) {
            renderer.unload(item)
            return null
        }
        val neighbours = item.chunk.neighbours.get()
        if (neighbours == null) {
            renderer.unload(item)
            return null
        }
        val sectionNeighbours = ChunkUtil.getDirectNeighbours(neighbours, item.chunk, item.section.sectionHeight)
        val mesh = WorldMesh(renderer.context, item.chunkPosition, item.sectionHeight, item.section.smallMesh)
        solidSectionPreparer.prepareSolid(item.chunkPosition, item.sectionHeight, item.chunk, item.section, neighbours, sectionNeighbours, mesh)

        if (item.section.blocks.fluidCount > 0) {
            fluidSectionPreparer.prepareFluid(item.chunkPosition, item.sectionHeight, item.chunk, item.section, neighbours, sectionNeighbours, mesh)
        }

        return mesh
    }

    private fun mesh(item: WorldQueueItem, runnable: ThreadPoolRunnable) {
        val mesh = mesh(item) ?: return
        runnable.interruptable = false
        if (Thread.interrupted()) return
        if (mesh.clearEmpty() == 0) {
            return renderer.unload(item)
        }
        mesh.finish()
        item.mesh = mesh
        renderer.loadingQueue.queue(mesh)
    }

    fun tryMesh(item: WorldQueueItem, task: MeshPrepareTask, runnable: ThreadPoolRunnable) {
        try {
            mesh(item, runnable)
        } catch (ignored: InterruptedException) {
        } finally {
            task.runnable.interruptable = false
            if (Thread.interrupted()) throw InterruptedException()
            renderer.meshingQueue.tasks -= task
            renderer.meshingQueue.work()
        }
    }
}
