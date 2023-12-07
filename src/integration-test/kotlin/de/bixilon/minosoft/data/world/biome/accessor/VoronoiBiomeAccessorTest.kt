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

package de.bixilon.minosoft.data.world.biome.accessor

import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.world.biome.accessor.noise.VoronoiBiomeAccessor
import de.bixilon.minosoft.test.ITUtil.allocate
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["biome"])
class VoronoiBiomeAccessorTest {
    private val getBiomeOffset = VoronoiBiomeAccessor::class.java.getDeclaredMethod("getBiomeOffset", Long::class.java, Int::class.java, Int::class.java, Int::class.java).apply { isAccessible = true }

    // TODO: those values are too far off. They match vanilla, yes, but I am still not going to allow that. The noise should be fairly smooth around the data
    @Test(enabled = false)
    fun testBiomeNoise1() {
        assertEquals(calculate(129, 3274, 91, 1823123L), Vec3i(32, 818, 22))
    }

    @Test(enabled = false)
    fun testBiomeNoise2() {
        assertEquals(calculate(129, 3274, 91, -123213L), Vec3i(32, 818, 22))
    }

    @Test(enabled = false)
    fun testBiomeNoise3() {
        assertEquals(calculate(-17, 3274, 91, -123213L), Vec3i(-5, 818, 22))
    }

    @Test(enabled = false)
    fun testBiomeNoise4() {
        assertEquals(calculate(-1123, 3, 1, -18209371253621313), Vec3i(-281, 0, 0))
    }

    fun testBiomeNoise5() {
        assertEquals(calculate(0, 3, 1, -33135639), Vec3i(0, 0, 0))
    }

    fun testBiomeNoise6() {
        assertEquals(calculate(16, 15, -16, 561363374), Vec3i(4, 3, -4))
    }

    fun testBiomeNoise7() {
        assertEquals(calculate(16, -15, -16, 79707367), Vec3i(4, -4, -5))
    }

    private fun calculate(x: Int, y: Int, z: Int, seed: Long): Vec3i {
        val accessor = VoronoiBiomeAccessor::class.java.allocate()
        val index = getBiomeOffset.invoke(accessor, seed, x, y, z) as Int
        return Vec3i(VoronoiBiomeAccessor.unpackX(index) + x, VoronoiBiomeAccessor.unpackY(index) + y, VoronoiBiomeAccessor.unpackZ(index) + z)
    }

    fun `packing positive offset`() {
        val x = 17
        val y = 16
        val z = 19
        val pack = VoronoiBiomeAccessor.pack(x, y, z)
        assertEquals(VoronoiBiomeAccessor.unpackX(pack), x); assertEquals(VoronoiBiomeAccessor.unpackY(pack), y); assertEquals(VoronoiBiomeAccessor.unpackZ(pack), z)
    }

    fun `packing negative offset`() {
        val x = -17
        val y = -16
        val z = -19
        val pack = VoronoiBiomeAccessor.pack(x, y, z)
        assertEquals(VoronoiBiomeAccessor.unpackX(pack), x); assertEquals(VoronoiBiomeAccessor.unpackY(pack), y); assertEquals(VoronoiBiomeAccessor.unpackZ(pack), z)
    }
}
