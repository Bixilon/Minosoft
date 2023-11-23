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

package de.bixilon.minosoft.terminal

import de.bixilon.kutil.shutdown.AbstractShutdownReason
import de.bixilon.kutil.shutdown.ShutdownManager
import de.bixilon.minosoft.assets.util.AssetsOptions
import de.bixilon.minosoft.gui.rendering.system.base.RenderSystemFactory
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGLRenderSystem
import de.bixilon.minosoft.gui.rendering.system.window.WindowFactory
import de.bixilon.minosoft.gui.rendering.system.window.glfw.GLFWWindowFactory
import de.bixilon.minosoft.modding.loader.parameters.ModParameters
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.json.Jackson
import de.bixilon.minosoft.util.logging.Log
import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.impl.Arguments
import net.sourceforge.argparse4j.inf.ArgumentParserException
import net.sourceforge.argparse4j.inf.Namespace
import java.io.PrintWriter
import java.nio.file.Path

object CommandLineArguments {
    lateinit var ARGUMENTS: List<String>
        private set
    private val PARSER = ArgumentParsers.newFor("Minosoft").build()
        .defaultHelp(true)
        .description("An open source minecraft client written from scratch")
        .apply {
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

            addArgument("--disable_eros")
                .action(Arguments.storeTrue())
                .help("Disables EROS (Launcher like graphical user interface)")

            addArgument("--disable_rendering")
                .action(Arguments.storeTrue())
                .help("Disables rendering")

            addArgument("--disable_cursor_catch")
                .action(Arguments.storeTrue())
                .help("Disables catching the cursor")

            addArgument("--headless")
                .action(Arguments.storeTrue())
                .help("Disables the server list and rendering")

            addArgument("--connect")
                .setDefault(null)
                .action(Arguments.store())
                .help("Automatically connects to a specific server. Full format: hostname.whatever<:port><,version><,account>\nPort is by default ${ProtocolDefinition.DEFAULT_PORT}, version is automatic and account is the current selected one")

            addArgument("--disable_profile_hot_reloading")
                .action(Arguments.storeTrue())
                .help("Disables profile hot reloading")

            addArgument("--verbose")
                .action(Arguments.storeTrue())
                .help("Enables verbose logging (only affects pre profiles loading stage)")

            addArgument("--ignore_yggdrasil")
                .action(Arguments.storeTrue())
                .help("Disable all yggdrasil (mojang) signature checking")

            addArgument("--ignore_mods")
                .action(Arguments.storeTrue())
                .help("Ignores all mods and disable mod loading")

            addArgument("--mod_parameters")
                .action(Arguments.store())
                .help("JSON of custom mod parameters")

            addArgument("--home")
                .action(Arguments.store())
                .help("Home path of minosoft")

            addArgument("--assets")
                .action(Arguments.store())
                .help("Path where assets are stored")

            addArgument("--config")
                .action(Arguments.store())
                .help("Path where minosoft configuration files are stored")

            addArgument("--window")
                .action(Arguments.store())
                .help("Window library to use. Defaults to glfw")

            addArgument("--render-api")
                .action(Arguments.store())
                .help("Render API to use. Defaults to gl")
        }

    fun parse(args: Array<String>) {
        check(!this::ARGUMENTS.isInitialized) { "Already initialized!" }
        this.ARGUMENTS = args.toList()
        val namespace: Namespace
        try {
            namespace = PARSER.parseArgs(args)
        } catch (exception: ArgumentParserException) {
            PARSER.handleError(exception, PrintWriter(Log.FATAL_PRINT_STREAM))
            return ShutdownManager.shutdown(reason = AbstractShutdownReason.CRASH)
        }

        RunConfiguration.LOG_COLOR_MESSAGE = namespace.getBoolean("disable_log_color_message")
        RunConfiguration.LOG_COLOR_LEVEL = namespace.getBoolean("disable_log_color_level")
        RunConfiguration.LOG_COLOR_TYPE = namespace.getBoolean("disable_log_color_type")
        RunConfiguration.LOG_RELATIVE_TIME = namespace.getBoolean("relative_log")
        RunConfiguration.DISABLE_CURSOR_CATCH = namespace.getBoolean("disable_cursor_catch")
        RunConfiguration.DISABLE_EROS = namespace.getBoolean("disable_eros")
        RunConfiguration.DISABLE_RENDERING = namespace.getBoolean("disable_rendering")

        if (namespace.getBoolean("headless")) {
            RunConfiguration.DISABLE_EROS = true
            RunConfiguration.DISABLE_RENDERING = true
        }

        RunConfiguration.AUTO_CONNECT_TO = namespace.getString("connect")


        RunConfiguration.PROFILES_HOT_RELOADING = !namespace.getBoolean("disable_profile_hot_reloading")
        RunConfiguration.VERBOSE_LOGGING = namespace.getBoolean("verbose")

        RunConfiguration.IGNORE_YGGDRASIL = namespace.getBoolean("ignore_yggdrasil")
        RunConfiguration.IGNORE_MODS = namespace.getBoolean("ignore_mods")
        namespace.getString("mod_parameters")?.let { RunConfiguration.MOD_PARAMETERS = Jackson.MAPPER.readValue(it, ModParameters::class.java) }



        namespace.getString("home")?.let { RunConfiguration.setHome(Path.of(it)) }
        namespace.getString("assets")?.let { AssetsOptions.PATH = Path.of(it) }
        namespace.getString("config")?.let { RunConfiguration.setConfig(Path.of(it)) }

        setWindowFactory(namespace.getString("window")?.lowercase() ?: "glfw")
        setRenderApi(namespace.getString("render_api")?.lowercase() ?: "gl")
    }

    private fun setWindowFactory(name: String) {
        WindowFactory.factory = when (name) {
            "glfw" -> GLFWWindowFactory
            else -> throw IllegalStateException("Unknown window library: $name")
        }
    }

    private fun setRenderApi(name: String) {
        RenderSystemFactory.factory = when (name) {
            "gl" -> OpenGLRenderSystem
            else -> throw IllegalStateException("Unknown render api: $name")
        }
    }
}
