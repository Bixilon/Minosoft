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
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.chunk.light.section.AbstractSectionLight
import de.bixilon.minosoft.data.world.chunk.light.types.LightLevel
import de.bixilon.minosoft.data.world.chunk.update.AbstractWorldUpdate
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition.SECTION_MAX_X
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition.SECTION_MAX_Z
import java.util.*

abstract class BorderSectionLight(
    val chunk: Chunk,
) : AbstractSectionLight {
    protected var event = false
    protected val light = ByteArray(ProtocolDefinition.SECTION_WIDTH_X * ProtocolDefinition.SECTION_WIDTH_Z)

    abstract fun Chunk.getBorderLight(): BorderSectionLight

    protected inline operator fun get(index: Int) = LightLevel(this.light[index])
    protected inline operator fun set(index: Int, value: LightLevel) {
        this.light[index] = value.raw
        event = true
    }

    abstract fun trace(position: InSectionPosition, level: LightLevel)

    override fun clear() {
        Arrays.fill(this.light, 0x00)
        event = true
    }

    override fun traceFrom(position: InSectionPosition, direction: Directions) {
        val current = LightLevel(light[position.xz])
        if (current.block <= 1) return

        val next = current.decrease()

        when (direction) {
            Directions.WEST -> traceWest(position, next)
            Directions.EAST -> traceEast(position, next)
            Directions.NORTH -> traceNorth(position, next)
            Directions.SOUTH -> traceSouth(position, next)
            else -> Broken("Not a vertical direction: $direction")
        }
    }

    protected inline fun traceWest(position: InSectionPosition, next: LightLevel) {
        if (position.x > 0) {
            trace(position.minusX(), next)
        } else {
            chunk.neighbours[Directions.WEST]?.getBorderLight()?.trace(position.with(x = SECTION_MAX_X), next)
        }
    }

    protected inline fun traceEast(position: InSectionPosition, next: LightLevel) {
        if (position.x < SECTION_MAX_X) {
            trace(position.plusX(), next)
        } else {
            chunk.neighbours[Directions.EAST]?.getBorderLight()?.trace(position.with(x = 0), next)
        }
    }

    protected inline fun traceNorth(position: InSectionPosition, next: LightLevel) {
        if (position.z > 0) {
            trace(position.minusZ(), next)
        } else {
            chunk.neighbours[Directions.NORTH]?.getBorderLight()?.trace(position.with(z = SECTION_MAX_Z), next)
        }
    }

    protected inline fun traceSouth(position: InSectionPosition, next: LightLevel) {
        if (position.z < SECTION_MAX_X) {
            trace(position.plusZ(), next)
        } else {
            chunk.neighbours[Directions.SOUTH]?.getBorderLight()?.trace(position.with(z = 0), next)
        }
    }

    protected inline fun traceVertical(position: InSectionPosition, next: LightLevel) {
        traceWest(position, next)
        traceEast(position, next)
        traceNorth(position, next)
        traceSouth(position, next)
    }

    override fun fireEvent(): AbstractWorldUpdate? {
        if (!event) return null

        // TODO: fire event
        return null
    }

    override fun calculate() = Unit // TODO: bottom sky light

    protected fun propagateVertical() {
        for (x in 0 until SECTION_MAX_X) {
            chunk.neighbours[Directions.NORTH]?.light?.bottom?.traceFrom(InSectionPosition(x, 0, SECTION_MAX_Z), Directions.SOUTH)
            chunk.neighbours[Directions.SOUTH]?.light?.bottom?.traceFrom(InSectionPosition(x, 0, 0), Directions.SOUTH)
        }
        for (z in 0 until SECTION_MAX_Z) {
            chunk.neighbours[Directions.WEST]?.light?.bottom?.traceFrom(InSectionPosition(SECTION_MAX_X, 0, z), Directions.EAST)
            chunk.neighbours[Directions.EAST]?.light?.bottom?.traceFrom(InSectionPosition(0, 0, z), Directions.WEST)
        }
    }
}
