package de.bixilon.minosoft.gui.rendering.world.preparer

import de.bixilon.minosoft.data.world.Chunk
import de.bixilon.minosoft.data.world.ChunkSection
import de.bixilon.minosoft.gui.rendering.world.mesh.ChunkSectionMeshes
import glm_.vec2.Vec2i

interface AbstractSectionPreparer {

    fun prepare(chunkPosition: Vec2i, sectionHeight: Int, chunk: Chunk, section: ChunkSection, neighbours: Array<ChunkSection?>, neighbourChunks: Array<Chunk>): ChunkSectionMeshes

}
