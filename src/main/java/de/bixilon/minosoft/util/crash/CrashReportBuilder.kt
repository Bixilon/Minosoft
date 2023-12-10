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

package de.bixilon.minosoft.util.crash

import de.bixilon.minosoft.util.crash.CrashReportUtil.removeTrailingNewline
import de.bixilon.minosoft.util.crash.section.AbstractCrashSection

class CrashReportBuilder {
    private val sections: MutableList<AbstractCrashSection> = mutableListOf()

    operator fun plusAssign(section: AbstractCrashSection) {
        sections += section
    }


    fun build(): String {
        val builder = StringBuilder()
        builder += """----- Minosoft Crash Report -----"""
        builder.append("// ").append(CrashReportUtil.CRASH_REPORT_COMMENTS.random())


        for (section in sections) {
            if (section.isEmpty) {
                continue
            }
            builder.appendLine()
            builder.appendLine()
            builder.append("-- ")
            builder.append(section.name)
            builder.append(" --")
            builder.appendLine()
            section.append(builder, CrashReportUtil.INTENT)
            builder.removeTrailingNewline()
        }

        return builder.toString()
    }

    private operator fun StringBuilder.plusAssign(string: String) {
        append(string)
        append('\n')
    }
}
