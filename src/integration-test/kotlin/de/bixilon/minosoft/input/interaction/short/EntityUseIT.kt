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

package de.bixilon.minosoft.input.interaction.short

import de.bixilon.kmath.vec.vec3.d.Vec3d
import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.kutil.observer.DataObserver
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.camera.target.targets.EntityTarget
import de.bixilon.minosoft.data.container.TestItem3
import de.bixilon.minosoft.data.container.equipment.EquipmentSlots
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.entities.animal.Pig
import de.bixilon.minosoft.data.entities.entities.player.Hands
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.input.interaction.InteractionTestUtil
import de.bixilon.minosoft.input.interaction.InteractionTestUtil.assertUseItem
import de.bixilon.minosoft.input.interaction.InteractionTestUtil.unsafePress
import de.bixilon.minosoft.protocol.network.session.play.PacketTestUtil.assertOnlyPacket
import de.bixilon.minosoft.protocol.network.session.play.PacketTestUtil.assertPacket
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.interact.EntityEmptyInteractC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.interact.EntityInteractPositionC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.move.PositionRotationC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.item.UseItemC2SP
import org.testng.annotations.Test

@Test(groups = ["interaction"], dependsOnGroups = ["item", "block"])
class EntityUseIT {
    private val pig = EntityType(Pig.identifier, minecraft(""), 1.0f, 1.0f, mapOf(), Pig, null)


    fun testAirOnPig() {
        val session = InteractionTestUtil.createSession()
        val entity = Pig(session, this.pig, EntityData(session), Vec3d.EMPTY, EntityRotation.EMPTY)
        session.world.entities.add(10, null, entity)
        session.camera.target::target.forceSet(DataObserver(EntityTarget(Vec3d.EMPTY, 1.0, Directions.DOWN, entity)))
        val use = session.camera.interactions.use

        use.unsafePress()

        session.assertPacket(EntityInteractPositionC2SP(session, entity, Vec3f.EMPTY, Hands.MAIN, false))
        session.assertPacket(EntityEmptyInteractC2SP(session, entity, Hands.MAIN, false))
        session.assertPacket(EntityInteractPositionC2SP(session, entity, Vec3f.EMPTY, Hands.OFF, false))
        session.assertOnlyPacket(EntityEmptyInteractC2SP(session, entity, Hands.OFF, false))
    }

    fun testCoalOnPig() {
        val session = InteractionTestUtil.createSession()
        val entity = Pig(session, this.pig, EntityData(session), Vec3d.EMPTY, EntityRotation.EMPTY)
        session.world.entities.add(10, null, entity)
        session.player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(TestItem3)
        session.camera.target::target.forceSet(DataObserver(EntityTarget(Vec3d.EMPTY, 1.0, Directions.DOWN, entity)))
        val use = session.camera.interactions.use

        use.unsafePress()

        session.assertPacket(EntityInteractPositionC2SP(session, entity, Vec3f.EMPTY, Hands.MAIN, false))
        session.assertPacket(EntityEmptyInteractC2SP(session, entity, Hands.MAIN, false))
        session.assertPacket(PositionRotationC2SP::class.java)
        session.assertUseItem(Hands.MAIN)
        session.assertPacket(EntityInteractPositionC2SP(session, entity, Vec3f.EMPTY, Hands.OFF, false))
        session.assertOnlyPacket(EntityEmptyInteractC2SP(session, entity, Hands.OFF, false))
    }

    fun testCoalOnPig2() {
        val session = InteractionTestUtil.createSession()
        val entity = Pig(session, this.pig, EntityData(session), Vec3d.EMPTY, EntityRotation.EMPTY)
        session.world.entities.add(10, null, entity)
        session.player.items.inventory[EquipmentSlots.OFF_HAND] = ItemStack(TestItem3)
        session.camera.target::target.forceSet(DataObserver(EntityTarget(Vec3d.EMPTY, 1.0, Directions.DOWN, entity)))
        val use = session.camera.interactions.use

        use.unsafePress()

        session.assertPacket(EntityInteractPositionC2SP(session, entity, Vec3f.EMPTY, Hands.MAIN, false))
        session.assertPacket(EntityEmptyInteractC2SP(session, entity, Hands.MAIN, false))
        session.assertPacket(EntityInteractPositionC2SP(session, entity, Vec3f.EMPTY, Hands.OFF, false))
        session.assertPacket(EntityEmptyInteractC2SP(session, entity, Hands.OFF, false))
        session.assertPacket(PositionRotationC2SP::class.java)
        session.assertOnlyPacket(UseItemC2SP(Hands.OFF))
    }

    fun testCoalOnPig3() {
        val session = InteractionTestUtil.createSession()
        val entity = Pig(session, this.pig, EntityData(session), Vec3d.EMPTY, EntityRotation.EMPTY)
        session.world.entities.add(10, null, entity)
        session.player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(TestItem3)
        session.player.items.inventory[EquipmentSlots.OFF_HAND] = ItemStack(TestItem3)
        session.camera.target::target.forceSet(DataObserver(EntityTarget(Vec3d.EMPTY, 1.0, Directions.DOWN, entity)))
        val use = session.camera.interactions.use

        use.unsafePress()

        session.assertPacket(EntityInteractPositionC2SP(session, entity, Vec3f.EMPTY, Hands.MAIN, false))
        session.assertPacket(EntityEmptyInteractC2SP(session, entity, Hands.MAIN, false))
        session.assertPacket(PositionRotationC2SP::class.java)
        session.assertUseItem(Hands.MAIN)
        session.assertPacket(EntityInteractPositionC2SP(session, entity, Vec3f.EMPTY, Hands.OFF, false))
        session.assertPacket(EntityEmptyInteractC2SP(session, entity, Hands.OFF, false))
        session.assertPacket(PositionRotationC2SP::class.java)
        session.assertOnlyPacket(UseItemC2SP(Hands.OFF, 2))
    }

    // TODO: egg on pig
}
