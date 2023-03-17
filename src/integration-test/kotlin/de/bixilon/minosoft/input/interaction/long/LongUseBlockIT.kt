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

package de.bixilon.minosoft.input.interaction.long

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.observer.DataObserver
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.camera.target.targets.BlockTarget
import de.bixilon.minosoft.data.container.equipment.EquipmentSlots
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.entities.entities.player.Hands
import de.bixilon.minosoft.data.registries.blocks.DirtTest0
import de.bixilon.minosoft.data.registries.blocks.types.stone.StoneTest0
import de.bixilon.minosoft.data.registries.item.items.weapon.defend.ShieldItem
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.EMPTY
import de.bixilon.minosoft.input.interaction.InteractionTestUtil
import de.bixilon.minosoft.input.interaction.InteractionTestUtil.assertUseItem
import de.bixilon.minosoft.input.interaction.InteractionTestUtil.tick
import de.bixilon.minosoft.input.interaction.InteractionTestUtil.unsafePress
import de.bixilon.minosoft.physics.ItemUsing
import de.bixilon.minosoft.protocol.network.connection.play.PacketTestUtil.assertNoPacket
import de.bixilon.minosoft.protocol.network.connection.play.PacketTestUtil.assertPacket
import de.bixilon.minosoft.protocol.packets.c2s.play.block.BlockInteractC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.move.PositionRotationC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.move.SwingArmC2SP
import org.testng.Assert.assertEquals
import org.testng.Assert.assertNull
import org.testng.annotations.Test

@Test(groups = ["interaction"], dependsOnGroups = ["item", "block"])
class LongUseBlockIT {


    fun shieldOnDirt() {
        val connection = InteractionTestUtil.createConnection()
        connection.player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(ShieldItem())
        connection.camera.target::target.forceSet(DataObserver(BlockTarget(Vec3d.EMPTY, 1.0, Directions.DOWN, DirtTest0.state, null, Vec3i.EMPTY)))
        val use = connection.camera.interactions.use


        use.unsafePress()
        use.tick(10)

        assertEquals(connection.player.using, ItemUsing(Hands.MAIN, 10))


        connection.assertPacket(BlockInteractC2SP::class.java).let { assertEquals(it.hand, Hands.MAIN) }
        connection.assertPacket(PositionRotationC2SP::class.java)
        connection.assertUseItem(Hands.MAIN)
        connection.assertNoPacket()
    }

    fun offHandShieldOnDirt() {
        val connection = InteractionTestUtil.createConnection()
        connection.player.items.inventory[EquipmentSlots.OFF_HAND] = ItemStack(ShieldItem())
        connection.camera.target::target.forceSet(DataObserver(BlockTarget(Vec3d.EMPTY, 1.0, Directions.DOWN, DirtTest0.state, null, Vec3i.EMPTY)))
        val use = connection.camera.interactions.use


        use.unsafePress()
        use.tick(10)

        assertEquals(connection.player.using, ItemUsing(Hands.OFF, 10))


        connection.assertPacket(BlockInteractC2SP::class.java).let { assertEquals(it.hand, Hands.MAIN) }
        connection.assertPacket(BlockInteractC2SP::class.java).let { assertEquals(it.hand, Hands.OFF) }
        connection.assertPacket(PositionRotationC2SP::class.java)
        connection.assertUseItem(Hands.OFF)
        connection.assertNoPacket()
    }

    fun shieldOnRepeater() {
        val connection = InteractionTestUtil.createConnection()
        connection.player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(ShieldItem())
        connection.camera.target::target.forceSet(DataObserver(BlockTarget(Vec3d.EMPTY, 1.0, Directions.DOWN, StoneTest0.state, null, Vec3i.EMPTY)))
        val use = connection.camera.interactions.use


        use.unsafePress()
        use.tick(10)


        assertNull(connection.player.using)


        // every 5 ticks
        connection.assertPacket(BlockInteractC2SP::class.java)
        connection.assertPacket(SwingArmC2SP(Hands.MAIN))
        connection.assertPacket(BlockInteractC2SP::class.java)
        connection.assertPacket(SwingArmC2SP(Hands.MAIN))
        connection.assertNoPacket()
    }
}
