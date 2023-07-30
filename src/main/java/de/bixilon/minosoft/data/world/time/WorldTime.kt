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

package de.bixilon.minosoft.data.world.time

import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import kotlin.math.abs

class WorldTime(
    time: Int = 0,
    age: Long = 0L,
) {
    val time = abs(time) % ProtocolDefinition.TICKS_PER_DAY
    val cycling = time >= 0

    val age = abs(age)

    val moonPhase = MoonPhases[(this.age / ProtocolDefinition.TICKS_PER_DAY % MoonPhases.VALUES.size).toInt()] // ToDo: Verify
    val phase = DayPhases.of(this.time)
    val progress = phase.getProgress(this.time)

    val day = (this.age + 6000) / ProtocolDefinition.TICKS_PER_DAY // day changes at midnight (18k)
}
