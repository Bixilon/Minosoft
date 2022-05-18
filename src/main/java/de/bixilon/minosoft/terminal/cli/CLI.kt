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
import de.bixilon.minosoft.ShutdownReasons
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.terminal.commands.Commands
import de.bixilon.minosoft.util.ShutdownManager
import org.jline.reader.*
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder

object CLI {
    var connection: PlayConnection? = null


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

        while (true) {
            try {
                val line: String = reader.readLine().removeDuplicatedWhitespaces()
                if (line.isBlank()) {
                    continue
                }
                terminal.flush()
                Commands.ROOT_NODE.execute(line)
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
            val suggestions = Commands.ROOT_NODE.getSuggestions(line.line())
            for (suggestion in suggestions) {
                candidates += Candidate(suggestion.toString())
            }
        }
    }
}
