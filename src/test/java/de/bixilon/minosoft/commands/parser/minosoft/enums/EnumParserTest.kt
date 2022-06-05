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

package de.bixilon.minosoft.commands.parser.minosoft.enums

import de.bixilon.kutil.enums.EnumUtil
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.minosoft.commands.util.CommandReader
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

internal class EnumParserTest {


    @Test
    fun checkFirst() {
        val reader = CommandReader("FIRST")
        val parser = EnumParser(Tests)
        assertEquals(parser.parse(reader), Tests.FIRST)
    }

    @Test
    fun checkSecond() {
        val reader = CommandReader("SECOND")
        val parser = EnumParser(Tests)
        assertEquals(parser.parse(reader), Tests.SECOND)
    }

    @Test
    fun checkCaseInsensitivity() {
        val reader = CommandReader("sEcOnD")
        val parser = EnumParser(Tests)
        assertEquals(parser.parse(reader), Tests.SECOND)
    }

    @Test
    fun checkInvalid() {
        val reader = CommandReader("invalid")
        val parser = EnumParser(Tests)
        assertThrows<EnumParseError> { parser.parse(reader) }
    }

    enum class Tests {
        FIRST,
        SECOND,
        ;

        companion object : ValuesEnum<Tests> {
            override val VALUES: Array<Tests> = values()
            override val NAME_MAP: Map<String, Tests> = EnumUtil.getEnumValues(VALUES)
        }
    }
}
