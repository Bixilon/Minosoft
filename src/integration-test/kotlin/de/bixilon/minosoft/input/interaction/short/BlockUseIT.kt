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

package de.bixilon.minosoft.input.interaction.short

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.observer.DataObserver
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.camera.target.targets.BlockTarget
import de.bixilon.minosoft.data.container.equipment.EquipmentSlots
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.entities.entities.player.Hands
import de.bixilon.minosoft.data.registries.blocks.types.stone.StoneTest0
import de.bixilon.minosoft.data.registries.items.CoalTest0
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.EMPTY
import de.bixilon.minosoft.input.interaction.InteractionTestUtil
import de.bixilon.minosoft.input.interaction.InteractionTestUtil.assertUseItem
import de.bixilon.minosoft.input.interaction.InteractionTestUtil.unsafePress
import de.bixilon.minosoft.protocol.network.connection.play.PacketTestUtil.assertNoPacket
import de.bixilon.minosoft.protocol.network.connection.play.PacketTestUtil.assertOnlyPacket
import de.bixilon.minosoft.protocol.network.connection.play.PacketTestUtil.assertPacket
import de.bixilon.minosoft.protocol.packets.c2s.play.block.BlockInteractC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.move.PositionRotationC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.item.UseItemC2SP
import org.testng.annotations.Test

@Test(groups = ["interaction"], dependsOnGroups = ["item", "block"])
class BlockUseIT {

    fun testAirOnStone() {
        val connection = InteractionTestUtil.createConnection()
        connection.camera.target::target.forceSet(DataObserver(BlockTarget(Vec3d.EMPTY, 1.0, Directions.DOWN, StoneTest0.state, null, Vec3i.EMPTY)))
        val use = connection.camera.interactions.use

        use.unsafePress()

        connection.assertPacket(BlockInteractC2SP(Vec3i.EMPTY, Directions.DOWN, Vec3.EMPTY, null, Hands.MAIN, false))
        connection.assertOnlyPacket(BlockInteractC2SP(Vec3i.EMPTY, Directions.DOWN, Vec3.EMPTY, null, Hands.OFF, false, 2))
    }

    fun testCoalOnStone() {
        val connection = InteractionTestUtil.createConnection()
        connection.camera.target::target.forceSet(DataObserver(BlockTarget(Vec3d.EMPTY, 1.0, Directions.DOWN, StoneTest0.state, null, Vec3i.EMPTY)))
        connection.player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(CoalTest0.item)
        val use = connection.camera.interactions.use

        use.unsafePress()

        connection.assertPacket(BlockInteractC2SP(Vec3i.EMPTY, Directions.DOWN, Vec3.EMPTY, ItemStack(CoalTest0.item), Hands.MAIN, false))
        connection.assertPacket(PositionRotationC2SP::class.java)
        connection.assertUseItem(Hands.MAIN)
        connection.assertOnlyPacket(BlockInteractC2SP(Vec3i.EMPTY, Directions.DOWN, Vec3.EMPTY, null, Hands.OFF, false, 3))
    }

    fun testCoalOnStone2() {
        val connection = InteractionTestUtil.createConnection()
        connection.camera.target::target.forceSet(DataObserver(BlockTarget(Vec3d.EMPTY, 1.0, Directions.DOWN, StoneTest0.state, null, Vec3i.EMPTY)))
        connection.player.items.inventory[EquipmentSlots.OFF_HAND] = ItemStack(CoalTest0.item)
        val use = connection.camera.interactions.use

        use.unsafePress()

        connection.assertPacket(BlockInteractC2SP(Vec3i.EMPTY, Directions.DOWN, Vec3.EMPTY, null, Hands.MAIN, false))
        connection.assertPacket(BlockInteractC2SP(Vec3i.EMPTY, Directions.DOWN, Vec3.EMPTY, ItemStack(CoalTest0.item), Hands.OFF, false, 2))
        connection.assertPacket(PositionRotationC2SP::class.java)
        connection.assertUseItem(Hands.OFF)
        connection.assertNoPacket()
    }

    fun testCoalOnStone3() {
        val connection = InteractionTestUtil.createConnection()
        connection.camera.target::target.forceSet(DataObserver(BlockTarget(Vec3d.EMPTY, 1.0, Directions.DOWN, StoneTest0.state, null, Vec3i.EMPTY)))
        connection.player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(CoalTest0.item)
        connection.player.items.inventory[EquipmentSlots.OFF_HAND] = ItemStack(CoalTest0.item)
        val use = connection.camera.interactions.use

        use.unsafePress()

        connection.assertPacket(BlockInteractC2SP(Vec3i.EMPTY, Directions.DOWN, Vec3.EMPTY, ItemStack(CoalTest0.item), Hands.MAIN, false))
        connection.assertPacket(PositionRotationC2SP::class.java)
        connection.assertUseItem(Hands.MAIN)
        connection.assertPacket(BlockInteractC2SP(Vec3i.EMPTY, Directions.DOWN, Vec3.EMPTY, ItemStack(CoalTest0.item), Hands.OFF, false, 3))
        connection.assertPacket(PositionRotationC2SP::class.java)
        connection.assertOnlyPacket(UseItemC2SP(Hands.OFF, 4))
    }

}
