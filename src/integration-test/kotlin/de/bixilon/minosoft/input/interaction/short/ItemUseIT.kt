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
import de.bixilon.minosoft.data.registries.items.EggTest0
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.EMPTY
import de.bixilon.minosoft.input.interaction.InteractionTestUtil.assertUseItem
import de.bixilon.minosoft.input.interaction.InteractionTestUtil.createConnection
import de.bixilon.minosoft.input.interaction.InteractionTestUtil.tick
import de.bixilon.minosoft.input.interaction.InteractionTestUtil.unsafePress
import de.bixilon.minosoft.input.interaction.InteractionTestUtil.unsafeRelease
import de.bixilon.minosoft.protocol.network.connection.play.PacketTestUtil.assertNoPacket
import de.bixilon.minosoft.protocol.network.connection.play.PacketTestUtil.assertOnlyPacket
import de.bixilon.minosoft.protocol.network.connection.play.PacketTestUtil.assertPacket
import de.bixilon.minosoft.protocol.packets.c2s.play.block.BlockInteractC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.item.UseItemC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.move.PositionRotationC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.move.SwingArmC2SP
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["interaction"], dependsOnGroups = ["item", "block"])
class ItemUseIT {

    fun eggOnAir() {
        val connection = createConnection()
        connection.player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(EggTest0.item)
        val use = connection.camera.interactions.use

        use.unsafePress()

        connection.assertPacket(PositionRotationC2SP::class.java)
        connection.assertUseItem(Hands.MAIN)
        connection.assertOnlyPacket(SwingArmC2SP(Hands.MAIN))
    }

    fun eggOnAir2() {
        val connection = createConnection()
        connection.player.items.inventory[EquipmentSlots.OFF_HAND] = ItemStack(EggTest0.item)
        val use = connection.camera.interactions.use

        use.unsafePress()

        connection.assertPacket(PositionRotationC2SP::class.java)
        connection.assertUseItem(Hands.OFF)
        connection.assertOnlyPacket(SwingArmC2SP(Hands.OFF))
    }

    fun eggOnAir3() {
        val connection = createConnection()
        connection.player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(EggTest0.item)
        connection.player.items.inventory[EquipmentSlots.OFF_HAND] = ItemStack(EggTest0.item)
        val use = connection.camera.interactions.use

        use.unsafePress()

        connection.assertPacket(PositionRotationC2SP::class.java)
        connection.assertUseItem(Hands.MAIN)
        connection.assertOnlyPacket(SwingArmC2SP(Hands.MAIN))
    }

    fun eggOnStone() {
        val connection = createConnection()
        connection.camera.target::target.forceSet(DataObserver(BlockTarget(Vec3d.EMPTY, 1.0, Directions.DOWN, StoneTest0.state, null, Vec3i.EMPTY)))
        connection.player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(EggTest0.item)
        val use = connection.camera.interactions.use

        use.unsafePress()

        connection.assertPacket(BlockInteractC2SP(Vec3i.EMPTY, Directions.DOWN, Vec3.EMPTY, ItemStack(EggTest0.item), Hands.MAIN, false))
        connection.assertPacket(PositionRotationC2SP::class.java)
        connection.assertUseItem(Hands.MAIN)
        connection.assertOnlyPacket(SwingArmC2SP(Hands.MAIN))
    }

    fun eggOnStone2() {
        val connection = createConnection()
        connection.camera.target::target.forceSet(DataObserver(BlockTarget(Vec3d.EMPTY, 1.0, Directions.DOWN, StoneTest0.state, null, Vec3i.EMPTY)))
        connection.player.items.inventory[EquipmentSlots.OFF_HAND] = ItemStack(EggTest0.item)
        val use = connection.camera.interactions.use

        use.unsafePress()

        connection.assertPacket(BlockInteractC2SP(Vec3i.EMPTY, Directions.DOWN, Vec3.EMPTY, null, Hands.MAIN, false))
        connection.assertPacket(BlockInteractC2SP(Vec3i.EMPTY, Directions.DOWN, Vec3.EMPTY, ItemStack(EggTest0.item), Hands.OFF, false, 2))
        connection.assertPacket(PositionRotationC2SP::class.java)
        connection.assertUseItem(Hands.OFF)
        connection.assertOnlyPacket(SwingArmC2SP(Hands.OFF))
    }

    fun eggOnStone3() {
        val connection = createConnection()
        connection.camera.target::target.forceSet(DataObserver(BlockTarget(Vec3d.EMPTY, 1.0, Directions.DOWN, StoneTest0.state, null, Vec3i.EMPTY)))
        connection.player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(EggTest0.item)
        connection.player.items.inventory[EquipmentSlots.OFF_HAND] = ItemStack(EggTest0.item)
        val use = connection.camera.interactions.use

        use.unsafePress()

        connection.assertPacket(BlockInteractC2SP(Vec3i.EMPTY, Directions.DOWN, Vec3.EMPTY, ItemStack(EggTest0.item), Hands.MAIN, false))
        connection.assertPacket(PositionRotationC2SP::class.java)
        connection.assertUseItem(Hands.MAIN)
        connection.assertOnlyPacket(SwingArmC2SP(Hands.MAIN))
    }


    fun eggOnAirHold() {
        val connection = createConnection()
        connection.player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(EggTest0.item, count = 16)
        val use = connection.camera.interactions.use

        use.unsafePress()

        use.tick(15)

        assertEquals(connection.player.items.inventory[Hands.MAIN], ItemStack(EggTest0.item, count = 12))

        for (x in 0 until 4) {
            connection.assertPacket(PositionRotationC2SP::class.java)
            connection.assertPacket(UseItemC2SP(Hands.MAIN, 1 + x))
            connection.assertPacket(SwingArmC2SP(Hands.MAIN))
        }
        connection.assertNoPacket()
    }

    fun eggOnAirMultipleClicks() {
        val connection = createConnection()
        connection.player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(EggTest0.item, count = 16)
        val use = connection.camera.interactions.use

        for (x in 0 until 10) {
            use.unsafePress()
            connection.assertPacket(PositionRotationC2SP::class.java)
            connection.assertPacket(UseItemC2SP(Hands.MAIN, 1 + x))
            connection.assertOnlyPacket(SwingArmC2SP(Hands.MAIN))
            use.unsafeRelease()
        }
    }
}
