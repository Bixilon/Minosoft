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

package de.bixilon.minosoft.gui.rendering.gui.elements.text.fade

import de.bixilon.minosoft.protocol.network.session.play.tick.Ticks
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

data class FadingTimes(
    val `in`: Duration = 100.milliseconds,
    val stay: Duration = 100.milliseconds,
    val out: Duration = 100.milliseconds,
) {

    constructor(`in`: Ticks, stay: Ticks, out: Ticks) : this(`in`.duration, stay.duration, out.duration)

    companion object {
        val DEFAULT = FadingTimes()
        val EMPTY = FadingTimes(0.milliseconds, 0.milliseconds, 0.milliseconds)
    }
}
