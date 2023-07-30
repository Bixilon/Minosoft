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

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseActions
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseButtons

class KeyClickCounter(
    maxDelay: Int = ClickCounter.MAX_DELAY,
    minDelayBetween: Int = ClickCounter.MIN_DELAY_BETWEEN,
) : ClickCounter {
    val press = SingleClickCounter(maxDelay, minDelayBetween)
    val release = SingleClickCounter(maxDelay, minDelayBetween)

    override fun getClicks(buttons: MouseButtons, action: MouseActions, position: Vec2, time: Long): Int {
        return if (action == MouseActions.PRESS) {
            press.getClicks(buttons, action, position, time)
        } else {
            release.getClicks(buttons, action, position, time)
        }
    }
}
