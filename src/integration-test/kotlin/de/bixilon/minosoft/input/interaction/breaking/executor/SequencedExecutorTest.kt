/*
 * Minosoft
 * Copyright (C) 2020-2024 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.input.interaction.breaking.executor

import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.registries.blocks.types.building.stone.StoneBlock
import de.bixilon.minosoft.input.interaction.breaking.BreakHandler
import de.bixilon.minosoft.protocol.network.session.play.SessionTestUtil.createSession
import org.testng.Assert.assertEquals
import org.testng.Assert.assertNull
import org.testng.annotations.Test

@Test(groups = ["interaction"])
class SequencedExecutorTest {

    fun testSequenceId() {
        val session = createSession()
        val executor = SequencedExecutor(BreakHandler(session.camera.interactions))
        val state = session.registries.block[StoneBlock.Block]!!.states.default
        assertEquals(1, executor.start(Vec3i(1, 1, 1), state))
        assertEquals(2, executor.finish())
        assertEquals(3, executor.start(Vec3i(1, 1, 2), state))
        assertEquals(4, executor.finish())
    }

    fun testRevert() {
        val session = createSession(1)
        val executor = SequencedExecutor(BreakHandler(session.camera.interactions))
        val state = session.registries.block[StoneBlock.Block]!!.states.default

        session.world[Vec3i(1, 1, 1)] = state

        executor.start(Vec3i(1, 1, 1), state)


        executor.abort(Vec3i(1, 1, 1), state) // TODO: simulate packet
        // session.world[Vec3i(1, 1, 1)] = state // <- set the same block -> revert/cancel

        executor.finish()
        Thread.sleep(10) // async, wait for thread to complete
        assertEquals(session.world[Vec3i(1, 1, 1)], state)
    }

    fun testAcknowledge() {
        val session = createSession()
        val executor = SequencedExecutor(BreakHandler(session.camera.interactions))
        val state = session.registries.block[StoneBlock.Block]!!.states.default

        executor.start(Vec3i(1, 1, 1), state)

        executor.finish()
        Thread.sleep(10) // async, wait for thread to complete
        assertNull(session.world[Vec3i(1, 1, 1)])
    }
}
