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
import de.bixilon.minosoft.data.container.DurableTestItem1
import de.bixilon.minosoft.test.IT
import org.testng.AssertJUnit.assertEquals
import org.testng.annotations.Test

@Test(groups = ["item_stack"], dependsOnGroups = ["item"])
class HidePropertyTest {

    fun `deserialize empty`() {
        val nbt: MutableJsonObject = mutableMapOf()


        val actual = HideProperty.of(nbt)
        val expected = HideProperty(0x00)

        assertEquals(actual, expected)
    }

    fun `deserialize hide`() {
        val nbt: MutableJsonObject = mutableMapOf("HideFlags" to 0x03)


        val actual = HideProperty.of(nbt)
        val expected = HideProperty(0x03)

        assertEquals(actual, expected)
    }

    fun `serialize empty`() {
        val property = HideProperty(0x00)
        val actual = mutableMapOf<String, Any>().apply { property.writeNbt(DurableTestItem1, IT.VERSION, IT.REGISTRIES, this) }

        val expected: MutableJsonObject = mutableMapOf()

        assertEquals(actual, expected)
    }

    fun `serialize non empty`() {
        val property = HideProperty(0x03)
        val actual = mutableMapOf<String, Any>().apply { property.writeNbt(DurableTestItem1, IT.VERSION, IT.REGISTRIES, this) }

        val expected: MutableJsonObject = mutableMapOf("HideFlags" to 0x03)

        assertEquals(actual, expected)
    }
}
