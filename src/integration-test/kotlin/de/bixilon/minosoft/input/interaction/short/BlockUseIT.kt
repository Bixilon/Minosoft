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

import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.kmath.vec.vec3.d.Vec3d
import de.bixilon.kutil.observer.DataObserver
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.camera.target.targets.BlockTarget
import de.bixilon.minosoft.data.container.equipment.EquipmentSlots
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.entities.entities.player.Hands
import de.bixilon.minosoft.data.registries.items.CoalTest0
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.input.interaction.InteractionTestUtil
import de.bixilon.minosoft.input.interaction.InteractionTestUtil.assertUseItem
import de.bixilon.minosoft.input.interaction.InteractionTestUtil.unsafePress
import de.bixilon.minosoft.protocol.network.session.play.PacketTestUtil.assertNoPacket
import de.bixilon.minosoft.protocol.network.session.play.PacketTestUtil.assertOnlyPacket
import de.bixilon.minosoft.protocol.network.session.play.PacketTestUtil.assertPacket
import de.bixilon.minosoft.protocol.packets.c2s.play.block.BlockInteractC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.move.PositionRotationC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.item.UseItemC2SP
import de.bixilon.minosoft.test.IT
import org.testng.annotations.Test

@Test(groups = ["interaction"], dependsOnGroups = ["item", "block"])
class BlockUseIT {

    fun testAirOnStone() {
        val session = InteractionTestUtil.createSession()
        session.camera.target::target.forceSet(DataObserver(BlockTarget(Vec3d.EMPTY, 1.0, Directions.DOWN, IT.BLOCK_1, null, BlockPosition.EMPTY)))
        val use = session.camera.interactions.use

        use.unsafePress()

        session.assertPacket(BlockInteractC2SP(BlockPosition.EMPTY, Directions.DOWN, Vec3d.EMPTY, null, Hands.MAIN, false))
        session.assertOnlyPacket(BlockInteractC2SP(BlockPosition.EMPTY, Directions.DOWN, Vec3d.EMPTY, null, Hands.OFF, false, 2))
    }

    fun testCoalOnStone() {
        val session = InteractionTestUtil.createSession()
        session.camera.target::target.forceSet(DataObserver(BlockTarget(Vec3d.EMPTY, 1.0, Directions.DOWN, IT.BLOCK_1, null, BlockPosition.EMPTY)))
        session.player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(CoalTest0.item)
        val use = session.camera.interactions.use

        use.unsafePress()

        session.assertPacket(BlockInteractC2SP(BlockPosition.EMPTY, Directions.DOWN, Vec3d.EMPTY, ItemStack(CoalTest0.item), Hands.MAIN, false))
        session.assertPacket(PositionRotationC2SP::class.java)
        session.assertUseItem(Hands.MAIN)
        session.assertOnlyPacket(BlockInteractC2SP(BlockPosition.EMPTY, Directions.DOWN, Vec3d.Vec3d, null, Hands.OFF, false, 3))
    }

    fun testCoalOnStone2() {
        val session = InteractionTestUtil.createSession()
        session.camera.target::target.forceSet(DataObserver(BlockTarget(Vec3d.EMPTY, 1.0, Directions.DOWN, IT.BLOCK_1, null, BlockPosition.EMPTY)))
        session.player.items.inventory[EquipmentSlots.OFF_HAND] = ItemStack(CoalTest0.item)
        val use = session.camera.interactions.use

        use.unsafePress()

        session.assertPacket(BlockInteractC2SP(BlockPosition.EMPTY, Directions.DOWN, Vec3d.EMPTY, null, Hands.MAIN, false))
        session.assertPacket(BlockInteractC2SP(BlockPosition.EMPTY, Directions.DOWN, Vec3d.EMPTY, ItemStack(CoalTest0.item), Hands.OFF, false, 2))
        session.assertPacket(PositionRotationC2SP::class.java)
        session.assertUseItem(Hands.OFF)
        session.assertNoPacket()
    }

    fun testCoalOnStone3() {
        val session = InteractionTestUtil.createSession()
        session.camera.target::target.forceSet(DataObserver(BlockTarget(Vec3d.EMPTY, 1.0, Directions.DOWN, IT.BLOCK_1, null, BlockPosition.EMPTY)))
        session.player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(CoalTest0.item)
        session.player.items.inventory[EquipmentSlots.OFF_HAND] = ItemStack(CoalTest0.item)
        val use = session.camera.interactions.use

        use.unsafePress()

        session.assertPacket(BlockInteractC2SP(BlockPosition.EMPTY, Directions.DOWN, Vec3d.EMPTY, ItemStack(CoalTest0.item), Hands.MAIN, false))
        session.assertPacket(PositionRotationC2SP::class.java)
        session.assertUseItem(Hands.MAIN)
        session.assertPacket(BlockInteractC2SP(BlockPosition.EMPTY, Directions.DOWN, Vec3d.EMPTY, ItemStack(CoalTest0.item), Hands.OFF, false, 3))
        session.assertPacket(PositionRotationC2SP::class.java)
        session.assertOnlyPacket(UseItemC2SP(Hands.OFF, 4))
    }

}
