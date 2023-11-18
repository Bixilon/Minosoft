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

package de.bixilon.minosoft.data.registries.blocks.properties.list

import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.properties.EnumProperty
import de.bixilon.minosoft.data.registries.blocks.properties.primitives.BooleanProperty
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["block"])
class MapPropertyListTest {

    fun `unpacking single boolean property`() {
        val list = MapPropertyList()
        val property = BooleanProperty("test")
        list += property
        assertEquals(list.unpack().toSet(), setOf(mapOf(property to false), mapOf(property to true)))
    }

    fun `unpacking single enum property`() {
        val list = MapPropertyList()
        val property = EnumProperty("test", Directions)
        list += property
        assertEquals(list.unpack().toSet(), setOf(
            mapOf(property to Directions.DOWN),
            mapOf(property to Directions.UP),
            mapOf(property to Directions.NORTH),
            mapOf(property to Directions.SOUTH),
            mapOf(property to Directions.WEST),
            mapOf(property to Directions.EAST),
        ))
    }

    fun `unpacking 2 boolean properties`() {
        val list = MapPropertyList()
        val a = BooleanProperty("a")
        val b = BooleanProperty("b")
        list += a; list += b
        assertEquals(list.unpack().toSet(), setOf(
            mapOf(a to false, b to false),
            mapOf(a to false, b to true),
            mapOf(a to true, b to false),
            mapOf(a to true, b to true),
        ))
    }
}
