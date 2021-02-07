package de.bixilon.minosoft.gui.rendering

import de.bixilon.minosoft.data.Directions
import de.bixilon.minosoft.data.mappings.blocks.Block
import de.bixilon.minosoft.data.world.ChunkLocation
import de.bixilon.minosoft.data.world.ChunkSection
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition

object ChunkPreparer {
    fun prepareChunk(world: World, chunkLocation: ChunkLocation, sectionHeight: Int, section: ChunkSection): FloatArray {
        val data: MutableList<Float> = mutableListOf()

        // ToDo: Greedy meshing!

        val below = world.allChunks[chunkLocation]?.sections?.get(sectionHeight - 1)
        val above = world.allChunks[chunkLocation]?.sections?.get(sectionHeight + 1)
        //val north = (world.allChunks[chunkLocation.getLocationByDirection(Directions.NORTH)]?: throw ChunkNotLoadedException("North not loaded")).sections?.get(sectionHeight)
        //val south = (world.allChunks[chunkLocation.getLocationByDirection(Directions.SOUTH)]?: throw ChunkNotLoadedException("South not loaded")).sections?.get(sectionHeight)
        //val west = (world.allChunks[chunkLocation.getLocationByDirection(Directions.WEST)]?: throw ChunkNotLoadedException("West not loaded")).sections?.get(sectionHeight)
        //val east = (world.allChunks[chunkLocation.getLocationByDirection(Directions.EAST)]?: throw ChunkNotLoadedException("North not loaded")).sections?.get(sectionHeight)
        val north = world.allChunks[chunkLocation.getLocationByDirection(Directions.NORTH)]?.sections?.get(sectionHeight)
        val south = world.allChunks[chunkLocation.getLocationByDirection(Directions.SOUTH)]?.sections?.get(sectionHeight)
        val west = world.allChunks[chunkLocation.getLocationByDirection(Directions.WEST)]?.sections?.get(sectionHeight)
        val east = world.allChunks[chunkLocation.getLocationByDirection(Directions.EAST)]?.sections?.get(sectionHeight)

        for ((position, block) in section.blocks) {
            val blockBelow: Block? = if (position.y == 0 && below != null) {
                below.getBlock(position.x, ProtocolDefinition.SECTION_HEIGHT_Y - 1, position.z)
            } else {
                section.getBlock(position.getLocationByDirection(Directions.DOWN))
            }
            val blockAbove: Block? = if (position.y == ProtocolDefinition.SECTION_HEIGHT_Y - 1 && above != null) {
                above.getBlock(position.x, 0, position.z)
            } else {
                section.getBlock(position.getLocationByDirection(Directions.UP))
            }
            val blockNorth: Block? = if (position.z == 0 && north != null) {
                north.getBlock(position.x, position.y, ProtocolDefinition.SECTION_WIDTH_Z - 1)
            } else {
                section.getBlock(position.getLocationByDirection(Directions.NORTH))
            }
            val blockSouth: Block? = if (position.z == ProtocolDefinition.SECTION_WIDTH_Z - 1 && south != null) {
                south.getBlock(position.x, position.y, 0)
            } else {
                section.getBlock(position.getLocationByDirection(Directions.SOUTH))
            }
            val blockWest: Block? = if (position.x == 0 && west != null) {
                west.getBlock(ProtocolDefinition.SECTION_WIDTH_X - 1, position.y, position.x)
            } else {
                section.getBlock(position.getLocationByDirection(Directions.WEST))
            }
            val blockEast: Block? = if (position.x == ProtocolDefinition.SECTION_WIDTH_X - 1 && east != null) {
                east.getBlock(0, position.y, position.x)
            } else {
                section.getBlock(position.getLocationByDirection(Directions.EAST))
            }

            fun drawBlock() {
                block.blockModel.render(position, data, arrayOf(blockBelow, blockAbove, blockNorth, blockSouth, blockWest, blockEast))
            }
            drawBlock()
        }
        return data.toFloatArray()
    }
}
