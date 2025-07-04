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

package de.bixilon.minosoft.input.interaction.long

import glm_.vec3.Vec3d
import de.bixilon.kutil.observer.DataObserver
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.camera.target.targets.BlockTarget
import de.bixilon.minosoft.data.container.equipment.EquipmentSlots
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.entities.entities.player.Hands
import de.bixilon.minosoft.data.registries.item.items.weapon.defend.ShieldItem
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import de.bixilon.minosoft.input.interaction.InteractionTestUtil
import de.bixilon.minosoft.input.interaction.InteractionTestUtil.assertUseItem
import de.bixilon.minosoft.input.interaction.InteractionTestUtil.tick
import de.bixilon.minosoft.input.interaction.InteractionTestUtil.unsafePress
import de.bixilon.minosoft.physics.ItemUsing
import de.bixilon.minosoft.protocol.network.session.play.PacketTestUtil.assertNoPacket
import de.bixilon.minosoft.protocol.network.session.play.PacketTestUtil.assertPacket
import de.bixilon.minosoft.protocol.packets.c2s.play.block.BlockInteractC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.move.PositionRotationC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.player.SwingArmC2SP
import de.bixilon.minosoft.test.IT
import de.bixilon.minosoft.test.ITUtil.todo
import org.testng.Assert.assertEquals
import org.testng.Assert.assertNull
import org.testng.annotations.Test

@Test(groups = ["interaction"], dependsOnGroups = ["item", "block"])
class LongUseBlockIT {


    fun shieldOnDirt() {
        val session = InteractionTestUtil.createSession()
        session.player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(ShieldItem())
        session.camera.target::target.forceSet(DataObserver(BlockTarget(Vec3d.EMPTY, 1.0, Directions.DOWN, IT.BLOCK_2, null, BlockPosition.EMPTY)))
        val use = session.camera.interactions.use


        use.unsafePress()
        use.tick(10)

        assertEquals(session.player.using, ItemUsing(Hands.MAIN, 10))


        session.assertPacket(BlockInteractC2SP::class.java).let { assertEquals(it.hand, Hands.MAIN) }
        session.assertPacket(PositionRotationC2SP::class.java)
        session.assertUseItem(Hands.MAIN)
        session.assertNoPacket()
    }

    fun offHandShieldOnDirt() {
        val session = InteractionTestUtil.createSession()
        session.player.items.inventory[EquipmentSlots.OFF_HAND] = ItemStack(ShieldItem())
        session.camera.target::target.forceSet(DataObserver(BlockTarget(Vec3d.EMPTY, 1.0, Directions.DOWN, IT.BLOCK_2, null, BlockPosition.EMPTY)))
        val use = session.camera.interactions.use


        use.unsafePress()
        use.tick(10)

        assertEquals(session.player.using, ItemUsing(Hands.OFF, 10))


        session.assertPacket(BlockInteractC2SP::class.java).let { assertEquals(it.hand, Hands.MAIN) }
        session.assertPacket(BlockInteractC2SP::class.java).let { assertEquals(it.hand, Hands.OFF) }
        session.assertPacket(PositionRotationC2SP::class.java)
        session.assertUseItem(Hands.OFF)
        session.assertNoPacket()
    }

    fun shieldOnRepeater() {
        todo()
        val session = InteractionTestUtil.createSession()
        session.player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(ShieldItem())
        session.camera.target::target.forceSet(DataObserver(BlockTarget(Vec3d.EMPTY, 1.0, Directions.DOWN, IT.BLOCK_1, null, BlockPosition.EMPTY)))
        val use = session.camera.interactions.use


        use.unsafePress()
        use.tick(10)


        assertNull(session.player.using)


        // every 5 ticks
        session.assertPacket(BlockInteractC2SP::class.java)
        session.assertPacket(SwingArmC2SP(Hands.MAIN))
        session.assertPacket(BlockInteractC2SP::class.java)
        session.assertPacket(SwingArmC2SP(Hands.MAIN))
        session.assertNoPacket()
    }
}
