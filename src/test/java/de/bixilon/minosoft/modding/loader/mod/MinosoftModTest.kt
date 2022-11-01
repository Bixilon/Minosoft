/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.modding.loader.mod

import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.minosoft.modding.loader.LoadingPhases
import de.bixilon.minosoft.modding.loader.ModList
import de.bixilon.minosoft.modding.loader.mod.manifest.ModManifest
import de.bixilon.minosoft.modding.loader.mod.manifest.load.LoadM
import de.bixilon.minosoft.modding.loader.mod.manifest.packages.PackagesM
import de.bixilon.minosoft.modding.loader.mod.source.ArchiveSource
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

internal class MinosoftModTest {

    private fun createMod(): MinosoftMod {
        return MinosoftMod(ArchiveSource(File(".")), LoadingPhases.PRE_BOOT, CountUpAndDownLatch(0))
    }

    @Test
    fun testDependsOn1() {
        val dependency = createMod().apply { manifest = ModManifest("dependency", version = "0", main = "") }
        val b = createMod().apply { manifest = ModManifest("bbb", version = "0", main = "", packages = PackagesM(setOf("dependency"))) }
        val c = createMod().apply { manifest = ModManifest("ccc", version = "0", main = "", packages = PackagesM(setOf("dependency"))) }

        val list = ModList()
        list += dependency
        list += b
        list += c


        assertEquals(list.sorted(), mutableListOf(dependency, b, c))
    }

    @Test
    fun testDependsOn2() {
        val dependency = createMod().apply { manifest = ModManifest("dependency", version = "0", main = "") }
        val b = createMod().apply { manifest = ModManifest("bbb", version = "0", main = "", packages = PackagesM(setOf("dependency"))) }
        val c = createMod().apply { manifest = ModManifest("ccc", version = "0", main = "", packages = PackagesM(setOf("dependency", "ccc"))) }

        val list = ModList()
        list += dependency
        list += b
        list += c


        assertEquals(list.sorted(), mutableListOf(dependency, b, c))
    }

    @Test
    fun testDependsOn3() {
        val dependency = createMod().apply { manifest = ModManifest("dependency", version = "0", main = "") }
        val c = createMod().apply { manifest = ModManifest("ccc", version = "0", main = "", packages = PackagesM(setOf("dependency", "bbb"))) }
        val b = createMod().apply { manifest = ModManifest("bbb", version = "0", main = "", packages = PackagesM(setOf("dependency"))) }

        val list = ModList()
        list += dependency
        list += b
        list += c


        assertEquals(list.sorted(), mutableListOf(dependency, b, c))
    }

    @Test
    fun testDependsOn4() {
        val dependency = createMod().apply { manifest = ModManifest("dependency", version = "0", main = "") }
        val b = createMod().apply { manifest = ModManifest("bbb", version = "0", main = "", packages = PackagesM(setOf("dependency"))) }
        val c = createMod().apply { manifest = ModManifest("ccc", version = "0", main = "", packages = PackagesM(setOf("bbb"))) }

        val list = ModList()
        list += dependency
        list += b
        list += c


        assertEquals(list.sorted(), mutableListOf(dependency, b, c))
    }

    @Test
    fun testLoadingOrder1() {
        val dependency = createMod().apply { manifest = ModManifest("dependency", version = "0", main = "") }
        val c = createMod().apply { manifest = ModManifest("ccc", version = "0", main = "", load = LoadM(after = setOf("bbb"))) }
        val b = createMod().apply { manifest = ModManifest("bbb", version = "0", main = "", packages = PackagesM(setOf("dependency"))) }

        val list = ModList()
        list += dependency
        list += b
        list += c


        assertEquals(list.sorted(), mutableListOf(dependency, b, c))
    }

    @Test
    fun testLoadingOrder2() {
        val dependency = createMod().apply { manifest = ModManifest("dependency", version = "0", main = "") }
        val c = createMod().apply { manifest = ModManifest("ccc", version = "0", main = "", load = LoadM(before = setOf("bbb"))) }
        val b = createMod().apply { manifest = ModManifest("bbb", version = "0", main = "", packages = PackagesM(setOf("dependency"))) }

        val list = ModList()
        list += dependency
        list += b
        list += c


        assertEquals(list.sorted(), mutableListOf(dependency, c, b))
    }
}
