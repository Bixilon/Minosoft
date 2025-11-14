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

package de.bixilon.minosoft.gui.rendering.chunk.visible

import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.minosoft.data.world.positions.SectionPosition
import de.bixilon.minosoft.gui.rendering.chunk.ChunkRenderer

class ChunkVisibilityManager(val renderer: ChunkRenderer) {
    private var camera = Vec3f.EMPTY
    private var sectionPosition = SectionPosition()

    private var invalid = true


    val visibility = context.camera.visibility
    private var previousViewDistance = session.world.view.viewDistance


    var visible = VisibleMeshes() // This name might be confusing. Those faces are from blocks.


    private fun onVisibilityChange() {
        var sort = false
        val blockPosition = session.camera.entity.physics.positionInfo.position
        val cameraPosition = Vec3f(blockPosition - context.camera.offset.offset)
        val sectionPosition = blockPosition.sectionPosition
        if (this.cameraPosition != cameraPosition) {
            if (this.cameraSectionPosition != sectionPosition) {
                this.cameraSectionPosition = sectionPosition
                loaded.updateDetails()
                sort = true
            }
            this.cameraPosition = cameraPosition
        }

        val visible = VisibleMeshes(cameraPosition, this.visible)

        loaded.collect(visible)

        val nextQueue = culledQueue.collect()


        for (section in nextQueue) {
            master.forceQueue(section)
        }

        if (nextQueue.isNotEmpty()) {
            if (sort) {
                meshingQueue.sort()
            }
            meshingQueue.work()
        }

        visible.sort()

        this.visible = visible
    }


    fun invalidate()


    fun onViewDistanceChange() {
        TODO()

        // Unload all chunks(-sections) that are out of view distance
        // TODO: this is junk, do that in inVisibilityChange
        lock.lock()

        loaded.cleanup(false)
        culledQueue.cleanup(false)

        meshingQueue.cleanup(false)

        meshingQueue.tasks.cleanup()
        loadingQueue.cleanup(false)

        lock.unlock()
    }

    fun setViewDistance(distance: Int) {

        val distance = maxOf(viewDistance, profile.simulationDistance)
        if (distance < this.previousViewDistance) {

        } else {
            master.tryQueue(world)
        }

        this.previousViewDistance = distance
    }
}
