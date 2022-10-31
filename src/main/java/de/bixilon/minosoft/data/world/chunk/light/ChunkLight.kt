/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.world.chunk.light

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.world.chunk.Chunk
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.chunk.neighbours.ChunkNeighbours
import de.bixilon.minosoft.gui.rendering.util.VecUtil.inSectionHeight
import de.bixilon.minosoft.gui.rendering.util.VecUtil.sectionHeight
import de.bixilon.minosoft.modding.event.events.blocks.chunk.LightChangeEvent
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition

class ChunkLight(private val chunk: Chunk) {
    private val connection = chunk.connection
    val heightmap = IntArray(ProtocolDefinition.SECTION_WIDTH_X * ProtocolDefinition.SECTION_WIDTH_Z) { Int.MIN_VALUE }

    val bottom = BorderSectionLight(false, chunk)
    val top = BorderSectionLight(true, chunk)


    fun onBlockChange(x: Int, y: Int, z: Int, section: ChunkSection, next: BlockState?) {
        val heightmapIndex = (z shl 4) or x
        val previous = heightmap[heightmapIndex]
        recalculateHeightmap(x, y, z, next)
        onHeightmapUpdate(x, y, z, previous, heightmap[heightmapIndex])

        val neighbours = chunk.neighbours.get() ?: return

        fireLightChange(section, y.sectionHeight, neighbours)
    }


    private fun fireLightChange(section: ChunkSection, sectionHeight: Int, neighbours: Array<Chunk>, fireSameChunkEvent: Boolean = true) {
        if (!section.light.update) {
            return
        }
        section.light.update = false

        val chunkPosition = chunk.chunkPosition
        if (fireSameChunkEvent) {
            connection.fire(LightChangeEvent(connection, chunkPosition, chunk, sectionHeight, true))

            val down = section.neighbours?.get(Directions.O_DOWN)?.light
            if (down != null && down.update) {
                down.update = false
                connection.fire(LightChangeEvent(connection, chunkPosition, chunk, sectionHeight - 1, false))
            }
            val up = section.neighbours?.get(Directions.O_UP)?.light
            if (up?.update == true) {
                up.update = false
                connection.fire(LightChangeEvent(connection, chunkPosition, chunk, sectionHeight + 1, false))
            }
        }


        var neighbourIndex = 0
        for (chunkX in -1..1) {
            for (chunkZ in -1..1) {
                if (chunkX == 0 && chunkZ == 0) {
                    continue
                }
                val nextPosition = chunkPosition + Vec2i(chunkX, chunkZ)
                val chunk = neighbours[neighbourIndex++]
                for (chunkY in -1..1) {
                    val neighbourSection = chunk[sectionHeight + chunkY] ?: continue
                    if (!neighbourSection.light.update) {
                        continue
                    }
                    neighbourSection.light.update = false
                    connection.fire(LightChangeEvent(connection, nextPosition, chunk, sectionHeight + chunkY, false))
                }
            }
        }
    }

    private fun fireLightChange(sections: Array<ChunkSection?>, fireSameChunkEvent: Boolean) {
        val neighbours = chunk.neighbours.get() ?: return
        for ((index, section) in sections.withIndex()) {
            fireLightChange(section ?: continue, index + chunk.lowestSection, neighbours, fireSameChunkEvent)
        }
    }


    operator fun get(position: Vec3i): Int {
        return get(position.x, position.y, position.z)
    }

    operator fun get(x: Int, y: Int, z: Int): Int {
        val sectionHeight = y.sectionHeight
        val inSectionHeight = y.inSectionHeight
        val heightmapIndex = (z shl 4) or x
        val index = inSectionHeight shl 8 or heightmapIndex
        if (sectionHeight == chunk.lowestSection - 1) {
            return bottom[index].toInt()
        }
        if (sectionHeight == chunk.highestSection + 1) {
            return top[index].toInt() or SectionLight.SKY_LIGHT_MASK // top has always sky=15
        }
        var light = chunk[sectionHeight]?.light?.get(index)?.toInt() ?: 0x00
        if (y >= heightmap[heightmapIndex]) {
            // set sky=15
            light = light or SectionLight.SKY_LIGHT_MASK
        }
        return light
    }

    fun recalculate(fireEvent: Boolean = true, fireSameChunkEvent: Boolean = true) {
        bottom.reset()
        top.reset()
        val sections = chunk.sections ?: Broken("Sections is null")
        for (section in sections) {
            if (section == null) {
                continue
            }
            section.light.recalculate()
        }
        calculateSkylight()
        if (fireEvent) {
            fireLightChange(sections, fireSameChunkEvent)
        }
    }

