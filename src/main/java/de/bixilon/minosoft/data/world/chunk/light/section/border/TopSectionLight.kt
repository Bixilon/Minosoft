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

import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.chunk.light.types.LightArray
import de.bixilon.minosoft.data.world.chunk.light.types.LightLevel
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition.*

class TopSectionLight(
    chunk: Chunk,
) : BorderSectionLight(chunk) {

    override fun get(position: InSectionPosition): LightLevel {
        if (position.y != 0) return LightLevel.EMPTY
        return LightLevel(this.light[position.xz])
    }

    override fun Chunk.getBorderLight() = this.light.top

    override fun trace(position: InSectionPosition, level: LightLevel) {
        val current = LightLevel(light[position.xz])
        if (current.block >= level.block) return

        light[position.xz] = level.raw
        if (level.block <= 1) return // can not decrease any further

        val next = LightLevel(block = level.block - 1, sky = 0) // remove sky light, its always max

        chunk.getOrPut(chunk.maxSection)?.light?.trace(position.with(y = SECTION_MAX_Y), next, Directions.DOWN)
        traceVertical(position, next)
    }

    override fun update(array: LightArray) {
        System.arraycopy(array.array, InSectionPosition(0, 0, 0).index, this.light, 0, SECTION_WIDTH_X * SECTION_WIDTH_Z)
    }

    override fun propagate() {
        super.propagateVertical()

        val section = chunk[chunk.maxSection]?.light ?: return
        for (xz in 0 until SECTION_WIDTH_X * SECTION_WIDTH_Z) {
            val position = InSectionPosition(xz).with(y = SECTION_MAX_Y)
            section.traceFrom(position, Directions.UP)
        }
    }
}
