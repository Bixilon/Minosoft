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
import de.bixilon.minosoft.data.world.chunk.light.types.LightLevel
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition

class SectionLight(
    private val section: ChunkSection,
    private val light: LightArray = LightArray(),
) : AbstractSectionLight {

    fun onBlockChange(position: InSectionPosition, previous: BlockState?, state: BlockState?) {
        val previousLuminance = previous?.luminance ?: 0
        val luminance = state?.luminance ?: 0

        when {
            luminance == previousLuminance -> Unit // TODO: check if light properties changed
            luminance > previousLuminance -> onIncrease(position, luminance)
            luminance < previousLuminance -> onDecrease(position)
        }
    }

    private fun onIncrease(position: InSectionPosition, luminance: Int) {}
    private fun onDecrease(position: InSectionPosition) {}

    fun trace(position: InSectionPosition) {
    }

    fun trace(position: InSectionPosition, level: LightLevel) {
    }


    override fun clear() = this.light.clear()
    fun calculate() {
        for (index in 0 until ProtocolDefinition.BLOCKS_PER_SECTION) {
            trace(InSectionPosition(index))
        }
    }

    override fun propagate() = Unit // TODO

    override fun update(array: LightArray) = TODO("Save light from server")

    override fun get(position: InSectionPosition) = light[position]
}
