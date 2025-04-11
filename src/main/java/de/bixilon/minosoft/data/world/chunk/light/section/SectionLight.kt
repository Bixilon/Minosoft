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

import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.light.LightProperties
import de.bixilon.minosoft.data.registries.blocks.light.TransparentProperty
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.chunk.light.types.LightArray
import de.bixilon.minosoft.data.world.chunk.light.types.LightLevel
import de.bixilon.minosoft.data.world.chunk.light.types.LightLevel.Companion.MAX_LEVEL
import de.bixilon.minosoft.data.world.chunk.update.chunk.SectionLightUpdate
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.minosoft.gui.rendering.util.VecUtil.inSectionHeight
import de.bixilon.minosoft.gui.rendering.util.VecUtil.sectionHeight
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition.*

class SectionLight(
    private val section: ChunkSection,
    private val light: LightArray = LightArray(),
) : AbstractSectionLight {
    private var event = false

    private fun handleLuminanceChange(position: InSectionPosition, previous: BlockState?, state: BlockState?) {
        val previousLuminance = previous?.luminance ?: 0
        val luminance = state?.luminance ?: 0

        when {
            luminance > previousLuminance -> onIncrease(position, luminance)
            luminance < previousLuminance -> onDecrease(position)
        }
    }

    fun handleLightPropertiesChange(position: InSectionPosition, previous: BlockState?, state: BlockState?) {
        val previousProperties = previous?.block?.getLightProperties(previous) ?: TransparentProperty
        val properties = state?.block?.getLightProperties(state) ?: TransparentProperty

        if (previousProperties == properties) return

        if (position.x > 0) traceFrom(position.minusX(), Directions.EAST) else getNeighbour(Directions.WEST)?.traceFrom(position.with(x = SECTION_MAX_X), Directions.EAST)
        if (position.x < SECTION_MAX_X) traceFrom(position.plusX(), Directions.WEST) else getNeighbour(Directions.EAST)?.traceFrom(position.with(x = 0), Directions.WEST)

        if (position.y > 0) traceFrom(position.minusY(), Directions.DOWN) else getNeighbour(Directions.UP)?.traceFrom(position.with(y = SECTION_MAX_Y), Directions.DOWN)
        if (position.y < SECTION_MAX_Y) traceFrom(position.plusY(), Directions.UP) else getNeighbour(Directions.DOWN)?.traceFrom(position.with(y = 0), Directions.UP)

        if (position.z > 0) traceFrom(position.minusZ(), Directions.NORTH) else getNeighbour(Directions.SOUTH)?.traceFrom(position.with(z = SECTION_MAX_Z), Directions.NORTH)
        if (position.z < SECTION_MAX_Z) traceFrom(position.plusZ(), Directions.SOUTH) else getNeighbour(Directions.NORTH)?.traceFrom(position.with(z = 0), Directions.SOUTH)
    }

    fun onBlockChange(position: InSectionPosition, previous: BlockState?, state: BlockState?) {
        handleLightPropertiesChange(position, previous, state)
        handleLuminanceChange(position, previous, state)
    }

    private fun onIncrease(position: InSectionPosition, luminance: Int) {
        trace(position, LightLevel(block = luminance, sky = this[position].sky), null)
    }

    private fun onDecrease(position: InSectionPosition) {
        // TODO: Trace until next light increase (or level 0), set all those levels to 0 and then force trace all blocks in range (also from neighbours).
    }

    private inline fun getOrPut(direction: Directions): SectionLight? {
        var section = this.section.neighbours?.get(direction.ordinal)

        if (section == null) {
            val chunk = if (direction.axis == Axes.Y) this.section.chunk else this.section.chunk.neighbours[direction]
            section = chunk?.getOrPut(this.section.height + direction.vector.y)
        }

        return section?.light
    }

    private inline fun traceWest(position: InSectionPosition, next: LightLevel, properties: LightProperties) {
        if (!properties.propagatesLight(Directions.WEST)) return

        val neighbour = if (position.x > 0) this else getOrPut(Directions.WEST)
        neighbour?.trace(if (position.x > 0) position.minusX() else position.with(x = SECTION_MAX_X), next, Directions.WEST)
    }

    private inline fun traceEast(position: InSectionPosition, next: LightLevel, properties: LightProperties) {
        if (!properties.propagatesLight(Directions.EAST)) return

        val neighbour = if (position.x < SECTION_MAX_X) this else getOrPut(Directions.EAST)
        neighbour?.trace(if (position.x < SECTION_MAX_X) position.plusX() else position.with(x = 0), next, Directions.EAST)
    }

    private inline fun traceUp(position: InSectionPosition, next: LightLevel, properties: LightProperties) {
        if (!properties.propagatesLight(Directions.UP)) return

        when {
            position.y > 0 -> trace(position.minusY(), next, Directions.UP)
            section.height > section.chunk.minSection -> getOrPut(Directions.DOWN)?.trace(position.with(y = SECTION_MAX_Y), next, Directions.UP)
            else -> section.chunk.light.bottom.trace(position.with(y = SECTION_MAX_Y), next)
        }
    }

    private inline fun traceDown(position: InSectionPosition, next: LightLevel, properties: LightProperties) {
        if (!properties.propagatesLight(Directions.DOWN)) return

        when {
            position.y < SECTION_MAX_Y -> trace(position.plusY(), next, Directions.DOWN)
            section.height < section.chunk.maxSection -> getOrPut(Directions.UP)?.trace(position.with(y = 0), next, Directions.DOWN)
            else -> section.chunk.light.top.trace(position.with(y = 0), next)
        }
    }

    private inline fun traceNorth(position: InSectionPosition, next: LightLevel, properties: LightProperties) {
        if (!properties.propagatesLight(Directions.NORTH)) return

        val neighbour = if (position.z > 0) this else getOrPut(Directions.NORTH)
        neighbour?.trace(if (position.z > 0) position.minusZ() else position.with(z = SECTION_MAX_Z), next, Directions.NORTH)
    }

    private inline fun traceSouth(position: InSectionPosition, next: LightLevel, properties: LightProperties) {
        if (!properties.propagatesLight(Directions.SOUTH)) return

        val neighbour = if (position.z < SECTION_MAX_Z) this else getOrPut(Directions.SOUTH)
        neighbour?.trace(if (position.z < SECTION_MAX_Z) position.plusZ() else position.with(z = 0), next, Directions.SOUTH)
    }


    override fun traceFrom(position: InSectionPosition, direction: Directions) {
        val current = this[position]
        if (current.block <= 1 && current.sky <= 1) return

        val state = section.blocks[position]
        val lightProperties = state?.block?.getLightProperties(state) ?: TransparentProperty

        if (lightProperties.propagatesLight(direction.inverted)) {
            return // light can not pass into from that side
        }

        if (!lightProperties.propagatesLight) return

        val next = current.decrease()
        when (direction) {
            Directions.DOWN -> traceDown(position, next, lightProperties)
            Directions.UP -> traceUp(position, next, lightProperties)

            Directions.NORTH -> traceNorth(position, next, lightProperties)
            Directions.SOUTH -> traceSouth(position, next, lightProperties)

            Directions.WEST -> traceWest(position, next, lightProperties)
            Directions.EAST -> traceEast(position, next, lightProperties)
        }
    }

    fun traceSkyDown(xz: InSectionPosition, topY: Int, bottomY: Int) {
        for (y in bottomY..topY) {
            traceSky(xz.with(y = y))
        }
    }

    fun traceSky(position: InSectionPosition) {
        val current = this[position]
        val level = current.with(sky = MAX_LEVEL)

        if (current.block >= level.block && current.sky >= level.sky) return // light is already same or higher, no need to increase
        val lightProperties = section.blocks[position]?.let { it.block.getLightProperties(it) } ?: TransparentProperty

        val height = section.chunk.light.heightmap[position.xz]

        this[position] = level

        if (!lightProperties.propagatesLight) return

        val next = level.decrease()

        traceWest(position, next, lightProperties)
        traceEast(position, next, lightProperties)

        // don't trace up, we are already above the heightmap
        if (height == section.height * SECTION_HEIGHT_Y + position.y) { // TODO: verify
            traceDown(position, next, lightProperties)
        }

        traceNorth(position, next, lightProperties)
        traceSouth(position, next, lightProperties)
    }


    fun trace(position: InSectionPosition, level: LightLevel, direction: Directions?) {
        var level = level
        val current = this[position]
        if (current.block >= level.block && current.sky >= level.sky) return // light is already same or higher, no need to increase
        val lightProperties = section.blocks[position]?.let { it.block.getLightProperties(it) } ?: TransparentProperty

        if (direction != null && !lightProperties.propagatesLight(direction.inverted)) {
            return // light can not pass into from that side
        }

        val height = section.chunk.light.heightmap[position.xz]
        if (section.height * SECTION_HEIGHT_Y + position.y >= height) {
            level = level.with(sky = 0) // level is set with heightmap, no need to trace anything
        }

        this[position] = level

        if (!lightProperties.propagatesLight) return
        if (level.block <= 1 && level.sky <= 1) return // can not decrease any further

        val next = level.decrease()

        traceWest(position, next, lightProperties)
        traceEast(position, next, lightProperties)

        traceUp(position, next, lightProperties)
        traceDown(position, next, lightProperties)

        traceNorth(position, next, lightProperties)
        traceSouth(position, next, lightProperties)
    }


    override fun clear() {
        this.light.clear()
        event = true
    }

    fun calculateBlocks() {
        if (section.blocks.isEmpty) return
        val min = section.blocks.minPosition
        val max = section.blocks.maxPosition

        for (y in min.y..max.y) {
            for (z in min.z..max.z) {
                for (x in min.x..max.x) {
                    val position = InSectionPosition(x, y, z)
                    val state = section.blocks[position] ?: continue
                    val luminance = state.luminance
                    if (luminance <= 0) continue

                    onIncrease(position, luminance)
                }
            }
        }
    }

    fun calculateSky() {
        val limit = (section.height + 1) * SECTION_HEIGHT_Y

        for (xz in 0 until SECTION_WIDTH_X * SECTION_WIDTH_Z) {
            val height = section.chunk.light.heightmap[xz]
            if (height >= limit) continue
            val min = if (height.sectionHeight == section.height) height.inSectionHeight else 0
            for (y in min until SECTION_MAX_Y) { // TODO: do that in chunk light and only up to max of neighbours
                traceSky(InSectionPosition(xz).with(y = y))
            }
        }
    }

    protected fun getNeighbour(direction: Directions): AbstractSectionLight? {
        return when (direction) {
            Directions.UP -> if (section.height == section.chunk.maxSection) section.chunk.light.top else section.chunk[section.height + 1]?.light
            Directions.DOWN -> if (section.height == section.chunk.minSection) section.chunk.light.bottom else section.chunk[section.height - 1]?.light
            else -> section.chunk.neighbours[direction]?.get(section.height)?.light
        }
    }

    private fun propagateVertical() {
        val north = getNeighbour(Directions.NORTH)
        val south = getNeighbour(Directions.SOUTH)
        val west = getNeighbour(Directions.WEST)
        val east = getNeighbour(Directions.EAST)

        if (north == null && south == null && west == null && east == null) return

        for (a in 0 until SECTION_WIDTH_X) {
            for (y in 0 until SECTION_WIDTH_X) {
                north?.traceFrom(InSectionPosition(a, y, SECTION_MAX_Z), Directions.SOUTH)
                south?.traceFrom(InSectionPosition(a, y, 0), Directions.NORTH)

                west?.traceFrom(InSectionPosition(SECTION_MAX_X, y, a), Directions.EAST)
                east?.traceFrom(InSectionPosition(0, y, a), Directions.WEST)
            }
        }
    }

    private fun propagateHorizontal() {
        val below = if (section.height == section.chunk.minSection) section.chunk.light.bottom else section.chunk[section.height - 1]?.light
        val above = if (section.height == section.chunk.maxSection) section.chunk.light.top else section.chunk[section.height + 1]?.light

        // TODO: merge both?
        if (below != null) {
            for (xz in 0 until SECTION_WIDTH_X * SECTION_WIDTH_Z) {
                val position = InSectionPosition(xz).with(y = SECTION_MAX_Y)
                below.traceFrom(position, Directions.UP)
            }
        }
        if (above != null) {
            for (xz in 0 until SECTION_WIDTH_X * SECTION_WIDTH_Z) {
                val position = InSectionPosition(xz).with(y = 0)
                above.traceFrom(position, Directions.DOWN)
            }
        }
    }

    override fun propagate() {
        propagateVertical()
        propagateHorizontal()
    }

    override fun get(position: InSectionPosition) = light[position]

    operator fun set(position: InSectionPosition, level: LightLevel) {
        light[position] = level
        event = true
    }

    override fun fireEvent(): SectionLightUpdate? {
        if (!event) return null
        event = false
        return SectionLightUpdate(section)
    }


    override fun update(array: LightArray) {
        System.arraycopy(array.array, 0, this.light.array, 0, array.array.size)
    }
}
