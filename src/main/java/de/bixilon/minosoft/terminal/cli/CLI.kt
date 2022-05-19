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
import de.bixilon.kutil.watcher.DataWatcher.Companion.observe
import de.bixilon.kutil.watcher.DataWatcher.Companion.watched
import de.bixilon.minosoft.ShutdownReasons
import de.bixilon.minosoft.commands.nodes.RootNode
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.terminal.commands.Commands
import de.bixilon.minosoft.terminal.commands.connection.ConnectionCommand
import de.bixilon.minosoft.util.ShutdownManager
import org.jline.reader.*
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder

object CLI {
    var connection: PlayConnection? by watched(null)
    private val ROOT_NODE = RootNode()

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
                val line: String = reader.readLine().removeDuplicatedWhitespaces()
                if (line.isBlank()) {
                    continue
                }
                terminal.flush()
                ROOT_NODE.execute(line, connection)
            } catch (exception: UserInterruptException) {
                ShutdownManager.shutdown(reason = ShutdownReasons.ALL_FINE)
            } catch (exception: Throwable) {
                exception.printStackTrace()
            }
        }
    }

    fun String.removeDuplicatedWhitespaces(): String {
        return this.replace("\\s{2,}".toRegex(), "")
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
