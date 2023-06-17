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

package de.bixilon.minosoft.data.entities

import de.bixilon.minosoft.data.entities.EntityRotation.Companion.interpolateYaw
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class EntityRotationTest {

    @Test
    fun interpolation1() {
        assertEquals(50.0f, interpolateYaw(0.5f, 0.0f, 100.0f))
    }

    @Test
    fun interpolation2() {
        assertEquals(0.0f, interpolateYaw(-1.0f, 0.0f, 100.0f))
    }

    @Test
    fun interpolation3() {
        assertEquals(100.0f, interpolateYaw(2.0f, 0.0f, 100.0f))
    }

    @Test
    fun interpolation4() {
        assertEquals(-180.0f, interpolateYaw(0.1f, 180.0f, -180.0f))
    }

    @Test
    fun interpolation5() {
        assertEquals(-180.0f, interpolateYaw(0.9f, 180.0f, -180.0f))
    }

    @Test
    fun interpolation6() {
        assertEquals(-170.0f, interpolateYaw(0.5f, 180.0f, -160.0f))
    }

    @Test
    fun interpolation7() {
        assertEquals(150.0f, interpolateYaw(0.5f, 110.0f, -170.0f))
    }
}
