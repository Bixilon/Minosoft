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

package de.bixilon.minosoft.gui.rendering.world

import de.bixilon.kutil.concurrent.pool.ThreadPoolRunnable
import de.bixilon.minosoft.gui.rendering.world.mesh.WorldMesh
import de.bixilon.minosoft.gui.rendering.world.preparer.FluidSectionPreparer
import de.bixilon.minosoft.gui.rendering.world.preparer.SolidSectionPreparer
import de.bixilon.minosoft.gui.rendering.world.preparer.cull.FluidCullSectionPreparer
import de.bixilon.minosoft.gui.rendering.world.preparer.cull.SolidCullSectionPreparer
import de.bixilon.minosoft.gui.rendering.world.queue.meshing.tasks.MeshPrepareTask
import de.bixilon.minosoft.gui.rendering.world.util.WorldRendererUtil.smallMesh

class ChunkMesher(
    private val renderer: WorldRenderer,
) {
    private val solidSectionPreparer: SolidSectionPreparer = SolidCullSectionPreparer(renderer.renderWindow)
    private val fluidSectionPreparer: FluidSectionPreparer = FluidCullSectionPreparer(renderer.renderWindow)

    private fun mesh(item: WorldQueueItem): WorldMesh? {
        if (item.section.blocks.isEmpty) {
            renderer.queueItemUnload(item)
            return null
        }
        val mesh = WorldMesh(renderer.renderWindow, item.chunkPosition, item.sectionHeight, item.section.smallMesh)
        solidSectionPreparer.prepareSolid(item.chunkPosition, item.sectionHeight, item.chunk, item.section, item.neighbours, item.chunkNeighbours, mesh)

        if (item.section.blocks.fluidCount > 0) {
            fluidSectionPreparer.prepareFluid(item.chunkPosition, item.sectionHeight, item.chunk, item.section, item.neighbours, item.chunkNeighbours, mesh)
        }

        return mesh
    }

    private fun mesh(item: WorldQueueItem, runnable: ThreadPoolRunnable) {
        val mesh = mesh(item) ?: return
        runnable.interruptable = false
        if (Thread.interrupted()) return
        if (mesh.clearEmpty() == 0) {
            return renderer.queueItemUnload(item)
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
