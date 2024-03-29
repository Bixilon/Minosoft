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

package de.bixilon.minosoft.data.registries.dimension

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


class DimensionPropertiesTest {

    @Test
    fun create1SectionHigh() {
        val minY = 0
        val height = 16
        val properties = DimensionProperties(minY = minY, height = height)
        assertEquals(1, properties.sections)
        assertEquals(0, properties.maxSection)
        assertEquals(15, properties.maxY)
    }

    @Test
    fun negativeY() {
        val minY = -16
        val height = 32
        val properties = DimensionProperties(minY = minY, height = height)
        assertEquals(2, properties.sections)
        assertEquals(0, properties.maxSection)
        assertEquals(15, properties.maxY)
    }

    @Test
    fun `legacy dimension`() {
        val properties = DimensionProperties()
        assertEquals(16, properties.sections)
        assertEquals(0, properties.minSection)
        assertEquals(15, properties.maxSection)
        assertEquals(255, properties.maxY)
    }

    @Test
    fun `default of deserialization`() {
        val properties = DimensionProperties.deserialize(null, emptyMap())
        assertEquals(properties, DimensionProperties())
    }
}
