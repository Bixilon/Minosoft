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

package de.bixilon.minosoft.data.container.stack.properties

import de.bixilon.kutil.json.MutableJsonObject
import de.bixilon.minosoft.data.text.TextComponent
import kotlin.test.Test
import kotlin.test.assertEquals

class DisplayPropertyTest {


    @Test
    fun `display properties`() {
        val nbt: MutableJsonObject = mutableMapOf("display" to mutableMapOf(
            "Name" to "display name",
            "Lore" to listOf(
                "first line",
                "second line",
            ),
        ))

        val property = DisplayProperty.of(null, nbt)
        val expected = DisplayProperty(
            displayName = TextComponent("display name"),
            lore = listOf(TextComponent("first line"), TextComponent("second line"))
        )


        assertEquals(property, expected)
        assertEquals(nbt, emptyMap())
    }
}
