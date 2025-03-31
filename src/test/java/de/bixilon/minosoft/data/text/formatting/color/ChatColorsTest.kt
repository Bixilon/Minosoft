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

package de.bixilon.minosoft.data.text.formatting.color

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ChatColorsTest {

    @Test
    fun `get yellow`() {
        assertEquals(ChatColors["yellow"], RGBAColor(255, 255, 85))
    }

    @Test
    fun `get name of gold`() {
        assertEquals(ChatColors.NAME_MAP.getKey(RGBAColor(255, 170, 0)), "gold")
    }

    @Test
    fun `get a`() {
        assertEquals(ChatColors.VALUES.getOrNull(Character.digit('a', 16)), RGBAColor(85, 255, 85))
    }

    @Test
    fun `get char of red`() {
        assertEquals(ChatColors.getChar(RGBAColor(170, 0, 0)), "4")
    }
}
