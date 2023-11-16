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

package de.bixilon.minosoft.gui.rendering.camera.frustum

import de.bixilon.kotlinglm.mat4x4.Mat4
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.observer.DataObserver
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.gui.rendering.camera.Camera
import de.bixilon.minosoft.gui.rendering.camera.WorldOffset
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.EMPTY
import de.bixilon.minosoft.test.ITUtil.allocate
import org.testng.Assert.assertFalse
import org.testng.Assert.assertTrue
import org.testng.annotations.Test

@Test(groups = ["frustum", "rendering"], enabled = false)
class FrustumTest {
    private val CAMERA = Frustum::class.java.getDeclaredField("camera").apply { isAccessible = true }
    private val RECALCULATE = Frustum::class.java.getDeclaredMethod("recalculate", Mat4::class.java).apply { isAccessible = true }

    private fun create(matrix: Mat4, offset: Vec3i = Vec3i.EMPTY): Frustum {
        val worldOffset = WorldOffset::class.java.allocate()
        worldOffset::offset.forceSet(DataObserver(offset))

        val camera = Camera::class.java.allocate()
        camera::offset.forceSet(worldOffset)

        val frustum = Frustum::class.java.allocate()
        CAMERA[frustum] = camera
        RECALCULATE.invoke(frustum, matrix)

        return frustum
    }

    fun test() {
        create(Mat4())
    }

    fun `aabb without world offset`() {
        val frustum = create(Mat4(floatArrayOf(-0.86422914f, -0.09317832f, -0.24142957f, -0.24142484f, 0.0f, 1.3786901f, -0.26089254f, -0.26088744f, -0.2232244f, 0.36074647f, 0.9347118f, 0.9346935f, -240.01099f, -351.1156f, -635.5928f, -635.5603f)))

        assertTrue(AABB(-430.56384174919845, 70.0, 590.7873477416574, -430.31384174919845, 70.25, 591.0373477416574) in frustum)
        assertTrue(AABB(-427.9736901136882, 70.0, 591.3889686121771, -427.7236901136882, 70.25, 591.6389686121771) in frustum)
        assertTrue(AABB(-429.7579415072009, 70.0, 589.9114582687175, -429.5079415072009, 70.25, 590.1614582687175) in frustum)
        assertTrue(AABB(-431.5273749202669, 82.0, 620.1584206819141, -431.2773749202669, 82.25, 620.4084206819141) in frustum)
        assertTrue(AABB(-446.4892105819352, 71.0, 597.5531901263694, -446.2392105819352, 71.25, 597.8031901263694) in frustum)

        assertFalse(AABB(-465.25, 90.0, 621.2639389106629, -465.0, 90.25, 621.5139389106629) in frustum)
        assertFalse(AABB(-436.766502697732, 71.0, 569.5983995094291, -436.516502697732, 71.25, 569.8483995094291) in frustum)
        assertFalse(AABB(-414.4408577375436, 71.0, 574.9274371774848, -414.1908577375436, 71.25, 575.1774371774848) in frustum)
    }

    fun `aabb without world offset 2`() {
        val frustum = create(Mat4(floatArrayOf(0.031299774f, 1.4272678f, 0.0017395335f, 0.0017394995f, 0.0f, 0.0024857917f, -1.0000181f, -0.99999857f, 0.89204353f, -0.050079573f, -6.103626E-5f, -6.103507E-5f, -533.13086f, 625.4982f, 80.68989f, 80.708305f)))

        assertTrue(AABB(-412.8941188408839, 71.0, 602.2574852969693, -412.6441188408839, 71.25, 602.5074852969693) in frustum)
        assertTrue(AABB(-410.98738348932096, 71.0, 607.7848105520435, -410.73738348932096, 71.25, 608.0348105520435) in frustum)
        assertTrue(AABB(-417.95394770358087, 72.0, 620.8937501690496, -417.70394770358087, 72.25, 621.1437501690496) in frustum)
        assertTrue(AABB(-423.388668602769, 71.0, 608.7942468147539, -423.138668602769, 71.25, 609.0442468147539) in frustum)

        assertFalse(AABB(-412.53503708991184, 71.0, 601.6134733480285, -412.28503708991184, 71.25, 601.8634733480285) in frustum)
        assertFalse(AABB(-410.4017313378348, 71.0, 608.0433986608168, -410.1517313378348, 71.25, 608.2933986608168) in frustum)
        assertFalse(AABB(-418.0516536152496, 72.0, 621.3850756796448, -417.8016536152496, 72.25, 621.6350756796448) in frustum)
        assertFalse(AABB(-423.7324126895721, 71.0, 608.7248577357954, -423.4824126895721, 71.25, 608.9748577357954) in frustum)
    }

    fun `aabb with world offset`() {
        val frustum = create(Mat4(floatArrayOf(-0.50417376f, 1.1785046f, 0.0014267226f, 0.0014266947f, 0.0f, 0.0024691392f, -1.0000181f, -0.99999857f, 0.7365663f, 0.80667686f, 9.765802E-4f, 9.765611E-4f, 482.31094f, 369.0533f, 91.83938f, 91.85758f)), Vec3i(12288, 0, -1837056))

        assertTrue(AABB(12381.468664298143, 75.0, -1837647.3519469386, 12381.718664298143, 75.25, -1837647.1019469386) in frustum)
        assertTrue(AABB(12392.072452953968, 77.0, -1837649.209980461, 12392.322452953968, 77.25, -1837648.959980461) in frustum)
        assertTrue(AABB(12378.786446140077, 90.0, -1837647.5059374704, 12379.036446140077, 90.25, -1837647.2559374704) in frustum)
        assertTrue(AABB(12373.378926686006, 75.0, -1837657.9311021646, 12373.628926686006, 75.25, -1837657.6811021646) in frustum)
        assertTrue(AABB(12362.253648637714, 73.0, -1837642.526695204, 12362.503648637714, 73.25, -1837642.276695204) in frustum)
        assertTrue(AABB(12377.927700770964, 72.0, -1837625.270155985, 12378.177700770964, 72.25, -1837625.020155985) in frustum)

        assertFalse(AABB(12375.81703836898, 71.0, -1837622.0629902035, 12376.06703836898, 71.25, -1837621.8129902035) in frustum)
        assertFalse(AABB(12386.114232198135, 72.0, -1837629.4777019175, 12386.364232198135, 72.25, -1837629.2277019175) in frustum)
        assertFalse(AABB(12394.104957870117, 78.0, -1837650.8539772641, 12394.354957870117, 78.25, -1837650.6039772641) in frustum)
        assertFalse(AABB(12380.219527944339, 80.0, -1837665.479991612, 12380.469527944339, 80.25, -1837665.229991612) in frustum)
        assertFalse(AABB(12377.953316849576, 90.0, -1837647.0120724367, 12378.203316849576, 90.25, -1837646.7620724367) in frustum)
        assertFalse(AABB(12379.90899152872, 98.0, -1837647.6819665642, 12380.15899152872, 98.25, -1837647.4319665642) in frustum)
        assertFalse(AABB(12379.90899152872, 980.0, -1837647.6819665642, 12380.15899152872, 980.25, -1837647.4319665642) in frustum)
    }

    // TODO: test (aabb, chunk) with camera offset
}
