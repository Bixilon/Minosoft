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
            for (direction in Directions.DIRECTIONS) {
                var blockToCheck: Block? = null
                when (direction) {
                    Directions.DOWN -> {
                        if (position.y == 0) {
                            below?.let {
                                blockToCheck = it.getBlock(position.x, ProtocolDefinition.SECTION_HEIGHT_Y - 1, position.z)
                            }
                        }
                    }
                    Directions.UP -> {
                        if (position.y == ProtocolDefinition.SECTION_HEIGHT_Y - 1) {
                            above?.let {
                                blockToCheck = it.getBlock(position.x, 0, position.z)
                            }
                        }
                    }
                    Directions.NORTH -> {
                        if (position.z == 0) {
                            north?.let {
                                blockToCheck = it.getBlock(position.x, position.y, ProtocolDefinition.SECTION_WIDTH_Z - 1)
                            }
                        }
                    }
                    Directions.SOUTH -> {
                        if (position.z == ProtocolDefinition.SECTION_WIDTH_Z - 1) {
                            south?.let {
                                blockToCheck = it.getBlock(position.x, position.y, 0)
                            }
                        }
                    }
                    Directions.WEST -> {
                        if (position.x == 0) {
                            west?.let {
                                blockToCheck = it.getBlock(ProtocolDefinition.SECTION_WIDTH_X - 1, position.y, position.z)
                            }
                        }
                    }
                    Directions.EAST -> {
                        if (position.x == ProtocolDefinition.SECTION_WIDTH_X - 1) {
                            east?.let {
                                blockToCheck = it.getBlock(0, position.y, position.z)
                            }
                        }
                    }
                }

                if (blockToCheck == null) {
                    blockToCheck = section.getBlock(position.getLocationByDirection(direction))
                }
                if (blockToCheck != null) {
                    if (blockToCheck!!.blockModel.full && block.blockModel.full) {
                        continue
                    }
                    //  if (block.forceDrawFace(direction.inverse())) {
                    //continue
                    // }
                }
                data.addAll(block.blockModel.render(position, direction))
            }
        }
        return data.toFloatArray()
    }
}
