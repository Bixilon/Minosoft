/*
 * Minosoft
 * Copyright (C) 2020-2026 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.tint.sampler.gaussian

import de.bixilon.kmath.vec.VecUtil.VERIFY_VECTORS
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class GaussianSampleTest {
    @Test
    fun `init correct min`() {
        GaussianSample(-15, -15, -15, -65535)
    }

    @Test
    fun `init correct max`() {
        GaussianSample(15, 15, 15, 65535)
    }

    @Test
    fun `init badly`() {
        if (!VERIFY_VECTORS) return
        assertThrows<AssertionError> { GaussianSample(16, -16, 16, 65536) }
    }

    @Test
    fun `correct positive x`() {
        val position = GaussianSample(2, 0xF, 0xF, 0xF)
        assertEquals(position.x, 2)
    }

    @Test
    fun `correct positive x large`() {
        val position = GaussianSample(15, 0xF, 0xF, 0xF)
        assertEquals(position.x, 15)
    }

    @Test
    fun `correct negative x`() {
        val position = GaussianSample(-2, 0xF, 0xF, 0xF)
        assertEquals(position.x, -2)
    }

    @Test
    fun `correct negative x large`() {
        val position = GaussianSample(-15, 0xF, 0xF, 0xF)
        assertEquals(position.x, -15)
    }

    @Test
    fun `correct negative y`() {
        val position = GaussianSample(0xF, -5, 0xF, 0xF)
        assertEquals(position.y, -5)
    }

    @Test
    fun `correct negative y large`() {
        val position = GaussianSample(0xF, -15, 0xF, 0xF)
        assertEquals(position.y, -15)
    }

    @Test
    fun `correct positive y`() {
        val position = GaussianSample(0xF, 5, 0xF, 0xF)
        assertEquals(position.y, 5)
    }

    @Test
    fun `correct positive y large`() {
        val position = GaussianSample(0xF, 15, 0xF, 0xF)
        assertEquals(position.y, 15)
    }

    @Test
    fun `correct positive z`() {
        val position = GaussianSample(0xF, 0xF, 4, 0xF)
        assertEquals(position.z, 4)
    }

    @Test
    fun `correct positive z large`() {
        val position = GaussianSample(0, 0, 15, 0xF)
        assertEquals(position.z, 15)
    }

    @Test
    fun `correct negative z`() {
        val position = GaussianSample(0xF, 0xF, -4, 0xF)
        assertEquals(position.z, -4)
    }

    @Test
    fun `correct negative z large`() {
        val position = GaussianSample(0, 0, -15, 0xF)
        assertEquals(position.z, -15)
    }

    @Test
    fun `correct positive w`() {
        val position = GaussianSample(0xF, 0xF, 0xF, 4)
        assertEquals(position.weight, 4)
    }

    @Test
    fun `correct positive w large`() {
        val position = GaussianSample(0, 0, 0xF, 65000)
        assertEquals(position.weight, 65000)
    }

    @Test
    fun `correct negative w`() {
        val position = GaussianSample(0xF, 0xF, 0xF, -4)
        assertEquals(position.weight, -4)
    }

    @Test
    fun `correct negative w large`() {
        val position = GaussianSample(0, 0, 0xF, -65000)
        assertEquals(position.weight, -65000)
    }
}
