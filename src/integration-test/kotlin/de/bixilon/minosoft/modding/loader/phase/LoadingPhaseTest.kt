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

package de.bixilon.minosoft.modding.loader.phase

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.latch.SimpleLatch
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.kutil.reflection.ReflectionUtil.getFieldOrNull
import de.bixilon.minosoft.modding.loader.ModLoader
import de.bixilon.minosoft.terminal.RunConfiguration
import org.testng.Assert.assertEquals
import org.testng.annotations.Test
import java.io.File


@Test(groups = ["mods"])
class LoadingPhaseTest {

    private fun create(path: String): LoadingPhase {
        ModLoader.mods.clear()
        val phase = LoadingPhase(path)
        phase::class.java.getFieldOrNull("path")!!.forceSet(phase, File("./src/integration-test/resources/mods/$path"))

        return phase
    }

    private val LoadingPhase.latch: SimpleLatch get() = this::class.java.getFieldOrNull("latch")!!.get(this).unsafeCast()

    fun setup() {
        val phase = create("empty")
        assertEquals(phase.state, PhaseStates.WAITING)
        assertEquals(phase.latch.count, 1) // not started
    }

    fun `load but skip loading`() {
        val phase = create("empty")
        RunConfiguration.IGNORE_MODS = true
        phase.load()
        RunConfiguration.IGNORE_MODS = false
        phase.latch.await()
    }

    fun `load empty`() {
        val phase = create("empty")
        phase.load()
        assertEquals(phase.state, PhaseStates.COMPLETE)
        assertEquals(phase.latch.count, 0)
        phase.await()
        assertEquals(ModLoader.mods.mods.size, 0)
    }

    fun `broken mod`() {
        val phase = create("broken")
        phase.load()
        phase.await()
        assertEquals(ModLoader.mods.mods.size, 0)
    }

    fun `nocode mod`() {
        val phase = create("nocode")
        phase.load()
        phase.await()
        assertEquals(ModLoader.mods.mods.size, 0)
    }

    fun `dummy mod`() {
        val phase = create("dummy")
        phase.load()
        phase.await()
        assertEquals(ModLoader.mods.mods.size, 1)
        val mod = ModLoader.mods["dummy"]!!.main!!
        assertEquals(mod::class.java.getFieldOrNull("dummy")!!.get(mod), 1)
        assertEquals(mod::class.java.getFieldOrNull("init")!!.get(mod), 1)
        assertEquals(mod::class.java.getFieldOrNull("postInit")!!.get(mod), 1)
    }
}
