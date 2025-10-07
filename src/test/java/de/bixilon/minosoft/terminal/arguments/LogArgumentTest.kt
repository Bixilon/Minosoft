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
import com.github.ajalt.clikt.core.parse
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LogArgumentTest {

    private class TestCommand : CliktCommand() {
        val log by LogArgument()
        private var executed = false

        fun test(vararg args: String): TestCommand {
            parse(args.toList())

            if (!executed) throw IllegalStateException("Not executed!")
            return this
        }

        override fun run() {
            executed = true
        }

    }

    @Test
    fun `default no color`() {
        val command = TestCommand().test()
        assertFalse(command.log.noColor)
    }

    @Test
    fun `no color`() {
        val command = TestCommand().test("--no-color")
        assertTrue(command.log.noColor)
    }

    @Test
    fun `relative log`() {
        val command = TestCommand().test("--relative-log")
        assertTrue(command.log.relative)
    }

    @Test
    fun `verbose log`() {
        val command = TestCommand().test("--verbose")
        assertTrue(command.log.verbose)
    }
}
