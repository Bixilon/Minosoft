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

package de.bixilon.minosoft.terminal.cli

import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.observer.DataObserver.Companion.observed
import de.bixilon.kutil.shutdown.AbstractShutdownReason
import de.bixilon.kutil.shutdown.ShutdownManager
import de.bixilon.kutil.string.WhitespaceUtil.trimWhitespaces
import de.bixilon.minosoft.commands.nodes.RootNode
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
    var connection: PlayConnection? by observed(null)
    val ROOT_NODE = RootNode()

    init {
        register()
    }

    @Synchronized
    private fun register() {
        ROOT_NODE.clear()
        val connection = this.connection

        for (command in Commands.COMMANDS) {
            if (command is ConnectionCommand && connection == null) {
                continue
            }
            ROOT_NODE.addChild(command.node)
        }
    }


    fun startThread(latch: CountUpAndDownLatch) {
        latch.inc()
        Thread({ latch.dec(); startLoop() }, "CLI").start()
    }

    private fun startLoop() {
        val builder = TerminalBuilder.builder()

        val terminal: Terminal = builder.build()
        val reader: LineReader = LineReaderBuilder.builder()
            .appName("Minosoft")
            .terminal(terminal)
            .completer(NodeCompleter)
            .build()

        this::connection.observe(this) { register() }


        while (true) {
            try {
                val line: String = reader.readLine().trimWhitespaces()
                if (line.isBlank()) {
                    continue
                }
                terminal.flush()
                ROOT_NODE.execute(line, connection)
            } catch (exception: EndOfFileException) {
                Log.log(LogMessageType.GENERAL, LogLevels.VERBOSE) { exception.printStackTrace() }
                Log.log(LogMessageType.GENERAL, LogLevels.WARN) { "End of file error in cli thread. Disabling cli." }
                break
            } catch (exception: UserInterruptException) {
                ShutdownManager.shutdown(reason = AbstractShutdownReason.DEFAULT)
            } catch (exception: Throwable) {
                exception.printStackTrace()
            }
        }
    }

    object NodeCompleter : Completer {

        override fun complete(reader: LineReader, line: ParsedLine, candidates: MutableList<Candidate>) {
            val suggestions = ROOT_NODE.getSuggestions(line.line())
            for (suggestion in suggestions) {
                candidates += Candidate(suggestion.toString())
            }
        }
    }
}
