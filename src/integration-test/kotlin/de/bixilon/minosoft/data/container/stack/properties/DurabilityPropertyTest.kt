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
class DurabilityPropertyTest {

    fun `deserialize unbreakable`() {
        val nbt: MutableJsonObject = mutableMapOf("unbreakable" to 1.toByte())


        val actual = DurabilityProperty.of(DurableTestItem1, nbt)
        val expected = DurabilityProperty(durability = 100, unbreakable = true)

        assertEquals(actual, expected)
        assertEquals(nbt.size, 0)
    }

    fun `serialize without damage`() {
        val property = DurabilityProperty(durability = 100, unbreakable = true)
        val actual = mutableMapOf<String, Any>().apply { property.writeNbt(DurableTestItem1, IT.VERSION, IT.REGISTRIES, this) }

        val expected: MutableJsonObject = mutableMapOf("Damage" to 0, "unbreakable" to 1.toByte())

        assertEquals(actual, expected)
    }

    fun `serialize with damage`() {
        val property = DurabilityProperty(durability = 10)
        val actual = mutableMapOf<String, Any>().apply { property.writeNbt(DurableTestItem1, IT.VERSION, IT.REGISTRIES, this) }

        val expected = mutableMapOf("Damage" to 90)

        assertEquals(actual, expected)
    }
}
