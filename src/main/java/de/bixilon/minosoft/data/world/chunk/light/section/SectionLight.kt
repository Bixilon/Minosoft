/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.world.chunk.light.section

import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.light.TransparentProperty
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.chunk.light.section.ChunkSkyLight.Companion.NEIGHBOUR_TRACE_LEVEL
import de.bixilon.minosoft.data.world.chunk.light.types.LightArray
import de.bixilon.minosoft.data.world.chunk.light.types.LightLevel
import de.bixilon.minosoft.data.world.chunk.neighbours.ChunkNeighbourArray
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition

class SectionLight(
    val section: ChunkSection,
    var light: LightArray = LightArray(), // packed (skyLight: 0xF0, blockLight: 0x0F)
) : AbstractSectionLight() {

    fun onBlockChange(position: InSectionPosition, previous: BlockState?, state: BlockState?) {
        val previousLuminance = previous?.luminance ?: 0
        val luminance = state?.luminance ?: 0

        if (previousLuminance == luminance) {
            val nowProperties = state?.block?.getLightProperties(state)
            if (previous?.block?.getLightProperties(previous)?.propagatesLight == nowProperties?.propagatesLight) {
                // no change for light data
                return
            }
            if (nowProperties == null || nowProperties.propagatesLight) {
                // block got destroyed/is propagating light now
                propagateFromNeighbours(position)
                return
            }
            // ToDo: else decrease light around placed block
        }

        if (luminance > previousLuminance) {
            traceBlockIncrease(position, luminance, null)
        } else {
            startDecreaseTrace(position)
        }
    }

    private fun startDecreaseTrace(position: InSectionPosition) {
        // that is kind of hacky, but far easier and kind of faster
        val light = this.light[position].block

        decreaseLight(position, light, true) // just clear the light
        decreaseLight(position, light, false) // increase the light in all sections
    }

    private fun decreaseLight(position: InSectionPosition, light: Int, reset: Boolean) {
        decreaseCheckLevel(position.x, position.z, light, reset)

        val neighbours = section.neighbours ?: return
        val chunk = section.chunk
        if (position.y - light < 0) {
            if (section.height == chunk.minSection) {
                chunk.light.bottom.decreaseCheckLevel(position.x, position.z, light - position.y, reset)
            } else {
                neighbours[Directions.O_DOWN]?.light?.decreaseCheckLevel(position.x, position.z, light - position.y, reset)
            }
        }
        if (position.y + light > ProtocolDefinition.SECTION_MAX_Y) {
            if (section.height == chunk.maxSection) {
                chunk.light.top.decreaseCheckLevel(position.x, position.z, light - (ProtocolDefinition.SECTION_MAX_Y - position.y), reset)
            } else {
                neighbours[Directions.O_UP]?.light?.decreaseCheckLevel(position.x, position.z, light - (ProtocolDefinition.SECTION_MAX_Y - position.y), reset)
            }
        }
    }

    private fun decreaseCheckLevel(x: Int, z: Int, light: Int, reset: Boolean) {
        decreaseCheckX(z, light, reset)
        val neighbours = section.neighbours ?: return

        if (x - light < 0) {
            neighbours[Directions.O_WEST]?.light?.decreaseCheckX(z, light - x, reset)
        }
        if (x + light > ProtocolDefinition.SECTION_MAX_X) {
            neighbours[Directions.O_EAST]?.light?.decreaseCheckX(z, light - (ProtocolDefinition.SECTION_MAX_X - x), reset)
        }
    }

    private fun decreaseCheckX(z: Int, light: Int, reset: Boolean) {
        val neighbours = section.neighbours ?: return
        if (reset) reset() else calculate()

        if (z - light < 0) {
            val neighbour = neighbours[Directions.O_NORTH]?.light
            if (reset) neighbour?.reset() else neighbour?.calculate()
        }
        if (z + light > ProtocolDefinition.SECTION_MAX_Z) {
            val neighbour = neighbours[Directions.O_SOUTH]?.light
            if (reset) neighbour?.reset() else neighbour?.calculate()
        }
    }

    private fun Array<ChunkSection?>.invalidateLight(position: InSectionPosition) {
        // we can not further increase the light
        // set neighbour update, cullface might change lighting properties
        if (position.y == 0) this[Directions.O_DOWN]?.light?.update = true
        if (position.y == ProtocolDefinition.SECTION_MAX_Y) this[Directions.O_UP]?.light?.update = true
        if (position.z == 0) this[Directions.O_NORTH]?.light?.update = true
        if (position.z == ProtocolDefinition.SECTION_MAX_Z) this[Directions.O_SOUTH]?.light?.update = true
        if (position.x == 0) this[Directions.O_WEST]?.light?.update = true
        if (position.x == ProtocolDefinition.SECTION_MAX_X) this[Directions.O_EAST]?.light?.update = true
    }

    fun traceBlockIncrease(position: InSectionPosition, nextLuminance: Int, target: Directions?) {
        val block = section.blocks[position]
        val lightProperties = block?.block?.getLightProperties(block) ?: TransparentProperty
        val blockLuminance = block?.luminance ?: 0
        if (block != null && !lightProperties.propagatesLight && blockLuminance == 0) {
            // light can not pass through the block
            return
        }
        if (target != null && !lightProperties.propagatesLight(target.inverted)) {
            // our block can not trace the light through that side
            return
        }

        // get block or next luminance level
        val level = this.light[position]
        val currentLight = level.block // we just care about block light
        if (currentLight >= nextLuminance) {
            // light is already higher, no need to trace
            return
        }
        this.light[position] = level.with(block = nextLuminance) // keep the sky light set
        if (!update) {
            update = true
        }
        val chunk = section.chunk
        val chunkNeighbours = chunk.neighbours.neighbours
        val neighbours = section.neighbours ?: return

        if (nextLuminance == 1) {
            neighbours.invalidateLight(position)
            return
        }


        if (blockLuminance > nextLuminance) {
            // we only want to set our own light sources
            return
        }

        val neighbourLuminance = nextLuminance - 1

        if (target == null || (target != Directions.UP && lightProperties.propagatesLight(Directions.DOWN))) {
            if (position.y > 0) {
                traceBlockIncrease(position.minusY(), neighbourLuminance, Directions.DOWN)
            } else if (section.height == chunk.minSection) {
                chunk.light.bottom.traceBlockIncrease(position.x, position.z, neighbourLuminance)
            } else {
                (neighbours[Directions.O_DOWN] ?: chunk.getOrPut(section.height - 1, false))?.light?.traceBlockIncrease(position.with(y = ProtocolDefinition.SECTION_MAX_Y), neighbourLuminance, Directions.DOWN)
            }
        }
        if (target == null || (target != Directions.DOWN && lightProperties.propagatesLight(Directions.UP))) {
            if (position.y < ProtocolDefinition.SECTION_MAX_Y) {
                traceBlockIncrease(position.plusY(), neighbourLuminance, Directions.UP)
            } else if (section.height == chunk.maxSection) {
                chunk.light.top.traceBlockIncrease(position.x, position.z, neighbourLuminance)
            } else {
                (neighbours[Directions.O_UP] ?: chunk.getOrPut(section.height + 1, false))?.light?.traceBlockIncrease(position.with(y = 0), neighbourLuminance, Directions.UP)
            }
        }

        if (target == null || (target != Directions.SOUTH && lightProperties.propagatesLight(Directions.NORTH))) {
            if (position.z > 0) {
                traceBlockIncrease(position.minusZ(), neighbourLuminance, Directions.NORTH)
            } else {
                neighbours[Directions.O_NORTH, Directions.NORTH, chunkNeighbours]?.light?.traceBlockIncrease(position.with(z = ProtocolDefinition.SECTION_MAX_Z), neighbourLuminance, Directions.NORTH)
            }
        }
        if (target == null || (target != Directions.NORTH && lightProperties.propagatesLight(Directions.SOUTH))) {
            if (position.z < ProtocolDefinition.SECTION_MAX_Y) {
                traceBlockIncrease(position.plusZ(), neighbourLuminance, Directions.SOUTH)
            } else {
                neighbours[Directions.O_SOUTH, Directions.SOUTH, chunkNeighbours]?.light?.traceBlockIncrease(position.with(z = 0), neighbourLuminance, Directions.SOUTH)
            }
        }
        if (target == null || (target != Directions.EAST && lightProperties.propagatesLight(Directions.WEST))) {
            if (position.x > 0) {
                traceBlockIncrease(position.minusX(), neighbourLuminance, Directions.WEST)
            } else {
                neighbours[Directions.O_WEST, Directions.WEST, chunkNeighbours]?.light?.traceBlockIncrease(position.with(x = ProtocolDefinition.SECTION_MAX_X), neighbourLuminance, Directions.WEST)
            }
        }
        if (target == null || (target != Directions.WEST && lightProperties.propagatesLight(Directions.EAST))) {
            if (position.x < ProtocolDefinition.SECTION_MAX_X) {
                traceBlockIncrease(position.plusX(), neighbourLuminance, Directions.EAST)
            } else {
                neighbours[Directions.O_EAST, Directions.EAST, chunkNeighbours]?.light?.traceBlockIncrease(position.with(x = 0), neighbourLuminance, Directions.EAST)
            }
        }
    }

    fun reset() {
        update = true
        light.clear()
    }


    fun recalculate() {
        update = true
        reset()
        calculate()
    }

    fun calculate() {
        update = true
        val blocks = section.blocks

        section.chunk.lock.lock()
        val min = blocks.minPosition
        val max = blocks.maxPosition

        for (x in min.x..max.x) {
            for (z in min.z..max.z) {
                for (y in min.y..max.y) {
                    val position = InSectionPosition(x, y, z)
                    val luminance = blocks[position]?.luminance ?: continue
                    if (luminance == 0) {
                        // block is not emitting light, ignore it
                        continue
                    }
                    traceBlockIncrease(position, luminance, null)
                }
            }
        }
        section.chunk.lock.unlock()
        section.chunk.light.sky.recalculate(section.height)
    }


    fun propagateFromNeighbours() {
        val neighbours = section.neighbours ?: return
        // ToDo(p): this::traceIncrease checks als the block light level, not needed

        // ToDo: Check if current block can propagate into that direction
        val baseY = section.height * ProtocolDefinition.SECTION_HEIGHT_Y
        for (x in 0 until ProtocolDefinition.SECTION_WIDTH_X) {
            if (neighbours[Directions.O_DOWN] != null || neighbours[Directions.O_UP] != null) {
                propagateY(neighbours, x, baseY)
            }
            if (neighbours[Directions.O_NORTH] != null || neighbours[Directions.O_SOUTH] != null) {
                propagateZ(baseY, neighbours, x)
            }
        }
        if (neighbours[Directions.O_WEST] != null || neighbours[Directions.O_EAST] != null) {
            propagateX(baseY, neighbours)
        }
    }

    private fun propagateX(baseY: Int, neighbours: Array<ChunkSection?>) {
        for (y in 0 until ProtocolDefinition.SECTION_HEIGHT_Y) {
            for (z in 0 until ProtocolDefinition.SECTION_WIDTH_Z) {
                val totalY = baseY + y
                neighbours[Directions.O_WEST]?.light?.get(InSectionPosition(ProtocolDefinition.SECTION_MAX_Z, y, z))?.let { light ->
                    light.block.let { if (it > 1) traceBlockIncrease(InSectionPosition(0, y, z), it - 1, Directions.EAST) }
                    light.sky.let { if (it > 1) traceSkyLightIncrease(InSectionPosition(0, y, z), it - 1, Directions.EAST, totalY) }
                }
                neighbours[Directions.O_EAST]?.light?.get(InSectionPosition(0, y, z))?.let { light ->
                    light.block.let { if (it > 1) traceBlockIncrease(InSectionPosition(ProtocolDefinition.SECTION_MAX_X, y, z), it - 1, Directions.WEST) }
                    light.sky.let { if (it > 1) traceSkyLightIncrease(InSectionPosition(ProtocolDefinition.SECTION_MAX_X, y, z), it - 1, Directions.WEST, totalY) }
                }
            }
        }
    }

    private fun propagateZ(baseY: Int, neighbours: Array<ChunkSection?>, x: Int) {
        for (y in 0 until ProtocolDefinition.SECTION_HEIGHT_Y) {
            val totalY = baseY + y
            neighbours[Directions.O_NORTH]?.light?.get(InSectionPosition(x, y, ProtocolDefinition.SECTION_MAX_Z))?.let { light ->
                light.block.let { if (it > 1) traceBlockIncrease(InSectionPosition(x, y, 0), it - 1, Directions.SOUTH) }
                light.sky.let { if (it > 1) traceSkyLightIncrease(InSectionPosition(x, y, 0), it - 1, Directions.SOUTH, totalY) }
            }
            neighbours[Directions.O_SOUTH]?.light?.get(InSectionPosition(x, y, 0))?.let { light ->
                light.block.let { if (it > 1) traceBlockIncrease(InSectionPosition(x, y, ProtocolDefinition.SECTION_MAX_Z), it - 1, Directions.NORTH) }
                light.sky.let { if (it > 1) traceSkyLightIncrease(InSectionPosition(x, y, ProtocolDefinition.SECTION_MAX_Z), it - 1, Directions.NORTH, totalY) }
            }
        }
    }

    private fun propagateY(neighbours: Array<ChunkSection?>, x: Int, baseY: Int) {
        // ToDo: Border light
        for (z in 0 until ProtocolDefinition.SECTION_WIDTH_Z) {
            neighbours[Directions.O_DOWN]?.light?.get(InSectionPosition(x, ProtocolDefinition.SECTION_MAX_Y, z))?.let { light ->
                light.block.let { if (it > 1) traceBlockIncrease(InSectionPosition(x, 0, z), it - 1, Directions.UP) }
                light.sky.let { if (it > 1) traceSkyLightIncrease(InSectionPosition(x, 0, z), it - 1, Directions.UP, baseY + 0) } // ToDo: Is that possible?
            }
            neighbours[Directions.O_UP]?.light?.get(InSectionPosition(x, 0, z))?.let { light ->
                light.block.let { if (it > 1) traceBlockIncrease(InSectionPosition(x, ProtocolDefinition.SECTION_MAX_Y, z), it - 1, Directions.DOWN) }
                light.sky.let { if (it > 1) traceSkyLightIncrease(InSectionPosition(x, ProtocolDefinition.SECTION_MAX_Y, z), it - 1, Directions.DOWN, baseY + ProtocolDefinition.SECTION_MAX_Y) }
            }
        }
    }

    fun traceSkyLightIncrease(position: InSectionPosition, nextLevel: Int, target: Directions?, totalY: Int) {
        val chunk = section.chunk
        val heightmapIndex = position.xz
        if (totalY >= chunk.light.heightmap[heightmapIndex]) {
            // this light level will be 15, don't care
            return
        }
        val chunkNeighbours = chunk.neighbours.neighbours
        val light = this[position]
        if (light.sky >= nextLevel) {
            return
        }

        val state = section.blocks[position]
        var lightProperties = state?.block?.getLightProperties(state)

        if (lightProperties == null) {
            lightProperties = TransparentProperty
        } else if (!lightProperties.propagatesLight || (target != null && !lightProperties.propagatesLight(target.inverted))) {
            return
        }

        val neighbours = this.section.neighbours ?: return

        this.light[position] = light.with(sky = nextLevel)

        if (!update) {
            update = true
        }


        if (nextLevel == 1) {
            neighbours.invalidateLight(position)
            return
        }

        if (nextLevel <= 1) {
            return
        }

        val nextNeighbourLevel = nextLevel - 1

        if (target != Directions.UP && (target == null || lightProperties.propagatesLight(Directions.DOWN))) {
            if (position.y > 0) {
                traceSkyLightIncrease(position.minusY(), nextNeighbourLevel, Directions.DOWN, totalY - 1)
            } else if (section.height == chunk.minSection) {
                chunk.light.bottom.traceSkyIncrease(position.x, position.z, nextLevel)
            } else {
                (neighbours[Directions.O_DOWN] ?: chunk.getOrPut(section.height - 1, false))?.light?.traceSkyLightIncrease(position.with(y = ProtocolDefinition.SECTION_MAX_Y), nextNeighbourLevel, Directions.DOWN, totalY - 1)
            }
        }
        if (target != Directions.DOWN && (target != null || lightProperties.propagatesLight(Directions.UP))) {
            if (position.y < ProtocolDefinition.SECTION_MAX_Y) {
                traceSkyLightIncrease(position.plusY(), nextNeighbourLevel, Directions.UP, totalY + 1)
            } else if (section.height < chunk.maxSection) {
                (neighbours[Directions.O_UP] ?: chunk.getOrPut(section.height + 1, false))?.light?.traceSkyLightIncrease(position.with(y = 0), nextNeighbourLevel, Directions.UP, totalY + 1)
            }
        }
        if (target != Directions.SOUTH && (target == null || lightProperties.propagatesLight(Directions.NORTH))) {
            if (position.z > 0) {
                traceSkyLightIncrease(position.minusZ(), nextNeighbourLevel, Directions.NORTH, totalY)
            } else {
                neighbours[Directions.O_NORTH, Directions.NORTH, chunkNeighbours]?.light?.traceSkyLightIncrease(position.with(z = ProtocolDefinition.SECTION_MAX_Z), nextNeighbourLevel, Directions.NORTH, totalY)
            }
        }
        if (target != Directions.NORTH && (target == null || lightProperties.propagatesLight(Directions.SOUTH))) {
            if (position.z < ProtocolDefinition.SECTION_MAX_Z) {
                traceSkyLightIncrease(position.plusZ(), nextNeighbourLevel, Directions.SOUTH, totalY)
            } else {
                neighbours[Directions.O_SOUTH, Directions.SOUTH, chunkNeighbours]?.light?.traceSkyLightIncrease(position.with(z = 0), nextNeighbourLevel, Directions.SOUTH, totalY)
            }
        }
        if (target != Directions.EAST && (target == null || lightProperties.propagatesLight(Directions.WEST))) {
            if (position.x > 0) {
                traceSkyLightIncrease(position.minusX(), nextNeighbourLevel, Directions.WEST, totalY)
            } else {
                neighbours[Directions.O_WEST, Directions.WEST, chunkNeighbours]?.light?.traceSkyLightIncrease(position.with(x = ProtocolDefinition.SECTION_MAX_X), nextNeighbourLevel, Directions.WEST, totalY)
            }
        }
        if (target != Directions.WEST && (target == null || lightProperties.propagatesLight(Directions.EAST))) {
            if (position.x < ProtocolDefinition.SECTION_MAX_X) {
                traceSkyLightIncrease(position.plusX(), nextNeighbourLevel, Directions.EAST, totalY)
            } else {
                neighbours[Directions.O_EAST, Directions.EAST, chunkNeighbours]?.light?.traceSkyLightIncrease(position.with(x = 0), nextNeighbourLevel, Directions.EAST, totalY)
            }
        }
    }

    fun traceSkyLightDown(position: InSectionPosition, target: Directions?, totalY: Int) { // TODO: remove code duplicates
        val chunk = section.chunk

        val state = section.blocks[position]
        var lightProperties = state?.block?.getLightProperties(state)

        if (lightProperties == null) {
            lightProperties = TransparentProperty
        } else if (!lightProperties.propagatesLight || (target != null && !lightProperties.propagatesLight(target.inverted))) {
            return
        }

        val neighbours = this.section.neighbours ?: return

        this.light[position] = this.light[position].with(sky = ProtocolDefinition.MAX_LIGHT_LEVEL_I)

        if (!update) {
            update = true
        }


        if (lightProperties.propagatesLight(Directions.DOWN)) {
            if (position.y > 0) {
                traceSkyLightIncrease(position.minusY(), NEIGHBOUR_TRACE_LEVEL, Directions.DOWN, totalY - 1)
            } else {
                (neighbours[Directions.O_DOWN] ?: chunk.getOrPut(section.height - 1, false))?.light?.traceSkyLightIncrease(position.with(y = ProtocolDefinition.SECTION_MAX_Y), NEIGHBOUR_TRACE_LEVEL, Directions.DOWN, totalY - 1)
            }
        }
    }

    private inline operator fun Array<ChunkSection?>.get(direction: Int, neighbour: Directions, neighbours: ChunkNeighbourArray): ChunkSection? {
        return this[direction] ?: neighbours[neighbour]?.getOrPut(section.height, false)
    }

    fun propagateFromNeighbours(position: InSectionPosition) {
        val neighbours = section.neighbours ?: return

        var level = LightLevel(0, 0)
        

        // ToDo: check if light can exit block at side or can enter block at neighbour

        if (position.x > 0) {
            level = level.max(this[position.minusX()])
        } else {
            neighbours[Directions.O_WEST]?.light?.get(position.with(x = ProtocolDefinition.SECTION_MAX_X))?.let { level = level.max(it) }
        }
        if (position.x < ProtocolDefinition.SECTION_MAX_X) {
            level = level.max(this[position.plusX()])
        } else {
            neighbours[Directions.O_EAST]?.light?.get(position.with(x = 0))?.let { level = level.max(it) }
        }

        if (position.y > 0) {
            level = level.max(this[position.minusY()])
        } else {
            neighbours[Directions.O_DOWN]?.light?.get(position.with(y = ProtocolDefinition.SECTION_MAX_Y))?.let { level = level.max(it) }
        }

        if (position.y < ProtocolDefinition.SECTION_MAX_Y) {
            level = level.max(this[position.plusY()])
        } else {
            neighbours[Directions.O_UP]?.light?.get(position.with(y = 0))?.let { level = level.max(it) }
        }

        if (position.z > 0) {
            level = level.max(this[position.minusZ()])
        } else {
            neighbours[Directions.O_NORTH]?.light?.get(position.with(z = ProtocolDefinition.SECTION_MAX_Z))?.let { level = level.max(it) }
        }
        if (position.z < ProtocolDefinition.SECTION_MAX_Z) {
            level = level.max(this[position.plusZ()])
        } else {
            neighbours[Directions.O_SOUTH]?.light?.get(position.with(z = 0))?.let { level = level.max(it) }
        }

        traceBlockIncrease(position, level.block - 1, null)

        val totalY = section.height * ProtocolDefinition.SECTION_HEIGHT_Y + position.y
        section.chunk.let {
            // check if neighbours are above heightmap, if so set light level to max
            val chunkNeighbours = it.neighbours.neighbours
            if (!it.neighbours.complete) return@let
            val minHeight = it.light.sky.getNeighbourMinHeight(chunkNeighbours, position.x, position.z)
            if (totalY > minHeight) {
                level = level.with(sky = ProtocolDefinition.MAX_LIGHT_LEVEL_I)
            }
        }
        traceSkyLightIncrease(position, level.sky - 1, null, totalY)
    }

    override fun get(position: InSectionPosition) = light[position]
}
