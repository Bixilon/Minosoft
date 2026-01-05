/*
 * Minosoft
 * Copyright (C) 2020-2026 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.network.session.play.tick

import kotlin.time.Duration

@JvmInline
value class Ticks(val ticks: Int) {
    val duration: Duration get() = TickUtil.TIME_PER_TICK * ticks


    operator fun plus(other: Int) = Ticks(this.ticks + other)
    operator fun plus(other: Ticks) = this + other.ticks

    operator fun minus(other: Int) = Ticks(this.ticks - other)
    operator fun minus(other: Ticks) = this - other.ticks

    operator fun times(other: Int) = Ticks(this.ticks * other)
    operator fun times(other: Ticks) = this * other.ticks

    operator fun div(other: Int) = Ticks(this.ticks / other)
    operator fun div(other: Ticks) = this / other.ticks


    operator fun rem(other: Int) = Ticks(this.ticks % other)
    operator fun rem(other: Ticks) = this % other.ticks


    operator fun inc() = this + 1
    operator fun dec() = this - 1

    operator fun compareTo(other: Ticks) = ticks.compareTo(other.ticks)
    operator fun compareTo(other: Duration) = duration.compareTo(other)


    companion object {
        val Int.ticks get() = Ticks(this)
    }
}
