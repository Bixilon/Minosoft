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

package de.bixilon.minosoft.input.interaction.breaking

import de.bixilon.minosoft.data.physics.PhysicsTestUtil.createPlayer
import de.bixilon.minosoft.data.registries.blocks.types.building.stone.StoneBlock
import de.bixilon.minosoft.input.interaction.InteractionTestUtil.tick
import de.bixilon.minosoft.input.interaction.InteractionTestUtil.unsafePress
import de.bixilon.minosoft.input.interaction.InteractionTestUtil.unsafeRelease
import de.bixilon.minosoft.protocol.network.session.play.PacketTestUtil.assertNoPacket
import de.bixilon.minosoft.protocol.network.session.play.PacketTestUtil.assertPacket
import de.bixilon.minosoft.protocol.network.session.play.SessionTestUtil.createSession
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.player.PlayerActionC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.player.SwingArmC2SP
import org.testng.annotations.Test

@Test(groups = ["input"])
class BreakHandlerInputTest {

    fun forceStart() {
        val session = createSession(1)
        createPlayer(session)
        val handler = BreakHandler(session.camera.interactions)
        BreakHandlerTest.createTarget(session, StoneBlock.Block.identifier, 1.0)


        handler.unsafePress()
        session.assertPacket(PlayerActionC2SP::class.java)
        handler.unsafeRelease()
    }

    fun forceStop() {
        val session = createSession(1)
        createPlayer(session)
        val handler = BreakHandler(session.camera.interactions)
        BreakHandlerTest.createTarget(session, StoneBlock.Block.identifier, 1.0)

        Thread.currentThread().priority = Thread.MAX_PRIORITY


        handler.unsafePress()
        session.assertPacket(PlayerActionC2SP::class.java)
        session.assertPacket(SwingArmC2SP::class.java)

        handler.unsafeRelease()
        session.assertPacket(PlayerActionC2SP::class.java)
        session.assertNoPacket()
    }

    fun continueBreak() {
        val session = createSession(1)
        createPlayer(session)
        val handler = BreakHandler(session.camera.interactions)
        BreakHandlerTest.createTarget(session, StoneBlock.Block.identifier, 1.0)


        Thread.currentThread().priority = Thread.MAX_PRIORITY

        handler.unsafePress()
        session.assertPacket(PlayerActionC2SP::class.java)
        session.assertPacket(SwingArmC2SP::class.java)
        session.assertNoPacket()

        handler.tick(1)
        session.assertPacket(SwingArmC2SP::class.java)
        session.assertNoPacket()

        handler.tick(1)
        session.assertPacket(SwingArmC2SP::class.java)
        session.assertNoPacket()

        handler.unsafeRelease()
    }
}
