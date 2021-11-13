package de.bixilon.minosoft.gui.rendering.block.preparer

import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.world.ChunkSection
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.block.mesh.ChunkSectionMeshes
import de.bixilon.minosoft.gui.rendering.util.VecUtil
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import glm_.vec2.Vec2i
import glm_.vec3.Vec3i
import java.util.*

class CullSectionPreparer(
    val renderWindow: RenderWindow,
) : AbstractSectionPreparer {
    private val ambientLight = floatArrayOf(1.0f, 1.0f, 1.0f, 1.0f)

    override fun prepare(chunkPosition: Vec2i, sectionHeight: Int, section: ChunkSection, neighbours: Array<ChunkSection?>): ChunkSectionMeshes {
        val mesh = ChunkSectionMeshes(renderWindow, chunkPosition, sectionHeight)
        val random = Random(0L)

        val blocks = section.blocks
        var block: BlockState?
        val neighbourBlocks: Array<BlockState?> = arrayOfNulls(Directions.SIZE)

        val offsetX = chunkPosition.x * ProtocolDefinition.SECTION_WIDTH_X
        val offsetY = sectionHeight * ProtocolDefinition.SECTION_HEIGHT_Y
        val offsetZ = chunkPosition.y * ProtocolDefinition.SECTION_WIDTH_Z

        for (x in 0 until ProtocolDefinition.SECTION_WIDTH_X) {
            for (y in 0 until ProtocolDefinition.SECTION_HEIGHT_Y) {
                for (z in 0 until ProtocolDefinition.SECTION_WIDTH_Z) {
                    block = blocks[ChunkSection.getIndex(x, y, z)]
                    val model = block?.model ?: continue

                    // ToDo: Chunk borders
                    neighbourBlocks[Directions.DOWN.ordinal] = if (y == 0) {
                        neighbours[Directions.DOWN.ordinal]?.blocks?.get(ChunkSection.getIndex(x, ProtocolDefinition.SECTION_MAX_Y, z))
                    } else {
                        blocks[ChunkSection.getIndex(x, y - 1, z)]
                    }
                    neighbourBlocks[Directions.UP.ordinal] = if (y == ProtocolDefinition.SECTION_MAX_Y) {
                        neighbours[Directions.UP.ordinal]?.blocks?.get(ChunkSection.getIndex(x, 0, z))
                    } else {
                        blocks[ChunkSection.getIndex(x, y + 1, z)]
                    }

                    neighbourBlocks[Directions.NORTH.ordinal] = if (z == 0) {
                        neighbours[Directions.NORTH.ordinal]?.blocks?.get(ChunkSection.getIndex(x, y, ProtocolDefinition.SECTION_MAX_Z))
                    } else {
                        blocks[ChunkSection.getIndex(x, y, z - 1)]
                    }
                    neighbourBlocks[Directions.SOUTH.ordinal] = if (z == ProtocolDefinition.SECTION_MAX_Z) {
                        neighbours[Directions.SOUTH.ordinal]?.blocks?.get(ChunkSection.getIndex(x, y, 0))
                    } else {
                        blocks[ChunkSection.getIndex(x, y, z + 1)]
                    }

                    neighbourBlocks[Directions.WEST.ordinal] = if (x == 0) {
                        neighbours[Directions.WEST.ordinal]?.blocks?.get(ChunkSection.getIndex(ProtocolDefinition.SECTION_MAX_X, y, z))
                    } else {
                        blocks[ChunkSection.getIndex(x - 1, y, z)]
                    }
                    neighbourBlocks[Directions.EAST.ordinal] = if (x == ProtocolDefinition.SECTION_MAX_X) {
                        neighbours[Directions.EAST.ordinal]?.blocks?.get(ChunkSection.getIndex(0, y, z))
                    } else {
                        blocks[ChunkSection.getIndex(x + 1, y, z)]
                    }

                    val position = Vec3i(offsetX + x, offsetY + y, offsetZ + z)
                    random.setSeed(VecUtil.generatePositionHash(position.x, position.y, position.z))
                    val rendered = model.singleRender(position, mesh, random, block, neighbourBlocks, 0xFF, ambientLight)
                    if (rendered) {
                        mesh.addBlock(x, y, z)
                    }
                }
            }
        }


        return mesh
    }
}
