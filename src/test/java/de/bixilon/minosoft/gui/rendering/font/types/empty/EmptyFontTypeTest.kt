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

package de.bixilon.minosoft.gui.rendering.font.types.empty

import de.bixilon.kutil.json.JsonObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class EmptyFontTypeTest {

    @Test
    fun empty1() {
        val json: JsonObject = mapOf()
        assertNull(EmptyFontType.load(json))
    }

    @Test
    fun empty2() {
        val json: JsonObject = mapOf("advances" to mapOf<String, Any>())
        assertNull(EmptyFontType.load(json))
    }

    @Test
    fun singleChar() {
        val json: JsonObject = mapOf("advances" to mapOf(" " to 10))
        assertEquals(EmptyFontType.load(json), mapOf(' '.code to EmptyCodeRenderer(10)))
    }

    @Test
    fun multipleChars() {
        val json: JsonObject = mapOf("advances" to mapOf(" " to 10, "a" to 0))
        assertEquals(EmptyFontType.load(json), mapOf(' '.code to EmptyCodeRenderer(10), 'a'.code to EmptyCodeRenderer(0)))
    }

    @Test
    fun invalidWith() {
        val json: JsonObject = mapOf("advances" to mapOf("r" to 1037))
        assertThrows<IllegalArgumentException> { EmptyFontType.load(json) }
    }
}
