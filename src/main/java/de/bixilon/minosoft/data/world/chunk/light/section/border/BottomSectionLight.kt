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

import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.world.chunk.ChunkSize.SECTION_HEIGHT_Y
import de.bixilon.minosoft.data.world.chunk.ChunkSize.SECTION_MAX_Y
import de.bixilon.minosoft.data.world.chunk.ChunkSize.SECTION_WIDTH_X
import de.bixilon.minosoft.data.world.chunk.ChunkSize.SECTION_WIDTH_Z
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.chunk.light.types.LightArray
import de.bixilon.minosoft.data.world.chunk.light.types.LightLevel
import de.bixilon.minosoft.data.world.positions.InSectionPosition

class BottomSectionLight(
    chunk: Chunk,
) : BorderSectionLight(chunk) {

    override fun get(position: InSectionPosition): LightLevel {
        if (position.y != SECTION_MAX_Y) return LightLevel.EMPTY
        return LightLevel(this.light[position.xz])
    }

    override fun Chunk.getBorderLight() = this.light.bottom

    override fun traceFrom(direction: Directions) {
        if (direction != Directions.UP) Broken()
        val above = chunk[chunk.minSection]?.light ?: return
        for (xz in 0 until SECTION_WIDTH_X * SECTION_WIDTH_Z) {
            val position = InSectionPosition(xz)
            val current = LightLevel(light[xz])
            if (current.block <= 1) return

            val next = current.decrease()
            above.trace(position.with(y = 0), next, Directions.UP)
        }
    }

    override fun trace(position: InSectionPosition, level: LightLevel) {
        var level = level
        val current = LightLevel(light[position.xz])
        if (current.block >= level.block && current.sky >= level.sky) return


        val height = chunk.light.heightmap[position.xz]
        if ((chunk.minSection - 1) * SECTION_HEIGHT_Y + SECTION_MAX_Y >= height) {
            level = level.with(sky = 0) // level is set with heightmap, no need to trace anything
        }

        light[position.xz] = level.raw
        if (level.block <= 1 && level.sky <= 1) return // can not decrease any further

        val next = level.decrease()

        chunk.getOrPut(chunk.minSection)?.light?.trace(position.with(y = SECTION_MAX_Y), next, Directions.UP)
        traceVertical(position, next)
    }

    fun traceSky(position: InSectionPosition) {
        val level = LightLevel(light[position.xz]).with(sky = LightLevel.MAX_LEVEL)
        val next = level.decrease()

        traceVertical(position, next)
    }

    override fun update(array: LightArray) {
        System.arraycopy(array.array, InSectionPosition(0, SECTION_MAX_Y, 0).index, this.light, 0, SECTION_WIDTH_X * SECTION_WIDTH_Z)
    }

    override fun propagate() {
        super.propagateVertical()

        val section = chunk[chunk.minSection]?.light ?: return
        for (xz in 0 until SECTION_WIDTH_X * SECTION_WIDTH_Z) {
            val position = InSectionPosition(xz).with(y = 0)
            section.traceFrom(position, Directions.DOWN)
        }
    }
}
