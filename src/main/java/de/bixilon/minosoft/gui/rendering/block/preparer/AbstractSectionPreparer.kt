package de.bixilon.minosoft.gui.rendering.block.preparer

import de.bixilon.minosoft.data.world.ChunkSection
import de.bixilon.minosoft.gui.rendering.block.mesh.ChunkSectionMeshes
import glm_.vec2.Vec2i

interface AbstractSectionPreparer {

    fun prepare(chunkPosition: Vec2i, sectionHeight: Int, section: ChunkSection, neighbours: Array<ChunkSection?>): ChunkSectionMeshes

}
