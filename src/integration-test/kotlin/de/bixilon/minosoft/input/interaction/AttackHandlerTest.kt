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
import de.bixilon.minosoft.protocol.network.session.play.PacketTestUtil.assertNoPacket
import de.bixilon.minosoft.protocol.network.session.play.PacketTestUtil.assertPacket
import de.bixilon.minosoft.protocol.network.session.play.SessionTestUtil.createSession
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.interact.EntityAttackC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.player.SwingArmC2SP
import org.testng.annotations.Test

@Test(groups = ["interaction"])
class AttackHandlerTest {
    // TODO: arm swing

    fun noTarget() {
        val session = createSession(1)
        val player = createPlayer(session)

        session.camera.interactions.tryAttack(true)

        session.assertPacket(SwingArmC2SP(Hands.MAIN))
        session.assertNoPacket()
        session.camera.interactions.tryAttack(false)
        session.assertNoPacket()
    }

    fun pig() {
        val session = createSession(1)
        val player = createPlayer(session)
        val attack = AttackHandler(session.camera.interactions)
        session.lookAtPig(1.0)

        attack.tryAttack()

        session.assertPacket(EntityAttackC2SP(10, false))
        session.assertPacket(SwingArmC2SP(Hands.MAIN))
        session.assertNoPacket()
    }

    fun maxReachDistance() {
        val session = createSession(1)
        val player = createPlayer(session)
        val attack = AttackHandler(session.camera.interactions)
        session.lookAtPig(2.9999999)

        attack.tryAttack()

        session.assertPacket(EntityAttackC2SP(10, false))
        session.assertPacket(SwingArmC2SP(Hands.MAIN))
        session.assertNoPacket()
    }

    fun outOfReachDistance() {
        val session = createSession(1)
        val player = createPlayer(session)
        val attack = AttackHandler(session.camera.interactions)
        session.lookAtPig(3.0)

        attack.tryAttack()
        session.assertPacket(SwingArmC2SP(Hands.MAIN))
        session.assertNoPacket()
    }

    fun attackMultipleTimes() {
        val session = createSession(1)
        val player = createPlayer(session)
        val attack = AttackHandler(session.camera.interactions)
        session.lookAtPig()

        attack.tryAttack()
        session.assertPacket(EntityAttackC2SP(10, false))
        session.assertPacket(SwingArmC2SP(Hands.MAIN))
        attack.tryAttack()
        session.assertPacket(EntityAttackC2SP(10, false))
        session.assertPacket(SwingArmC2SP(Hands.MAIN))

        session.assertNoPacket()
    }

    fun attackMiss() {
        val session = createSession(1)
        val player = createPlayer(session)

        session.camera.interactions.tryAttack(true)
        session.assertPacket(SwingArmC2SP(Hands.MAIN))
        session.assertNoPacket()
        session.camera.interactions.tryAttack(false)
        session.assertNoPacket()
        session.camera.interactions.tryAttack(true)
        session.assertPacket(SwingArmC2SP(Hands.MAIN))
        session.assertNoPacket() // on cooldown
        session.camera.interactions.tryAttack(false)
    }

    fun attackMiss2() {
        val session = createSession(1)
        val player = createPlayer(session)


        session.camera.interactions.tryAttack(true)
        session.assertPacket(SwingArmC2SP(Hands.MAIN))
        session.assertNoPacket()
        session.camera.interactions.tryAttack(false)
        session.assertNoPacket()

        session.lookAtPig()
        session.camera.interactions.attack.tick(5)
        session.camera.interactions.attack.tryAttack()

        session.assertPacket(EntityAttackC2SP(10, false))  // TODO
        session.assertPacket(SwingArmC2SP(Hands.MAIN))
        session.assertNoPacket()
    }

    fun attackItem() {
        val session = createSession(1)
        val player = createPlayer(session)
        val attack = AttackHandler(session.camera.interactions)

        session.lookAt(session.registries.entityType[ItemEntity.identifier]!!)

        attack.tryAttack()
        session.assertPacket(SwingArmC2SP(Hands.MAIN))
        session.assertNoPacket()
    }

    fun attackTnt() {
        val session = createSession(1)
        val player = createPlayer(session)
        val attack = AttackHandler(session.camera.interactions)

        session.lookAt(session.registries.entityType[PrimedTNT.identifier]!!)

        attack.tryAttack()
        session.assertPacket(EntityAttackC2SP(10, false))  // TODO
        session.assertPacket(SwingArmC2SP(Hands.MAIN))
        session.assertNoPacket() // TODO
    }

    fun attackArmorStand() {
        val session = createSession(1)
        val player = createPlayer(session)
        val attack = AttackHandler(session.camera.interactions)

        session.lookAt(session.registries.entityType[ArmorStand.identifier]!!)

        attack.tryAttack()
        session.assertPacket(EntityAttackC2SP(10, false))
        session.assertPacket(SwingArmC2SP(Hands.MAIN))
        session.assertNoPacket()
    }

    fun attackArmorStandMarker() {
        val session = createSession(1)
        val player = createPlayer(session)

        val armorStand = session.lookAt(session.registries.entityType[ArmorStand.identifier]!!).unsafeCast<ArmorStand>()
        armorStand.data[ArmorStand.FLAGS_DATA] = 0x10
        if (!armorStand.canRaycast) {
            session.camera.target::target.forceSet(DataObserver(null))
        }

        session.camera.interactions.tryAttack(true)
        session.assertPacket(SwingArmC2SP(Hands.MAIN))
        session.assertNoPacket()
        session.camera.interactions.tryAttack(false)
    }

    fun attackPlayer() {
        val session = createSession(1)
        val player = createPlayer(session)
        val attack = AttackHandler(session.camera.interactions)

        session.lookAtPlayer()

        attack.tryAttack()
        session.assertPacket(EntityAttackC2SP(10, false))
        session.assertPacket(SwingArmC2SP(Hands.MAIN))

        session.assertNoPacket()
    }

    fun attackInvisiblePlayer() {
        val session = createSession(1)
        val player = createPlayer(session)
        val attack = AttackHandler(session.camera.interactions)

        val remote = session.lookAtPlayer()
        remote.data[Entity.Companion.FLAGS_DATA] = 0x20

        attack.tryAttack()
        session.assertPacket(EntityAttackC2SP(10, false))
        session.assertPacket(SwingArmC2SP(Hands.MAIN))

        session.assertNoPacket()
    }
}
