package de.bixilon.minosoft.gui.rendering.block.preparer

import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.world.ChunkSection
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.block.mesh.ChunkSectionMesh
import de.bixilon.minosoft.gui.rendering.util.VecUtil
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import glm_.vec3.Vec3i
import java.util.*

class CullSectionPreparer(
    val renderWindow: RenderWindow,
) : AbstractSectionPreparer {

    override fun prepare(section: ChunkSection): ChunkSectionMesh {
        val mesh = ChunkSectionMesh(renderWindow)
        val random = Random(0L)

        var block: BlockState?
        val neighbours: Array<BlockState?> = arrayOfNulls(Directions.SIZE)

        for (x in 0 until ProtocolDefinition.SECTION_WIDTH_X) {
            for (y in 0 until ProtocolDefinition.SECTION_HEIGHT_Y) {
                for (z in 0 until ProtocolDefinition.SECTION_WIDTH_Z) {
                    block = section.blocks[ChunkSection.getIndex(x, y, z)]

                    // ToDo: Chunk borders
                    neighbours[Directions.DOWN.ordinal] = if (y == 0) {
                        null
                    } else {
                        section.blocks[ChunkSection.getIndex(x, y - 1, z)]
                    }
                    neighbours[Directions.UP.ordinal] = if (y == ProtocolDefinition.SECTION_MAX_Y) {
                        null
                    } else {
                        section.blocks[ChunkSection.getIndex(x, y + 1, z)]
                    }
                    neighbours[Directions.NORTH.ordinal] = if (z == 0) {
                        null
                    } else {
                        section.blocks[ChunkSection.getIndex(x, y, z - 1)]
                    }
                    neighbours[Directions.SOUTH.ordinal] = if (z == ProtocolDefinition.SECTION_MAX_Z) {
                        null
                    } else {
                        section.blocks[ChunkSection.getIndex(x, y, z + 1)]
                    }
                    neighbours[Directions.WEST.ordinal] = if (x == 0) {
                        null
                    } else {
                        section.blocks[ChunkSection.getIndex(x - 1, y, z)]
                    }
                    neighbours[Directions.EAST.ordinal] = if (x == ProtocolDefinition.SECTION_MAX_X) {
                        null
                    } else {
                        section.blocks[ChunkSection.getIndex(x + 1, y, z)]
                    }

                    random.setSeed(VecUtil.generatePositionHash(x, y, z))
                    block?.model?.singleRender(Vec3i(x, y, z), mesh, random, neighbours, 0xFF, floatArrayOf(1.0f, 1.0f, 1.0f, 1.0f))
                }
            }
        }


        return mesh
    }
}
