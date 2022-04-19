/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.input.count

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseActions
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseButtons
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.abs
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.isGreater

class SingleClickCounter(
    val maxDelay: Int = ClickCounter.MAX_DELAY,
    val minDelayBetween: Int = ClickCounter.MIN_DELAY_BETWEEN,
) : ClickCounter {
    private var lastPosition = Vec2i(-1, -1)
    private var lastChange = -1L
    private var lastDoubleChange = -1L

    override fun getClicks(buttons: MouseButtons, action: MouseActions, position: Vec2i, time: Long): Int {
        val lastPosition = lastPosition
        this.lastPosition = position
        val lastMousePress = lastChange
        this.lastChange = time

        if (time - lastDoubleChange < minDelayBetween) {
            return 1
        }
        if (time - lastMousePress < maxDelay) {
            if ((lastPosition - position).abs isGreater MAX_MOUSE_MOVE) {
                return 1
            }
            lastDoubleChange = time
            return 2
        }
        return 1
    }

    companion object {
        val MAX_MOUSE_MOVE = Vec2i(10, 10)
    }
}