    fun calculate(fireEvent: Boolean = true, fireSameChunkEvent: Boolean = true) {
        val sections = chunk.sections ?: Broken("Sections is null")
        for (section in sections) {
            if (section == null) {
                continue
            }
            section.light.calculate()
        }
        calculateSkylight()
        if (fireEvent) {
            fireLightChange(sections, fireSameChunkEvent)
        }
    }

    fun reset() {
        val sections = chunk.sections ?: Broken("Sections is null")
        for (section in sections) {
            if (section == null) {
                continue
            }
            section.light.reset()
        }
        bottom.reset()
        top.reset()
    }

    fun propagateFromNeighbours(fireEvent: Boolean = true, fireSameChunkEvent: Boolean = true) {
        val sections = chunk.sections ?: Broken("Sections is null")
        for (section in sections) {
            if (section == null) {
                continue
            }
            section.light.propagateFromNeighbours()
        }
        if (fireEvent) {
            fireLightChange(sections, fireSameChunkEvent)
        }
    }

    fun recalculateHeightmap() {
        chunk.lock.lock()
        val maxY = chunk.highestSection * ProtocolDefinition.SECTION_HEIGHT_Y

        for (x in 0 until ProtocolDefinition.SECTION_WIDTH_X) {
            for (z in 0 until ProtocolDefinition.SECTION_WIDTH_Z) {
                checkHeightmapY(x, maxY, z)
            }
        }
        chunk.lock.unlock()
        calculateSkylight()
    }

    private fun checkHeightmapY(x: Int, startY: Int, z: Int) {
        val sections = chunk.sections ?: return

        var y = Int.MIN_VALUE

        sectionLoop@ for (sectionIndex in (startY.sectionHeight - chunk.lowestSection) downTo 0) {
            if (sectionIndex >= sections.size) {
                // starting from above world
                continue
            }
            val section = sections[sectionIndex] ?: continue

            section.acquire()
            for (sectionY in ProtocolDefinition.SECTION_MAX_Y downTo 0) {
                val light = section.blocks.unsafeGet(x, sectionY, z)?.lightProperties ?: continue

                if (light.skylightEnters && !light.filtersSkylight && light.propagatesLight(Directions.DOWN)) {
                    // can go through block
                    continue
                }
                y = (sectionIndex + chunk.lowestSection) * ProtocolDefinition.SECTION_HEIGHT_Y + sectionY
                if (!light.skylightEnters) {
                    y++
                }
                section.release()
                break@sectionLoop
            }
            section.release()
        }
        val heightmapIndex = (z shl 4) or x
        heightmap[heightmapIndex] = y
    }

    private fun onHeightmapUpdate(x: Int, y: Int, z: Int, previous: Int, now: Int) {
        if (previous == now) {
            return
        }

        if (previous < y) {
            // block is now higher
            // ToDo: Neighbours
            val sections = chunk.sections ?: Broken("Sections == null")
            val maxIndex = previous.sectionHeight - chunk.lowestSection
            val minIndex = now.sectionHeight - chunk.lowestSection
            for (index in maxIndex downTo minIndex) {
                val section = sections[index] ?: continue
                section.light.reset()
            }
            for (index in maxIndex downTo minIndex) {
                val section = sections[index] ?: continue
                section.light.calculate()
            }
            calculateSkylight()
        } else if (previous > y) {
            // block is lower
            startSkylightFloodFill(x, z)
        }
    }

    private fun recalculateHeightmap(x: Int, y: Int, z: Int, blockState: BlockState?) {
        chunk.lock.lock()
        val index = (z shl 4) or x

        val current = heightmap[index]

        if (current > y + 1) {
            // our block is/was not the highest, ignore everything
            chunk.lock.unlock()
            return
        }
        if (blockState == null) {
            checkHeightmapY(x, y, z)
            chunk.lock.unlock()
            return
        }

        // we are the highest block now
        // check if light can pass
        val light = blockState.lightProperties
        if (!light.skylightEnters) {
            heightmap[index] = y + 1
        } else if (light.filtersSkylight || !light.propagatesLight(Directions.DOWN)) {
            heightmap[index] = y
        }

        chunk.lock.unlock()
        return
    }

    private fun calculateSkylight() {
        if (chunk.world.dimension?.hasSkyLight != true || !chunk.neighbours.complete) {
            // no need to calculate it
            return
        }
        for (x in 0 until ProtocolDefinition.SECTION_WIDTH_X) {
            for (z in 0 until ProtocolDefinition.SECTION_WIDTH_Z) {
                startSkylightFloodFill(x, z)
            }
        }
    }

