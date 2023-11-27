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

package de.bixilon.minosoft.input.interaction

import de.bixilon.minosoft.data.entities.entities.player.Hands
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.createPlayer
import de.bixilon.minosoft.data.registries.blocks.types.building.stone.Stone
import de.bixilon.minosoft.input.interaction.InteractionTestUtil.lookAtPig
import de.bixilon.minosoft.input.interaction.breaking.BreakHandlerTest.Companion.createTarget
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil.createConnection
import de.bixilon.minosoft.protocol.network.connection.play.PacketTestUtil.assertNoPacket
import de.bixilon.minosoft.protocol.network.connection.play.PacketTestUtil.assertPacket
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.interact.EntityAttackC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.player.PlayerActionC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.player.SwingArmC2SP
import org.testng.annotations.Test

@Test(groups = ["interaction"])
class InteractionManagerTest {

    fun attackNoTarget() {
        val connection = createConnection(1)
        val player = createPlayer(connection)

        connection.camera.interactions.tryAttack(true)
        connection.assertPacket(SwingArmC2SP(Hands.MAIN))
        connection.assertNoPacket()
    }

    fun attackEntity() {
        val connection = createConnection(1)
        val player = createPlayer(connection)

        connection.lookAtPig()

        connection.camera.interactions.tryAttack(true)
        connection.assertPacket(EntityAttackC2SP::class.java)
        connection.assertPacket(SwingArmC2SP(Hands.MAIN))
        connection.assertNoPacket()
    }

    fun digBlock() {
        val connection = createConnection(1)
        val player = createPlayer(connection)

        createTarget(connection, Stone.Block.identifier, 1.0)

        connection.camera.interactions.tryAttack(true)
        connection.assertPacket(PlayerActionC2SP::class.java)
        connection.assertPacket(SwingArmC2SP(Hands.MAIN))
        connection.assertNoPacket()
    }
}
