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

package de.bixilon.minosoft.data.physics.attribute

import de.bixilon.minosoft.data.physics.PhysicsTestUtil.applySlowness
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.applySpeed
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertEquals
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.createPlayer
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil.createConnection
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import org.testng.annotations.Test

@Test(groups = ["physics"])
class AttributeIT {
    private val connection: PlayConnection by lazy { createConnection(0) }

    fun testSpeed0() {
        val player = createPlayer(connection)
        assertEquals(player.physics().movementSpeed, 0.1f)
    }

    fun testSpeed1() {
        val player = createPlayer(connection)
        player.applySpeed(1)
        assertEquals(player.physics().movementSpeed, 0.14f)
    }

    fun testSpeed2() {
        val player = createPlayer(connection)
        player.applySpeed(2)
        assertEquals(player.physics().movementSpeed, 0.16f)
    }

    fun testSpeed3() {
        val player = createPlayer(connection)
        player.applySpeed(3)
        assertEquals(player.physics().movementSpeed, 0.18f)
    }

    fun testSpeed4() {
        val player = createPlayer(connection)
        player.applySpeed(4)
        assertEquals(player.physics().movementSpeed, 0.2f)
    }

    fun testSpeed123() {
        val player = createPlayer(connection)
        player.applySpeed(123)
        assertEquals(player.physics().movementSpeed, 2.5800002f)
    }

    fun testSpeed1024() {
        val player = createPlayer(connection)
        player.applySpeed(1024)
        assertEquals(player.physics().movementSpeed, 20.6f)
    }

    fun testSprinting() {
        val player = createPlayer(connection)
        player.isSprinting = true
        assertEquals(player.physics().movementSpeed, 0.13000001f)
    }

    fun testSprintingSpeed1() {
        val player = createPlayer(connection)
        player.isSprinting = true
        player.applySpeed(1)
        assertEquals(player.physics().movementSpeed, 0.18200001f)
    }

    fun testSprintingSpeed5() {
        val player = createPlayer(connection)
        player.isSprinting = true
        player.applySpeed(5)
        assertEquals(player.physics().movementSpeed, 0.286f)
    }

    fun testSlowness1() {
        val player = createPlayer(connection)
        player.applySlowness(1)
        assertEquals(player.physics().movementSpeed, 0.07f)
    }

    fun testSlowness5() {
        val player = createPlayer(connection)
        player.applySlowness(5)
        assertEquals(player.physics().movementSpeed, 0.009999997f)
    }

    fun speed1Slowness1() {
        val player = createPlayer(connection)
        player.applySpeed(1)
        player.applySlowness(1)
        assertEquals(player.physics().movementSpeed, 0.098f)
    }

    fun speed3Slowness5() {
        val player = createPlayer(connection)
        player.applySpeed(3)
        player.applySlowness(5)
        assertEquals(player.physics().movementSpeed, 0.017999994f)
    }

    fun speed9Slowness6Sprint() {
        val player = createPlayer(connection)
        player.applySpeed(9)
        player.applySlowness(6)
        player.isSprinting = true
        assertEquals(player.physics().movementSpeed, 0.0f)
    }

    fun speed9Slowness1Sprint() {
        val player = createPlayer(connection)
        player.applySpeed(9)
        player.applySlowness(1)
        player.isSprinting = true
        assertEquals(player.physics().movementSpeed, 0.273f)
    }
}
