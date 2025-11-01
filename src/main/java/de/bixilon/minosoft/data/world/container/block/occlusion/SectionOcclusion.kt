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

package de.bixilon.minosoft.data.world.container.block.occlusion

import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.cube.CubeDirections
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.world.container.block.BlockSectionDataProvider
import de.bixilon.minosoft.data.world.container.block.occlusion.SectionOcclusionTracer.isFullyOpaque

class SectionOcclusion(
    val provider: BlockSectionDataProvider,
) {
    private var occlusion = SectionOcclusionTracer.EMPTY
    private var state = OcclusionState.INVALID


    fun invalidate(notify: Boolean) {
        SectionOcclusionTracer.calculateFast(provider)?.let {
            return update(it, notify)
        }
        if (this.state == OcclusionState.INVALID) return

        state = OcclusionState.INVALID
        if (notify) notify()
    }

    fun onSet(previous: BlockState?, value: BlockState?) {
        if (previous.isFullyOpaque() == value.isFullyOpaque()) {
            return
        }
        invalidate(true)
    }

    private fun calculate() {
        val occlusion = SectionOcclusionTracer.calculate(provider)
        update(occlusion, false)
    }

    @JvmName("notify2")
    private fun notify() {
        provider.section.chunk.world.occlusion++
    }

    private fun update(occlusion: BooleanArray, notify: Boolean) {
        val same = this.occlusion.contentEquals(occlusion)
        this.occlusion = occlusion

        this.state = OcclusionState.VALID

        if (!same && notify) {
            notify()
        }
    }

    /**
     * If it is not possible to look from `in` to `out`
     */
    fun isOccluded(`in`: Directions, out: Directions): Boolean {
        if (`in` == out) {
            return false
        }
        return isOccluded(CubeDirections.getIndex(`in`, out))
    }

    fun isOccluded(index: Int): Boolean {
        if (state == OcclusionState.INVALID) {
            calculate()
        }
        return occlusion[index]
    }
}
