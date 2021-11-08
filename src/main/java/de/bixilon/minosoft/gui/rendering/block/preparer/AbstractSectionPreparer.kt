package de.bixilon.minosoft.gui.rendering.block.preparer

import de.bixilon.minosoft.data.world.ChunkSection
import de.bixilon.minosoft.gui.rendering.block.mesh.ChunkSectionMesh

interface AbstractSectionPreparer {

    fun prepare(section: ChunkSection): ChunkSectionMesh

}
