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

import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.Test

class GaussianKernelTest {

    @Test
    fun `2d kernel with radius 1`() {
        val kernel = GaussianKernel.get2D(1)
        assertEquals(kernel.array.size, 5)
    }

    @Test
    fun `2d kernel with radius 15`() {
        val kernel = GaussianKernel.get2D(15)
        assertEquals(kernel.array.size, 709)
    }

    @Test
    fun `3d kernel with radius 1`() {
        val kernel = GaussianKernel.get3D(1)
        assertEquals(kernel.array.size, 7)
    }

    @Test
    fun `3d kernel with radius 15`() {
        val kernel = GaussianKernel.get3D(15)
        assertEquals(kernel.array.size, 14147)
    }
}
