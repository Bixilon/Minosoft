/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.terminal

import de.bixilon.minosoft.config.StaticConfiguration
import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.inf.ArgumentParserException
import net.sourceforge.argparse4j.inf.Namespace
import kotlin.system.exitProcess

object CommandLineArguments {
    private val PARSER = with(ArgumentParsers.newFor("Minosoft").build()
        .defaultHelp(true)
        .description("An open source minecraft client written from scratch")) {

        addArgument("--relative_log")
            .choices(true, false)
            .help("Prefixes all log messages with relative time instead of absolute time")
            .default = false

        this
    }

    fun parse(args: Array<String>) {
        val namespace: Namespace
        try {
            namespace = PARSER.parseArgs(args)
        } catch (exception: ArgumentParserException) {
            PARSER.handleError(exception)
            exitProcess(1)
        }

        StaticConfiguration.LOG_RELATIVE_TIME = namespace.getBoolean("relative_log")
    }
}
