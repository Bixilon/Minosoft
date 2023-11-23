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

package de.bixilon.minosoft.terminal.cli

import com.sun.jna.LastErrorException
import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.observer.DataObserver.Companion.observed
import de.bixilon.kutil.shutdown.AbstractShutdownReason
import de.bixilon.kutil.shutdown.ShutdownManager
import de.bixilon.kutil.string.WhitespaceUtil.trimWhitespaces
import de.bixilon.minosoft.commands.errors.ReaderError
import de.bixilon.minosoft.commands.nodes.RootNode
import de.bixilon.minosoft.main.MinosoftBoot
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.terminal.commands.Commands
import de.bixilon.minosoft.terminal.commands.connection.ConnectionCommand
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import org.jline.reader.*
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder

object CLI {
    const val CLI_PREFIX = '.'
    var connection: PlayConnection? by observed(null)
    val commands = RootNode()

    @Synchronized
    private fun register() {
        commands.clear()
        val connection = this.connection

        for (command in Commands.COMMANDS) {
            if (command is ConnectionCommand && connection == null) {
                continue
            }
            commands.addChild(command.node)
        }
    }


    fun startThread(latch: AbstractLatch?) {
        latch?.inc()
        Thread({ latch?.dec(); startLoop() }, "CLI").start()
    }

    private fun startLoop() {
        register()
        val builder = TerminalBuilder.builder()

        val terminal: Terminal = builder.build()
        val reader: LineReader = LineReaderBuilder.builder()
            .appName("Minosoft")
            .terminal(terminal)
            .completer(NodeCompleter)
            .build()

        this::connection.observe(this) { register() }

        MinosoftBoot.LATCH.await()

        Log.log(LogMessageType.OTHER, LogLevels.INFO) { "§aA headless input system is available, §epress tab§a or type §ehelp§a to see all available commands!" }
        reader.pollLines()
    }

    private fun LineReader.pollLines() {
        while (true) {
            val line: String
            try {
                line = readLine().trimWhitespaces().replace("\n", "").replace("\r", "")
                terminal.flush()
            } catch (exception: EndOfFileException) {
                eol(exception); break
            } catch (exception: LastErrorException) {
                eol(exception); break
            } catch (exception: UserInterruptException) {
                ShutdownManager.shutdown(reason = AbstractShutdownReason.DEFAULT)
                break
            } catch (exception: Throwable) {
                exception.printStackTrace()
                continue
            }
            if (line.isBlank()) continue

            processLine(line)
        }
    }

    private fun eol(exception: Throwable) {
        Log.log(LogMessageType.GENERAL, LogLevels.VERBOSE) { exception.printStackTrace() }
        Log.log(LogMessageType.GENERAL, LogLevels.WARN) { "End of file error in cli thread. Disabling cli." }
    }

    private fun processLine(line: String) {
        try {
            commands.execute(line, connection)
        } catch (error: ReaderError) {
            Log.log(LogMessageType.OTHER, LogLevels.WARN) { error.message }
        } catch (error: Throwable) {
            error.printStackTrace()
        }
    }

    object NodeCompleter : Completer {

        override fun complete(reader: LineReader, line: ParsedLine, candidates: MutableList<Candidate>) {
            val line = line.line()
            val suggestions = commands.getSuggestions(line)
            for (suggestion in suggestions) {
                candidates += Candidate(suggestion.text) // TODO: add offset, ...
            }
        }
    }
}
