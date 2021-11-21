package de.bixilon.minosoft.gui.rendering.world

import de.bixilon.minosoft.data.world.Chunk
import de.bixilon.minosoft.data.world.ChunkSection
import de.bixilon.minosoft.gui.rendering.world.mesh.ChunkSectionMeshes
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import java.util.*

class WorldQueueItem(
    val chunkPosition: Vec2i,
    val sectionHeight: Int,
    val chunk: Chunk?,
    val section: ChunkSection?,
    val center: Vec3,
    val neighbours: Array<ChunkSection?>?,
) {
    var mesh: ChunkSectionMeshes? = null

    override fun equals(other: Any?): Boolean {
        if (other !is WorldQueueItem) {
            return false
        }

        return chunkPosition == other.chunkPosition && sectionHeight == other.sectionHeight
    }

    override fun hashCode(): Int {
        return Objects.hash(chunkPosition, sectionHeight)
    }
}
