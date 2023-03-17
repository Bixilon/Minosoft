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

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.observer.DataObserver
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.decoration.armorstand.ArmorStand
import de.bixilon.minosoft.data.entities.entities.item.ItemEntity
import de.bixilon.minosoft.data.entities.entities.item.PrimedTNT
import de.bixilon.minosoft.data.entities.entities.player.Hands
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.createPlayer
import de.bixilon.minosoft.input.interaction.InteractionTestUtil.lookAt
import de.bixilon.minosoft.input.interaction.InteractionTestUtil.lookAtPig
import de.bixilon.minosoft.input.interaction.InteractionTestUtil.lookAtPlayer
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil.createConnection
import de.bixilon.minosoft.protocol.network.connection.play.PacketTestUtil.assertNoPacket
import de.bixilon.minosoft.protocol.network.connection.play.PacketTestUtil.assertPacket
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.interact.EntityAttackC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.move.SwingArmC2SP
import org.testng.annotations.Test

@Test(groups = ["interaction"])
class AttackHandlerTest {
    // TODO: arm swing

    fun noTarget() {
        val connection = createConnection(1)
        val player = createPlayer(connection)

        connection.camera.interactions.tryAttack(true)

        connection.assertPacket(SwingArmC2SP(Hands.MAIN))
        connection.assertNoPacket()
        connection.camera.interactions.tryAttack(false)
        connection.assertNoPacket()
    }

    fun pig() {
        val connection = createConnection(1)
        val player = createPlayer(connection)
        val attack = AttackHandler(connection.camera.interactions)
        connection.lookAtPig(1.0)

        attack.tryAttack()

        connection.assertPacket(EntityAttackC2SP(10, false))
        connection.assertPacket(SwingArmC2SP(Hands.MAIN))
        connection.assertNoPacket()
    }

    fun maxReachDistance() {
        val connection = createConnection(1)
        val player = createPlayer(connection)
        val attack = AttackHandler(connection.camera.interactions)
        connection.lookAtPig(2.9999999)

        attack.tryAttack()

        connection.assertPacket(EntityAttackC2SP(10, false))
        connection.assertPacket(SwingArmC2SP(Hands.MAIN))
        connection.assertNoPacket()
    }

    fun outOfReachDistance() {
        val connection = createConnection(1)
        val player = createPlayer(connection)
        val attack = AttackHandler(connection.camera.interactions)
        connection.lookAtPig(3.0)

        attack.tryAttack()
        connection.assertPacket(SwingArmC2SP(Hands.MAIN))
        connection.assertNoPacket()
    }

    fun attackMultipleTimes() {
        val connection = createConnection(1)
        val player = createPlayer(connection)
        val attack = AttackHandler(connection.camera.interactions)
        connection.lookAtPig()

        attack.tryAttack()
        connection.assertPacket(EntityAttackC2SP(10, false))
        connection.assertPacket(SwingArmC2SP(Hands.MAIN))
        attack.tryAttack()
        connection.assertPacket(EntityAttackC2SP(10, false))
        connection.assertPacket(SwingArmC2SP(Hands.MAIN))

        connection.assertNoPacket()
    }

    fun attackMiss() {
        val connection = createConnection(1)
        val player = createPlayer(connection)

        connection.camera.interactions.tryAttack(true)
        connection.assertPacket(SwingArmC2SP(Hands.MAIN))
        connection.assertNoPacket()
        connection.camera.interactions.tryAttack(false)
        connection.assertNoPacket()
        connection.camera.interactions.tryAttack(true)
        connection.assertPacket(SwingArmC2SP(Hands.MAIN))
        connection.assertNoPacket() // on cooldown
        connection.camera.interactions.tryAttack(false)
    }

    fun attackMiss2() {
        val connection = createConnection(1)
        val player = createPlayer(connection)


        connection.camera.interactions.tryAttack(true)
        connection.assertPacket(SwingArmC2SP(Hands.MAIN))
        connection.assertNoPacket()
        connection.camera.interactions.tryAttack(false)
        connection.assertNoPacket()

        connection.lookAtPig()
        connection.camera.interactions.attack.tick(5)
        connection.camera.interactions.attack.tryAttack()

        connection.assertPacket(EntityAttackC2SP(10, false))  // TODO
        connection.assertPacket(SwingArmC2SP(Hands.MAIN))
        connection.assertNoPacket()
    }

    fun attackItem() {
        val connection = createConnection(1)
        val player = createPlayer(connection)
        val attack = AttackHandler(connection.camera.interactions)

        connection.lookAt(connection.registries.entityType[ItemEntity.identifier]!!)

        attack.tryAttack()
        connection.assertPacket(SwingArmC2SP(Hands.MAIN))
        connection.assertNoPacket()
    }

    fun attackTnt() {
        val connection = createConnection(1)
        val player = createPlayer(connection)
        val attack = AttackHandler(connection.camera.interactions)

        connection.lookAt(connection.registries.entityType[PrimedTNT.identifier]!!)

        attack.tryAttack()
        connection.assertPacket(EntityAttackC2SP(10, false))  // TODO
        connection.assertPacket(SwingArmC2SP(Hands.MAIN))
        connection.assertNoPacket() // TODO
    }

    fun attackArmorStand() {
        val connection = createConnection(1)
        val player = createPlayer(connection)
        val attack = AttackHandler(connection.camera.interactions)

        connection.lookAt(connection.registries.entityType[ArmorStand.identifier]!!)

        attack.tryAttack()
        connection.assertPacket(EntityAttackC2SP(10, false))
        connection.assertPacket(SwingArmC2SP(Hands.MAIN))
        connection.assertNoPacket()
    }

    fun attackArmorStandMarker() {
        val connection = createConnection(1)
        val player = createPlayer(connection)

        val armorStand = connection.lookAt(connection.registries.entityType[ArmorStand.identifier]!!).unsafeCast<ArmorStand>()
        armorStand.data[ArmorStand.FLAGS_DATA] = 0x10
        if (!armorStand.canRaycast) {
            connection.camera.target::target.forceSet(DataObserver(null))
        }

        connection.camera.interactions.tryAttack(true)
        connection.assertPacket(SwingArmC2SP(Hands.MAIN))
        connection.assertNoPacket()
        connection.camera.interactions.tryAttack(false)
    }

    fun attackPlayer() {
        val connection = createConnection(1)
        val player = createPlayer(connection)
        val attack = AttackHandler(connection.camera.interactions)

        connection.lookAtPlayer()

        attack.tryAttack()
        connection.assertPacket(EntityAttackC2SP(10, false))
        connection.assertPacket(SwingArmC2SP(Hands.MAIN))

        connection.assertNoPacket()
    }

    fun attackInvisiblePlayer() {
        val connection = createConnection(1)
        val player = createPlayer(connection)
        val attack = AttackHandler(connection.camera.interactions)

        val remote = connection.lookAtPlayer()
        remote.data[Entity.Companion.FLAGS_DATA] = 0x20

        attack.tryAttack()
        connection.assertPacket(EntityAttackC2SP(10, false))
        connection.assertPacket(SwingArmC2SP(Hands.MAIN))

        connection.assertNoPacket()
    }
}
