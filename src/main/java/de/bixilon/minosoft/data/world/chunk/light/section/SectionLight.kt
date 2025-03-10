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

import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.chunk.light.types.LightArray
import de.bixilon.minosoft.data.world.positions.InSectionPosition

class SectionLight(
    val section: ChunkSection,
    var light: LightArray = LightArray(),
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
                propagate(position)
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

    fun reset()


    fun recalculate()
    fun calculate()
    fun propagate()


    override fun get(position: InSectionPosition) = light[position]
}
