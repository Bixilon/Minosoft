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

package de.bixilon.minosoft.data.world.chunk.light.section.border

import de.bixilon.kutil.array.ArrayUtil.getFirst
import de.bixilon.kutil.array.ArrayUtil.getLast
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.chunk.ChunkSize
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.chunk.light.section.AbstractSectionLight
import de.bixilon.minosoft.data.world.chunk.light.types.LightArray
import de.bixilon.minosoft.data.world.chunk.light.types.LightLevel
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import java.util.*

abstract class BorderSectionLight(
    val chunk: Chunk,
) : AbstractSectionLight() {
    val light = ByteArray(ChunkSize.SECTION_WIDTH_X * ChunkSize.SECTION_WIDTH_Z)

    protected abstract fun getNearestSection(): ChunkSection?
    protected abstract fun Chunk.getBorderLight(): BorderSectionLight

    protected inline operator fun get(index: Int) = LightLevel(this.light[index])
    protected inline operator fun set(index: Int, value: LightLevel) {
        this.light[index] = value.raw
    }

    private fun updateY() {
        // we can not further increase the light
        val section = getNearestSection()
        section?.light?.apply { if (!update) update = true }
    }

    fun traceBlockIncrease(x: Int, z: Int, nextLuminance: Int) {
        val index = z shl 4 or x
        val currentLight = this[index]
        if (currentLight.block >= nextLuminance) {
            // light is already higher, no need to trace
            return
        }
        this[index] = currentLight.with(block = nextLuminance)

        if (!update) {
            update = true
        }


        if (nextLuminance == 1) {
            return updateY()
        }
        val neighbourLuminance = nextLuminance - 1

        if (this is TopSectionLight) { // TODO: slow check
            chunk.sections.getLast()?.light?.traceBlockIncrease(InSectionPosition(x, ChunkSize.SECTION_MAX_Y, z), neighbourLuminance, Directions.DOWN)
        } else {
            chunk.sections.getFirst()?.light?.traceBlockIncrease(InSectionPosition(x, 0, z), neighbourLuminance, Directions.UP)
        }

        if (z > 0) {
            traceBlockIncrease(x, z - 1, neighbourLuminance)
        } else {
            chunk.neighbours[Directions.NORTH]?.getBorderLight()?.traceBlockIncrease(x, ChunkSize.SECTION_MAX_Z, neighbourLuminance)
        }
        if (z < ChunkSize.SECTION_MAX_Z) {
            traceBlockIncrease(x, z + 1, neighbourLuminance)
        } else {
            chunk.neighbours[Directions.SOUTH]?.getBorderLight()?.traceBlockIncrease(x, 0, neighbourLuminance)
        }

        if (x > 0) {
            traceBlockIncrease(x - 1, z, neighbourLuminance)
        } else {
            chunk.neighbours[Directions.WEST]?.getBorderLight()?.traceBlockIncrease(ChunkSize.SECTION_MAX_X, z, neighbourLuminance)
        }
        if (x < ChunkSize.SECTION_MAX_X) {
            traceBlockIncrease(x + 1, z, neighbourLuminance)
        } else {
            chunk.neighbours[Directions.EAST]?.getBorderLight()?.traceBlockIncrease(0, z, neighbourLuminance)
        }
    }

    fun traceSkyIncrease(x: Int, z: Int, nextLevel: Int) {
        // TODO: check heightmap
        val index = z shl 4 or x
        val light = this[index]
        if (light.sky >= nextLevel) {
            // light is already higher, no need to trace
            return
        }
        this[index] = light.with(sky = nextLevel)

        if (!update) {
            update = true
        }


        if (nextLevel <= 1) {
            return updateY()
        }
        val neighbourLevel = nextLevel - 1

        if (this is TopSectionLight) { // TOOD: slow check
            chunk.sections.getLast()?.light?.traceSkyLightIncrease(InSectionPosition(x, ChunkSize.SECTION_MAX_Y, z), neighbourLevel, Directions.DOWN, chunk.maxSection * ChunkSize.SECTION_HEIGHT_Y + ChunkSize.SECTION_MAX_Y)
        } else {
            chunk.sections.getFirst()?.light?.traceSkyLightIncrease(InSectionPosition(x, 0, z), neighbourLevel, Directions.UP, chunk.minSection * ChunkSize.SECTION_HEIGHT_Y)
        }

        if (z > 0) {
            traceSkyIncrease(x, z - 1, neighbourLevel)
        } else {
            chunk.neighbours[Directions.NORTH]?.getBorderLight()?.traceSkyIncrease(x, ChunkSize.SECTION_MAX_Z, neighbourLevel)
        }
        if (z < ChunkSize.SECTION_MAX_Y) {
            traceSkyIncrease(x, z + 1, neighbourLevel)
        } else {
            chunk.neighbours[Directions.SOUTH]?.getBorderLight()?.traceSkyIncrease(x, 0, neighbourLevel)
        }

        if (x > 0) {
            traceSkyIncrease(x - 1, z, neighbourLevel)
        } else {
            chunk.neighbours[Directions.WEST]?.getBorderLight()?.traceSkyIncrease(ChunkSize.SECTION_MAX_X, z, neighbourLevel)
        }
        if (x < ChunkSize.SECTION_MAX_X) {
            traceSkyIncrease(x + 1, z, neighbourLevel)
        } else {
            chunk.neighbours[Directions.EAST]?.getBorderLight()?.traceSkyIncrease(0, z, neighbourLevel)
        }
    }

    internal fun decreaseCheckLevel(x: Int, z: Int, light: Int, reset: Boolean) {
        decreaseCheckX(z, light, reset)
        val neighbours = chunk.neighbours

        if (x - light < 0) {
            neighbours[Directions.WEST]?.getBorderLight()?.decreaseCheckX(z, light - x, reset)
        }
        if (x + light > ChunkSize.SECTION_MAX_X) {
            neighbours[Directions.EAST]?.getBorderLight()?.decreaseCheckX(z, light - (ChunkSize.SECTION_MAX_X - x), reset)
        }
    }

    private fun decreaseCheckX(z: Int, light: Int, reset: Boolean) {
        val neighbours = chunk.neighbours
        if (reset) reset()

        if (z - light < 0) {
            val neighbour = neighbours[Directions.NORTH]?.getBorderLight()
            if (reset) neighbour?.reset()
        }
        if (z + light > ChunkSize.SECTION_MAX_Z) {
            val neighbour = neighbours[Directions.SOUTH]?.getBorderLight()
            if (reset) neighbour?.reset()
        }
    }

    fun reset() {
        Arrays.fill(this.light, 0x00)
    }

    fun update(array: LightArray) {
        // ToDo: Save light from server
    }
}
