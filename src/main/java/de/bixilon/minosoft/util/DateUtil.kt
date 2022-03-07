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

package de.bixilon.minosoft.util

import de.bixilon.kutil.time.TimeUtil
import java.util.*

@Deprecated("Part of kutil 1.10")
object DateUtil {
    val currentCalendar: Calendar
        get() {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = TimeUtil.time
            return calendar
        }

    val christmas: Boolean
        get() {
            val calendar = currentCalendar
            if (calendar.get(Calendar.MONTH) != Calendar.DECEMBER) {
                return false
            }
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            return day in 24..26
        }

    val newYear: Boolean
        get() {
            val calendar = currentCalendar
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            return (month == Calendar.DECEMBER && day == 31) || (month == Calendar.JANUARY && day == 1)
        }
}
