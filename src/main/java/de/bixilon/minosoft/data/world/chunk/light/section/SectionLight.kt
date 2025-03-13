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
import de.bixilon.minosoft.data.world.chunk.light.types.LightArray
import de.bixilon.minosoft.data.world.chunk.light.types.LightLevel
import de.bixilon.minosoft.data.world.chunk.update.chunk.SectionLightUpdate
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition.*

class SectionLight(
    private val section: ChunkSection,
    private val light: LightArray = LightArray(),
) : AbstractSectionLight {
    private var event = false

    fun onBlockChange(position: InSectionPosition, previous: BlockState?, state: BlockState?) {
        val previousLuminance = previous?.luminance ?: 0
        val luminance = state?.luminance ?: 0

        if (luminance == previousLuminance) {
            // TODO: check if light properties changed
            return
        }

        when {
            luminance > previousLuminance -> onIncrease(position, luminance)
            luminance < previousLuminance -> onDecrease(position)
        }
    }

    private fun onIncrease(position: InSectionPosition, luminance: Int) {
        trace(position, LightLevel(block = luminance, sky = 0), null) // TODO: sky light
    }

    private fun onDecrease(position: InSectionPosition) {
        // TODO: Trace until next light increase (or level 0), set all those levels to 0 and then force trace all blocks in range (also from neighbours).
    }

    fun trace(position: InSectionPosition, level: LightLevel, direction: Directions?) {
        val light = this[position]
        if (light.block >= level.block) return // light is already same or higher, no need to increase
        val state = section.blocks[position]
        val lightProperties = state?.block?.getLightProperties(state) ?: TransparentProperty

        if (direction != null && lightProperties.propagatesLight(direction.inverted)) {
            // light can not pass into from that side
            return
        }

        this[position] = level
        if (!lightProperties.propagatesLight) return


        if (level.block <= 1) return // can not decrease any further

        val next = level.decrease()

        // TODO: sky (trace and heightmap)

        if (lightProperties.propagatesLight(Directions.WEST)) {
            val neighbour = if (position.x > 0) this else section.neighbours?.get(Directions.O_WEST)?.light
            neighbour?.trace(if (position.x > 0) position.minusX() else position.with(x = SECTION_MAX_X), next, Directions.WEST)
        }
        if (lightProperties.propagatesLight(Directions.EAST)) {
            val neighbour = if (position.x < SECTION_MAX_X) this else section.neighbours?.get(Directions.O_EAST)?.light
            neighbour?.trace(if (position.x < SECTION_MAX_X) position.plusX() else position.with(x = 0), next, Directions.EAST)
        }


        if (lightProperties.propagatesLight(Directions.UP)) {
            if (position.y > 0) {
                trace(position.minusY(), next, Directions.UP)
            } else if (section.height > section.chunk.minSection) {
                section.neighbours?.get(Directions.O_DOWN)?.light?.trace(position.with(y = SECTION_MAX_Y), next, Directions.UP)
            } else {
                section.chunk.light.bottom.trace(position.with(y = SECTION_MAX_Y), next)
            }
        }
        if (lightProperties.propagatesLight(Directions.DOWN)) {
            if (position.y < SECTION_MAX_Y) {
                trace(position.plusY(), next, Directions.DOWN)
            } else if (section.height < section.chunk.maxSection) {
                section.neighbours?.get(Directions.O_UP)?.light?.trace(position.with(y = 0), next, Directions.DOWN)
            } else {
                section.chunk.light.top.trace(position.with(y = 0), next)
            }
        }

        if (lightProperties.propagatesLight(Directions.NORTH)) {
            val neighbour = if (position.z > 0) this else section.neighbours?.get(Directions.O_NORTH)?.light
            neighbour?.trace(if (position.z > 0) position.minusZ() else position.with(z = SECTION_MAX_Z), next, Directions.NORTH)
        }
        if (lightProperties.propagatesLight(Directions.SOUTH)) {
            val neighbour = if (position.z < SECTION_MAX_Z) this else section.neighbours?.get(Directions.O_SOUTH)?.light
            neighbour?.trace(if (position.z < SECTION_MAX_Z) position.plusZ() else position.with(z = 0), next, Directions.SOUTH)
        }
    }


    override fun clear() {
        this.light.clear()
        event = true
    }

    fun calculate() {
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

    override fun propagate() = Unit // TODO

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
