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

package de.bixilon.minosoft.gui.rendering.util.mesh.uv

import de.bixilon.minosoft.gui.rendering.util.mesh.uv.array.PackedUVArray
import de.bixilon.minosoft.gui.rendering.util.mesh.uv.array.UnpackedUVArray
import org.testng.Assert.assertEquals
import org.testng.annotations.Test


@Test(groups = ["rendering"])
class PackedUVTest {

    fun `pack 0_0`() {
        val packed = PackedUVArray.pack(0.0f, 0.0f)
        assertEquals(packed.toBits(), 0x000_000)
    }

    fun `pack 1_0`() {
        val packed = PackedUVArray.pack(1.0f, 0.0f)
        assertEquals(packed.toBits(), 0xFFF_000)
    }

    fun `pack 0_1`() {
        val packed = PackedUVArray.pack(0.0f, 1.0f)
        assertEquals(packed.toBits(), 0x000_FFF)
    }

    fun `pack 1_1`() {
        val packed = PackedUVArray.pack(1.0f, 1.0f)
        assertEquals(packed.toBits(), 0xFFF_FFF)
    }

    fun `pack 0,5_0,5`() {
        val packed = PackedUVArray.pack(0.5f, 0.5f)
        assertEquals(packed.toBits(), 0x7FF_7FF)
    }

    fun `upack 0_0`() {
        assertEquals(UnpackedUVArray.unpackU(Float.fromBits(0x000_000)), 0.0f)
        assertEquals(UnpackedUVArray.unpackV(Float.fromBits(0x000_000)), 0.0f)
    }

    fun `upack 1_0`() {
        assertEquals(UnpackedUVArray.unpackU(Float.fromBits(0xFFF_000)), 1.0f)
        assertEquals(UnpackedUVArray.unpackV(Float.fromBits(0xFFF_000)), 0.0f)
    }

    fun `upack 0_1`() {
        assertEquals(UnpackedUVArray.unpackU(Float.fromBits(0x000_FFF)), 0.0f)
        assertEquals(UnpackedUVArray.unpackV(Float.fromBits(0x000_FFF)), 1.0f)
    }

    fun `upack 1_1`() {
        assertEquals(UnpackedUVArray.unpackU(Float.fromBits(0xFFF_FFF)), 1.0f)
        assertEquals(UnpackedUVArray.unpackV(Float.fromBits(0xFFF_FFF)), 1.0f)
    }
}
