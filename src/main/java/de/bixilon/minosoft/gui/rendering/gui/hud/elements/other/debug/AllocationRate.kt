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

package de.bixilon.minosoft.gui.rendering.gui.hud.elements.other.debug

import de.bixilon.kutil.concurrent.schedule.RepeatedTask
import de.bixilon.kutil.concurrent.schedule.TaskScheduler
import de.bixilon.kutil.unit.Bytes.Companion.bytes
import kotlin.time.Duration.Companion.seconds

object AllocationRate {
    private val RUNTIME = Runtime.getRuntime()
    var allocationRate = 0.bytes
        private set
    private var previous = 0.bytes

    init {
        TaskScheduler += RepeatedTask(1.seconds) { tick() }
    }

    private fun tick() {
        val previous = this.previous
        val allocated = RUNTIME.totalMemory().bytes - RUNTIME.freeMemory().bytes
        this.previous = allocated
        if (allocated.bytes < previous.bytes) {
            // gc was active
            return
        }
        this.allocationRate = allocated - previous
    }
}
