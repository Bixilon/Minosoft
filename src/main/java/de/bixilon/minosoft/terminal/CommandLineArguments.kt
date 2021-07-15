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

import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.impl.Arguments
import net.sourceforge.argparse4j.inf.ArgumentParserException
import net.sourceforge.argparse4j.inf.Namespace
import kotlin.system.exitProcess

object CommandLineArguments {
    private val PARSER = with(ArgumentParsers.newFor("Minosoft").build()
        .defaultHelp(true)
        .description("An open source minecraft client written from scratch")) {


        addArgument("--disable_log_color_message")
            .action(Arguments.storeFalse())
            .help("The message (after all prefixes) should be colored with ANSI color codes")
        addArgument("--disable_log_color_level")
            .action(Arguments.storeFalse())
            .help("The level (e.g. [INFO]) should be colored")
        addArgument("--disable_log_color_type")
            .action(Arguments.storeFalse())
            .help("The type (e.g. [OTHER]) should be colored")
        addArgument("--relative_log")
            .action(Arguments.storeTrue())
            .help("Prefixes all log messages with relative time instead of absolute time")

        addArgument("--disable_server_list")
            .action(Arguments.storeTrue())
            .help("Disables the server list")

        addArgument("--disable_rendering")
            .action(Arguments.storeTrue())
            .help("Disables rendering")

        addArgument("--headless")
            .action(Arguments.storeTrue())
            .help("Disables the server list and rendering")

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

        RunConfiguration.LOG_COLOR_MESSAGE = namespace.getBoolean("disable_log_color_message")
        RunConfiguration.LOG_COLOR_LEVEL = namespace.getBoolean("disable_log_color_level")
        RunConfiguration.LOG_COLOR_TYPE = namespace.getBoolean("disable_log_color_type")
        RunConfiguration.LOG_RELATIVE_TIME = namespace.getBoolean("relative_log")

        RunConfiguration.DISABLE_SERVER_LIST = namespace.getBoolean("disable_server_list")
        RunConfiguration.DISABLE_RENDERING = namespace.getBoolean("disable_rendering")

        if (namespace.getBoolean("headless")) {
            RunConfiguration.DISABLE_SERVER_LIST = true
            RunConfiguration.DISABLE_RENDERING = true
        }

    }
}
