/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.gui.hud.elements.other.debug

import de.bixilon.kutil.concurrent.schedule.RepeatedTask
import de.bixilon.kutil.concurrent.schedule.TaskScheduler

object AllocationRate {
    const val RUNS_PER_SECOND = 3
    private val RUNTIME = Runtime.getRuntime()
    var allocationRate = 0L
        private set
    private var previous = 0L

    init {
        TaskScheduler += RepeatedTask(1000 / RUNS_PER_SECOND) { tick() }
    }

    private fun tick() {
        val previous = this.previous
        val allocated = RUNTIME.totalMemory() - RUNTIME.freeMemory()
        this.previous = allocated
        if (allocated < previous) {
            // gc was active
            return
        }
        this.allocationRate = (allocated - previous) * RUNS_PER_SECOND
    }
}