    fun getNeighbourMaxHeight(neighbours: Array<Chunk>, x: Int, z: Int, heightmapIndex: Int = (z shl 4) or x): Int {
        return maxOf(
            if (x > 0) {
                heightmap[heightmapIndex - 1]
            } else {
                neighbours[ChunkNeighbours.WEST].light.heightmap[(z shl 4) or ProtocolDefinition.SECTION_MAX_X]
            },

            if (x < ProtocolDefinition.SECTION_MAX_X) {
                heightmap[heightmapIndex + 1]
            } else {
                neighbours[ChunkNeighbours.EAST].light.heightmap[(z shl 4) or 0]
            },

            if (z > 0) {
                heightmap[((z - 1) shl 4) or x]
            } else {
                neighbours[ChunkNeighbours.NORTH].light.heightmap[(ProtocolDefinition.SECTION_MAX_Z shl 4) or x]
            },

            if (z < ProtocolDefinition.SECTION_MAX_Z) {
                heightmap[((z + 1) shl 4) or x]
            } else {
                neighbours[ChunkNeighbours.SOUTH].light.heightmap[(0 shl 4) or x]
            }
        )
    }

    fun getNeighbourMinHeight(neighbours: Array<Chunk>, x: Int, z: Int, heightmapIndex: Int = (z shl 4) or x): Int {
        return minOf(
            if (x > 0) {
                heightmap[heightmapIndex - 1]
            } else {
                neighbours[ChunkNeighbours.WEST].light.heightmap[(z shl 4) or ProtocolDefinition.SECTION_MAX_X]
            },

            if (x < ProtocolDefinition.SECTION_MAX_X) {
                heightmap[heightmapIndex + 1]
            } else {
                neighbours[ChunkNeighbours.EAST].light.heightmap[(z shl 4) or 0]
            },

            if (z > 0) {
                heightmap[((z - 1) shl 4) or x]
            } else {
                neighbours[ChunkNeighbours.NORTH].light.heightmap[(ProtocolDefinition.SECTION_MAX_Z shl 4) or x]
            },

            if (z < ProtocolDefinition.SECTION_MAX_Z) {
                heightmap[((z + 1) shl 4) or x]
            } else {
                neighbours[ChunkNeighbours.SOUTH].light.heightmap[(0 shl 4) or x]
            }
        )
    }

    private fun startSkylightFloodFill(x: Int, z: Int) {
        val neighbours = chunk.neighbours.get() ?: return
        val heightmapIndex = (z shl 4) or x
        val maxHeight = heightmap[heightmapIndex]
        val maxHeightSection = maxHeight.sectionHeight
        val skylightStart = getNeighbourMaxHeight(neighbours, x, z, heightmapIndex)

        val skylightStartSectionHeight = skylightStart.sectionHeight
        if (skylightStart.inSectionHeight == 1) {
            // Create section below max section
            chunk.getOrPut(skylightStartSectionHeight - 1)
        }

        for (sectionHeight in minOf(skylightStartSectionHeight, chunk.highestSection) downTo maxOf(maxHeightSection + 1, chunk.lowestSection)) {
            val section = chunk.sections?.get(sectionHeight - chunk.lowestSection) ?: continue

            // ToDo: Only update if affected by heightmap change
            section.light.update = true
            // ToDo: bare tracing
            val baseY = sectionHeight * ProtocolDefinition.SECTION_HEIGHT_Y
            for (y in ProtocolDefinition.SECTION_MAX_Y downTo 0) {
                section.light.traceSkylightIncrease(x, y, z, ProtocolDefinition.MAX_LIGHT_LEVEL_I, null, baseY + y, false)
            }
        }
        if (maxHeight.sectionHeight < chunk.lowestSection) {
            // bottom section
            bottom.traceSkyIncrease(x, z, ProtocolDefinition.MAX_LIGHT_LEVEL_I)
        } else {
            val maxSection = chunk.getOrPut(maxHeightSection)
            val baseY = maxHeightSection * ProtocolDefinition.SECTION_HEIGHT_Y
            if (maxSection != null) {
                for (y in (if (skylightStartSectionHeight != maxHeightSection) ProtocolDefinition.SECTION_MAX_Y else skylightStart.inSectionHeight) downTo maxHeight.inSectionHeight) {
                    maxSection.light.traceSkylightIncrease(x, y, z, ProtocolDefinition.MAX_LIGHT_LEVEL_I, null, baseY + y, false)
                }
                maxSection.light.update = true
            }
        }
    }

    inline fun getMaxHeight(x: Int, z: Int): Int {
        return heightmap[(z shl 4) or x]
    }

    fun recalculateSkylight(sectionHeight: Int) {
        val minY = sectionHeight * ProtocolDefinition.SECTION_HEIGHT_Y

        // TODO: clear neighbours and let them propagate?
        // TODO: Optimize for specific section height (i.e. not trace everything above)
        calculateSkylight()
    }
}
