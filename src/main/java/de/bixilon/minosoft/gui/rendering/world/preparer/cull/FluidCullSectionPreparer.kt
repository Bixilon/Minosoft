package de.bixilon.minosoft.gui.rendering.world.preparer.cull

import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.types.FluidBlock
import de.bixilon.minosoft.data.registries.blocks.types.FluidFillable
import de.bixilon.minosoft.data.registries.fluid.DefaultFluids
import de.bixilon.minosoft.data.world.Chunk
import de.bixilon.minosoft.data.world.ChunkSection
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.world.mesh.WorldMesh
import de.bixilon.minosoft.gui.rendering.world.preparer.FluidSectionPreparer
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.chunk.ChunkUtil.acquire
import de.bixilon.minosoft.util.chunk.ChunkUtil.release
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import glm_.vec2.Vec2i
import glm_.vec3.Vec3i

class FluidCullSectionPreparer(
    val renderWindow: RenderWindow,
) : FluidSectionPreparer {
    private val water = renderWindow.connection.registries.fluidRegistry[DefaultFluids.WATER]
    private val tintColorCalculator = renderWindow.tintManager


    override fun prepareFluid(chunkPosition: Vec2i, sectionHeight: Int, chunk: Chunk, section: ChunkSection, neighbours: Array<ChunkSection?>, neighbourChunks: Array<Chunk>): WorldMesh? {
        val mesh = WorldMesh(renderWindow, chunkPosition, sectionHeight, smallMesh = true)

        val isLowestSection = sectionHeight == chunk.lowestSection
        val isHighestSection = sectionHeight == chunk.highestSection
        val blocks = section.blocks
        val sectionLight = section.light
        section.acquire()
        neighbours.acquire()

        var blockState: BlockState
        var position: Vec3i
        var rendered = false
        var tints: IntArray?

        val offsetX = chunkPosition.x * ProtocolDefinition.SECTION_WIDTH_X
        val offsetY = sectionHeight * ProtocolDefinition.SECTION_HEIGHT_Y
        val offsetZ = chunkPosition.y * ProtocolDefinition.SECTION_WIDTH_Z

        for (x in 0 until ProtocolDefinition.SECTION_WIDTH_X) {
            for (y in 0 until ProtocolDefinition.SECTION_HEIGHT_Y) {
                for (z in 0 until ProtocolDefinition.SECTION_WIDTH_Z) {
                    blockState = blocks.unsafeGet(x, y, z) ?: continue
                    val block = blockState.block
                    val fluid = when {
                        block is FluidBlock -> (blockState.block as FluidBlock).fluid
                        blockState.properties[BlockProperties.WATERLOGGED] == true && water != null -> water
                        block is FluidFillable -> block.fluid
                        else -> continue
                    }
                    Log.log(LogMessageType.OTHER, LogLevels.VERBOSE) { "Can not render fluid: $fluid" }


                    if (rendered) {
                        mesh.addBlock(x, y, z)
                    }
                }
            }
        }
        section.release()
        neighbours.release()

        if (mesh.clearEmpty() == 0) {
            return null
        }

        return mesh
    }

}
