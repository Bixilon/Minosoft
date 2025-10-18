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
import de.bixilon.minosoft.data.world.chunk.ChunkSize
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.chunk.light.types.LightArray
import de.bixilon.minosoft.data.world.chunk.light.types.LightLevel
import de.bixilon.minosoft.data.world.positions.InSectionPosition

class BottomSectionLight(
    chunk: Chunk,
) : BorderSectionLight(chunk) {

    override fun get(position: InSectionPosition): LightLevel {
        if (position.y != ChunkSize.SECTION_MAX_Y) return LightLevel.EMPTY
        return LightLevel(this.light[position.xz])
    }

    override fun getNearestSection() = chunk.sections.getFirst()
    override fun Chunk.getBorderLight() = this.light.bottom

    override fun update(array: LightArray) {
        System.arraycopy(array.array, InSectionPosition(0, ProtocolDefinition.SECTION_MAX_Y, 0).index, this.light, 0, ProtocolDefinition.SECTION_WIDTH_X * ProtocolDefinition.SECTION_WIDTH_Z)
    }
}
