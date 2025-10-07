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

package de.bixilon.minosoft.terminal.arguments

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.groups.groupSwitch
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.eagerOption
import com.github.ajalt.clikt.parameters.options.option
import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.minosoft.commands.stack.print.StringPrintTarget
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.terminal.arguments.connect.AutoConnectArgument
import de.bixilon.minosoft.terminal.arguments.connect.AutoConnectFactory
import de.bixilon.minosoft.terminal.arguments.connect.ConnectArgument
import de.bixilon.minosoft.terminal.arguments.connect.LocalArgument
import de.bixilon.minosoft.terminal.arguments.ui.ErosArgument
import de.bixilon.minosoft.terminal.arguments.ui.RenderingArgument
import de.bixilon.minosoft.terminal.arguments.ui.UiArgument
import de.bixilon.minosoft.terminal.commands.AboutCommand.printAbout

object CommandLineArguments : CliktCommand(RunConfiguration.APPLICATION_NAME), AppliedArgument {
    val log by LogArgument()
    val mod by ModArgument()
    val mojang by MojangArgument()
    val profile by ProfileArgument()
    val storage by StorageArgument()
    val update by UpdateArgument()

    val eros by ErosArgument()
    val rendering by RenderingArgument()
    val ui by UiArgument()

    private val connector: AutoConnectFactory? by option().groupSwitch(
        "--local" to LocalArgument(),
        "--connect" to ConnectArgument(),
    )
    val connect by AutoConnectArgument { connector }

    var raw: List<String> = unsafeNull()

    init {
        eagerOption("--version") { throw PrintMessage(StringPrintTarget().printAbout().toString()) }
    }

    fun parse(args: Array<String>) {
        val list = args.toList()
        raw = list
        main(args)
    }

    override fun apply() {
        log.apply()
        mod.apply()
        mojang.apply()
        profile.apply()
        storage.apply()
        update.apply()

        eros.apply()
        rendering.apply()
        ui.apply()
    }

    override fun run() = apply()
}
