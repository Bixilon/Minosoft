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

package de.bixilon.minosoft.input.interaction

import de.bixilon.minosoft.data.entities.entities.player.Hands
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.createPlayer
import de.bixilon.minosoft.data.registries.blocks.types.building.stone.StoneBlock
import de.bixilon.minosoft.input.interaction.InteractionTestUtil.lookAtPig
import de.bixilon.minosoft.input.interaction.breaking.BreakHandlerTest.Companion.createTarget
import de.bixilon.minosoft.protocol.network.session.play.PacketTestUtil.assertNoPacket
import de.bixilon.minosoft.protocol.network.session.play.PacketTestUtil.assertPacket
import de.bixilon.minosoft.protocol.network.session.play.SessionTestUtil.createSession
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.interact.EntityAttackC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.player.PlayerActionC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.player.SwingArmC2SP
import org.testng.annotations.Test

@Test(groups = ["interaction"])
class InteractionManagerTest {

    fun attackNoTarget() {
        val session = createSession(1)
        val player = createPlayer(session)

        session.camera.interactions.tryAttack(true)
        session.assertPacket(SwingArmC2SP(Hands.MAIN))
        session.assertNoPacket()
    }

    fun attackEntity() {
        val session = createSession(1)
        val player = createPlayer(session)

        session.lookAtPig()

        session.camera.interactions.tryAttack(true)
        session.assertPacket(EntityAttackC2SP::class.java)
        session.assertPacket(SwingArmC2SP(Hands.MAIN))
        session.assertNoPacket()
    }

    fun digBlock() {
        val session = createSession(1)
        val player = createPlayer(session)

        createTarget(session, StoneBlock.Block.identifier, 1.0)

        session.camera.interactions.tryAttack(true)
        session.assertPacket(PlayerActionC2SP::class.java)
        session.assertPacket(SwingArmC2SP(Hands.MAIN))
        session.assertNoPacket()
    }
}
