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

import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseActions
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseButtons
import glm_.vec2.Vec2i

class MouseClickCounter(
    val maxDelay: Int = ClickCounter.MAX_DELAY,
    val minDelayBetween: Int = ClickCounter.MIN_DELAY_BETWEEN,
) : ClickCounter {
    private val keys: MutableMap<MouseButtons, KeyClickCounter> = mutableMapOf()

    override fun getClicks(buttons: MouseButtons, action: MouseActions, position: Vec2i, time: Long): Int {
        return keys.getOrPut(buttons) { KeyClickCounter(maxDelay, minDelayBetween) }.getClicks(buttons, action, position, time)
    }
}
