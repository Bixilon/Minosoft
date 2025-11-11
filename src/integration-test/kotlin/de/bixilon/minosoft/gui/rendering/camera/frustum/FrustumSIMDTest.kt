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

package de.bixilon.minosoft.gui.rendering.camera.frustum

import de.bixilon.kmath.mat.mat4.f.Mat4f
import de.bixilon.kutil.benchmark.BenchmarkUtil
import org.testng.Assert.assertFalse
import org.testng.Assert.assertTrue
import org.testng.annotations.Test

@Test(groups = ["frustum", "rendering"])
class FrustumSIMDTest {

    private fun create(matrix: Mat4f) = FrustumSIMD.calculate(matrix)

    fun test() {
        create(Mat4f())
    }

    fun `aabb without world offset`() {
        val frustum = create(Mat4f(-0.86422914f, -0.09317832f, -0.24142957f, -0.24142484f, 0.0f, 1.3786901f, -0.26089254f, -0.26088744f, -0.2232244f, 0.36074647f, 0.9347118f, 0.9346935f, -240.01099f, -351.1156f, -635.5928f, -635.5603f).transpose())

        assertTrue(frustum.containsAABB(-430.56384f, 70.0f, 590.78735f, -430.31384f, 70.25f, 591.03735f))
        assertTrue(frustum.containsAABB(-427.9737f, 70.0f, 591.389f, -427.7237f, 70.25f, 591.639f))
        assertTrue(frustum.containsAABB(-429.75793f, 70.0f, 589.91144f, -429.50793f, 70.25f, 590.16144f))
        assertTrue(frustum.containsAABB(-431.52737f, 82.0f, 620.15845f, -431.27737f, 82.25f, 620.40845f))
        assertTrue(frustum.containsAABB(-446.4892f, 71.0f, 597.55316f, -446.2392f, 71.25f, 597.80316f))

        assertFalse(frustum.containsAABB(-465.25f, 90.0f, 621.2639f, -465.0f, 90.25f, 621.5139f))
        assertFalse(frustum.containsAABB(-436.7665f, 71.0f, 569.5984f, -436.5165f, 71.25f, 569.8484f))
        assertFalse(frustum.containsAABB(-414.44086f, 71.0f, 574.9274f, -414.19086f, 71.25f, 575.1774f))
    }

    fun `aabb without world offset 2`() {
        val frustum = create(Mat4f(0.031299774f, 1.4272678f, 0.0017395335f, 0.0017394995f, 0.0f, 0.0024857917f, -1.0000181f, -0.99999857f, 0.89204353f, -0.050079573f, -6.103626E-5f, -6.103507E-5f, -533.13086f, 625.4982f, 80.68989f, 80.708305f).transpose())

        assertTrue(frustum.containsAABB(-412.8941f, 71.0f, 602.2575f, -412.6441f, 71.25f, 602.5075f))
        assertTrue(frustum.containsAABB(-410.9874f, 71.0f, 607.7848f, -410.7374f, 71.25f, 608.0348f))
        assertTrue(frustum.containsAABB(-417.95395f, 72.0f, 620.89374f, -417.70395f, 72.25f, 621.14374f))
        assertTrue(frustum.containsAABB(-423.38867f, 71.0f, 608.79425f, -423.13867f, 71.25f, 609.04425f))

        assertFalse(frustum.containsAABB(-412.53503f, 71.0f, 601.61346f, -412.28503f, 71.25f, 601.86346f))
        assertFalse(frustum.containsAABB(-410.40173f, 71.0f, 608.0434f, -410.15173f, 71.25f, 608.2934f))
        assertFalse(frustum.containsAABB(-418.05167f, 72.0f, 621.3851f, -417.80167f, 72.25f, 621.6351f))
        assertFalse(frustum.containsAABB(-423.73242f, 71.0f, 608.72485f, -423.48242f, 71.25f, 608.97485f))
    }

    @Test(enabled = false)
    fun benchmark() {
        val frustum = create(Mat4f(0.031299774f, 1.4272678f, 0.0017395335f, 0.0017394995f, 0.0f, 0.0024857917f, -1.0000181f, -0.99999857f, 0.89204353f, -0.050079573f, -6.103626E-5f, -6.103507E-5f, -533.13086f, 625.4982f, 80.68989f, 80.708305f).transpose())

        BenchmarkUtil.benchmark(iterations = 100000) {
            assertTrue(frustum.containsAABB(-412.8941f, 71.0f, 602.2575f, -412.6441f, 71.25f, 602.5075f))
            assertTrue(frustum.containsAABB(-410.9874f, 71.0f, 607.7848f, -410.7374f, 71.25f, 608.0348f))
            assertTrue(frustum.containsAABB(-417.95395f, 72.0f, 620.89374f, -417.70395f, 72.25f, 621.14374f))
            assertTrue(frustum.containsAABB(-423.38867f, 71.0f, 608.79425f, -423.13867f, 71.25f, 609.04425f))

            assertFalse(frustum.containsAABB(-412.53503f, 71.0f, 601.61346f, -412.28503f, 71.25f, 601.86346f))
            assertFalse(frustum.containsAABB(-410.40173f, 71.0f, 608.0434f, -410.15173f, 71.25f, 608.2934f))
            assertFalse(frustum.containsAABB(-418.05167f, 72.0f, 621.3851f, -417.80167f, 72.25f, 621.6351f))
            assertFalse(frustum.containsAABB(-423.73242f, 71.0f, 608.72485f, -423.48242f, 71.25f, 608.97485f))
        }.println()

        BenchmarkUtil.benchmark(iterations = 100000000) {
            assertTrue(frustum.containsAABB(-412.8941f, 71.0f, 602.2575f, -412.6441f, 71.25f, 602.5075f))
            assertTrue(frustum.containsAABB(-410.9874f, 71.0f, 607.7848f, -410.7374f, 71.25f, 608.0348f))
            assertTrue(frustum.containsAABB(-417.95395f, 72.0f, 620.89374f, -417.70395f, 72.25f, 621.14374f))
            assertTrue(frustum.containsAABB(-423.38867f, 71.0f, 608.79425f, -423.13867f, 71.25f, 609.04425f))

            assertFalse(frustum.containsAABB(-412.53503f, 71.0f, 601.61346f, -412.28503f, 71.25f, 601.86346f))
            assertFalse(frustum.containsAABB(-410.40173f, 71.0f, 608.0434f, -410.15173f, 71.25f, 608.2934f))
            assertFalse(frustum.containsAABB(-418.05167f, 72.0f, 621.3851f, -417.80167f, 72.25f, 621.6351f))
            assertFalse(frustum.containsAABB(-423.73242f, 71.0f, 608.72485f, -423.48242f, 71.25f, 608.97485f))
        }.println()
    }

    // TODO: test chunk, section (with camera offset)
}
