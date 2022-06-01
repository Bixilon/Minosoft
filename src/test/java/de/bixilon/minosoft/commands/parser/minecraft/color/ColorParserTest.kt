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

package de.bixilon.minosoft.commands.parser.minecraft.color

import de.bixilon.minosoft.commands.util.CommandReader
import de.bixilon.minosoft.data.text.ChatColors
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals


internal class ColorParserTest {

    @Test
    fun testYellow() {
        val reader = CommandReader("yellow")
        val parser = ColorParser()
        assertEquals(parser.parse(reader), ChatColors.YELLOW)
    }

    @Test
    fun testGreen() {
        val reader = CommandReader("green")
        val parser = ColorParser()
        assertEquals(parser.parse(reader), ChatColors.GREEN)
    }

    @Test
    fun testHexBlack() {
        val reader = CommandReader("#000000")
        val parser = ColorParser()
        assertEquals(parser.parse(reader), ChatColors.BLACK)
    }

    @Test
    fun testHexWhite() {
        val reader = CommandReader("#FFFFFF")
        val parser = ColorParser()
        assertEquals(parser.parse(reader), ChatColors.WHITE)
    }

    @Test
    fun testInvalidHex() {
        val reader = CommandReader("#FFIFFF")
        val parser = ColorParser()
        assertThrows<ColorParseError> { parser.parse(reader) }
    }

    @Test
    fun testHexDisabled() {
        val reader = CommandReader("#ABCDEF")
        val parser = ColorParser(supportsRGB = false)
        assertThrows<HexNotSupportedError> { parser.parse(reader) }
    }
}
