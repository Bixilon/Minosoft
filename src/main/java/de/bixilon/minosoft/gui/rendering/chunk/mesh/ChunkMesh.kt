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

package de.bixilon.minosoft.gui.rendering.chunk.mesh

import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.VertexBuffer
import de.bixilon.minosoft.gui.rendering.system.base.query.RenderQuery
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh

class ChunkMesh(
    buffer: VertexBuffer,
    val query: RenderQuery,
) : Mesh(buffer), Comparable<ChunkMesh> {
    var distance: Int = 0
    var occlusion = OcclusionStates.MAYBE

    override fun compareTo(other: ChunkMesh): Int {
        if (distance < other.distance) return -1
        if (distance > other.distance) return 1
        return 0
    }

    override fun load() {
        super.load()
        query.init()
    }

    override fun draw() {
        if (occlusion == OcclusionStates.INVISIBLE) return

        val query = occlusion == OcclusionStates.MAYBE

        if (query) this.query.begin()
        super.draw()
        if (query) this.query.end()
    }

    fun updateOcclusion() {
        if (occlusion != OcclusionStates.MAYBE) return
        if (query.recordings == 0) return

        query.collect()
        occlusion = if (query.result > 0) OcclusionStates.VISIBLE else OcclusionStates.INVISIBLE
    }

    override fun unload() {
        super.unload()
        query.destroy()
    }

    enum class OcclusionStates {
        INVISIBLE,
        VISIBLE,
        MAYBE,
    }
}
