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

package de.bixilon.minosoft.input.interaction.breaking

import de.bixilon.minosoft.data.physics.PhysicsTestUtil.createPlayer
import de.bixilon.minosoft.data.registries.blocks.types.stone.RockBlock
import de.bixilon.minosoft.input.interaction.KeyHandlerUtil.awaitTicks
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil.createConnection
import de.bixilon.minosoft.protocol.network.connection.play.PacketTestUtil.assertNoPacket
import de.bixilon.minosoft.protocol.network.connection.play.PacketTestUtil.assertPacket
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.player.PlayerActionC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.player.SwingArmC2SP
import org.testng.SkipException
import org.testng.annotations.Test
import kotlin.system.measureTimeMillis

@Test(groups = ["input"])
class BreakHandlerInputTest {

    fun forceStart() {
        val connection = createConnection(1)
        val player = createPlayer(connection)
        val handler = BreakHandler(connection.camera.interactions)
        BreakHandlerTest.createTarget(connection, RockBlock.Stone.identifier, 1.0)


        handler.press()
        connection.assertPacket(PlayerActionC2SP::class.java)
        handler.release()
    }

    fun forceStop() {
        val connection = createConnection(1)
        val player = createPlayer(connection)
        val handler = BreakHandler(connection.camera.interactions)
        BreakHandlerTest.createTarget(connection, RockBlock.Stone.identifier, 1.0)

        Thread.currentThread().priority = Thread.MAX_PRIORITY


        handler.press() // key down
        connection.assertPacket(PlayerActionC2SP::class.java)
        connection.assertPacket(SwingArmC2SP::class.java)
        handler.release() // key not down anymore
        connection.assertPacket(PlayerActionC2SP::class.java)
        Thread.sleep(100)
        connection.assertNoPacket()
    }

    fun continueBreak() {
        val connection = createConnection(1)
        val player = createPlayer(connection)
        val handler = BreakHandler(connection.camera.interactions)
        BreakHandlerTest.createTarget(connection, RockBlock.Stone.identifier, 1.0)


        Thread.currentThread().priority = Thread.MAX_PRIORITY

        handler.press() // key down
        connection.assertPacket(PlayerActionC2SP::class.java)
        connection.assertPacket(SwingArmC2SP::class.java)
        connection.assertNoPacket()

        if (measureTimeMillis { Thread.sleep(20) } > 30) throw SkipException("system busy")
        connection.assertNoPacket()
        handler.awaitTicks(1)
        connection.assertPacket(SwingArmC2SP::class.java)
        connection.assertNoPacket()
        handler.awaitTicks(1)
        connection.assertPacket(SwingArmC2SP::class.java)
        connection.assertNoPacket()
        handler.release()
    }
}
