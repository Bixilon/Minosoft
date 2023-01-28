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

package de.bixilon.minosoft.commands.parser.minecraft.time

import de.bixilon.minosoft.commands.parser.brigadier._float.FloatParseError
import de.bixilon.minosoft.commands.util.CommandReader
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals


internal class TimeParserTest {

    @Test
    fun testNoUnit() {
        val reader = CommandReader("1235")
        assertEquals(TimeParser().parse(reader), 1235)
    }

    @Test
    fun testTickUnit() {
        val reader = CommandReader("7382t")
        assertEquals(TimeParser().parse(reader), 7382)
    }

    @Test
    fun testSecondsUnit() {
        val reader = CommandReader("64s")
        assertEquals(TimeParser().parse(reader), 64 * ProtocolDefinition.TICKS_PER_SECOND)
    }

    @Test
    fun testDaysUnit() {
        val reader = CommandReader("89d")
        assertEquals(TimeParser().parse(reader), 89 * ProtocolDefinition.TICKS_PER_DAY)
    }

    @Test
    fun testEmpty() {
        val reader = CommandReader("")
        assertThrows<FloatParseError> { TimeParser().parse(reader) }
    }

    @Test
    fun testInvalidUnit() {
        val reader = CommandReader("48i")
        assertThrows<InvalidTimeUnitError> { TimeParser().parse(reader) }
    }
}
