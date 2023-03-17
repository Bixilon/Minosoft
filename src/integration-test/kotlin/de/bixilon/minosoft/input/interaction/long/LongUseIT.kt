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

import de.bixilon.minosoft.data.container.equipment.EquipmentSlots
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.entities.entities.player.Hands
import de.bixilon.minosoft.data.registries.item.items.entities.chicken.EggItem
import de.bixilon.minosoft.data.registries.item.items.food.AppleItem
import de.bixilon.minosoft.data.registries.item.items.weapon.attack.range.pullable.BowItem
import de.bixilon.minosoft.data.registries.item.items.weapon.defend.ShieldItem
import de.bixilon.minosoft.input.interaction.InteractionTestUtil
import de.bixilon.minosoft.input.interaction.InteractionTestUtil.assertUseItem
import de.bixilon.minosoft.input.interaction.InteractionTestUtil.tick
import de.bixilon.minosoft.input.interaction.InteractionTestUtil.unsafePress
import de.bixilon.minosoft.input.interaction.InteractionTestUtil.unsafeRelease
import de.bixilon.minosoft.physics.ItemUsing
import de.bixilon.minosoft.protocol.network.connection.play.PacketTestUtil.assertNoPacket
import de.bixilon.minosoft.protocol.network.connection.play.PacketTestUtil.assertPacket
import de.bixilon.minosoft.protocol.packets.c2s.play.HotbarSlotC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.PlayerActionC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.item.UseItemC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.move.PositionRotationC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.move.SwingArmC2SP
import org.testng.Assert.*
import org.testng.annotations.Test

@Test(groups = ["interaction"], dependsOnGroups = ["item", "block"])
class LongUseIT {

    fun pullBowWithArrows() {
        val connection = InteractionTestUtil.createConnection()
        connection.player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(BowItem())
        val use = connection.camera.interactions.use

        assertNull(connection.player.using)

        use.unsafePress()
        use.tick(10)

        assertEquals(connection.player.using, ItemUsing(Hands.MAIN, 10))
        assertTrue(connection.player.items.inventory[EquipmentSlots.MAIN_HAND]?.item?.item is BowItem)


        connection.assertPacket(PositionRotationC2SP::class.java)
        connection.assertUseItem(Hands.MAIN)
        connection.assertNoPacket()
    }

    fun pullBowOffhand() {
        val connection = InteractionTestUtil.createConnection()
        connection.player.items.inventory[EquipmentSlots.OFF_HAND] = ItemStack(BowItem())
        val use = connection.camera.interactions.use

        assertNull(connection.player.using)

        use.unsafePress()
        use.tick(10)

        assertEquals(connection.player.using, ItemUsing(Hands.OFF, 10))
        assertTrue(connection.player.items.inventory[EquipmentSlots.OFF_HAND]?.item?.item is BowItem)


        connection.assertPacket(PositionRotationC2SP::class.java)
        connection.assertUseItem(Hands.OFF)
        connection.assertNoPacket()
    }

    fun pullBowWithoutArrows() {
        val connection = InteractionTestUtil.createConnection()
        connection.player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(BowItem())
        val use = connection.camera.interactions.use

        assertNull(connection.player.using)

        use.unsafePress()
        use.tick(10)

        assertNull(connection.player.using)

        // those 2 packets spam
        connection.assertPacket(PositionRotationC2SP::class.java)
        connection.assertUseItem(Hands.MAIN)
        connection.assertPacket(PositionRotationC2SP::class.java)
        connection.assertUseItem(Hands.MAIN)
        connection.assertNoPacket()
    }

    fun shootBow() {
        val connection = InteractionTestUtil.createConnection()
        connection.player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(BowItem())
        val use = connection.camera.interactions.use

        use.unsafePress()
        use.tick(100)
        use.unsafeRelease()

        assertNull(connection.player.using)
        assertTrue(connection.player.items.inventory[EquipmentSlots.MAIN_HAND]?.item?.item is BowItem)


        connection.assertPacket(PositionRotationC2SP::class.java)
        connection.assertUseItem(Hands.MAIN)
        connection.assertPacket(PlayerActionC2SP(PlayerActionC2SP.Actions.RELEASE_ITEM))
        connection.assertNoPacket()
    }

    fun testSlotChangeBow() {
        val connection = InteractionTestUtil.createConnection()
        connection.player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(BowItem())
        val use = connection.camera.interactions.use

        use.unsafePress()
        use.tick(100)
        connection.camera.interactions.hotbar.selectSlot(2)
        use.tick()

        assertNull(connection.player.using)


        connection.assertPacket(PositionRotationC2SP::class.java)
        connection.assertUseItem(Hands.MAIN)
        connection.assertPacket(HotbarSlotC2SP(2))
        connection.assertNoPacket()
    }

    fun testOffhandSlotChange() {
        val connection = InteractionTestUtil.createConnection()
        connection.player.items.inventory[EquipmentSlots.OFF_HAND] = ItemStack(BowItem())
        val use = connection.camera.interactions.use

        use.unsafePress()
        use.tick(100)
        connection.camera.interactions.hotbar.selectSlot(2)
        use.tick(10)

        assertEquals(connection.player.using, ItemUsing(Hands.OFF, 110))
        assertTrue(connection.player.items.inventory[EquipmentSlots.OFF_HAND]?.item?.item is BowItem)


        connection.assertPacket(PositionRotationC2SP::class.java)
        connection.assertUseItem(Hands.OFF)
        connection.assertPacket(HotbarSlotC2SP(2))
        connection.assertNoPacket()
    }


    fun eatSingleGoldenApple() {
        val connection = InteractionTestUtil.createConnection()
        connection.player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(AppleItem.GoldenAppleItem())
        val use = connection.camera.interactions.use

        use.unsafePress()
        use.tick(32)

        assertNull(connection.player.using)
        assertNull(connection.player.items.inventory[EquipmentSlots.MAIN_HAND])

        connection.assertPacket(PositionRotationC2SP::class.java)
        connection.assertUseItem(Hands.MAIN)
        connection.assertNoPacket()
    }

    fun eatingSlotChange() {
        val connection = InteractionTestUtil.createConnection()
        connection.player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(AppleItem.GoldenAppleItem())
        val use = connection.camera.interactions.use

        use.unsafePress()
        use.tick(25)
        connection.camera.interactions.hotbar.selectSlot(2)
        use.tick(7)


        assertNull(connection.player.using)
        assertNull(connection.player.items.inventory[EquipmentSlots.MAIN_HAND])

        connection.assertPacket(PositionRotationC2SP::class.java)
        connection.assertUseItem(Hands.MAIN)
        connection.assertPacket(HotbarSlotC2SP(2))
        connection.assertNoPacket()
    }

    fun abortEating() {
        val connection = InteractionTestUtil.createConnection()
        connection.player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(AppleItem.GoldenAppleItem())
        val use = connection.camera.interactions.use

        use.unsafePress()
        use.tick(31)
        use.unsafeRelease()

        assertNull(connection.player.using)
        assertTrue(connection.player.items.inventory[EquipmentSlots.MAIN_HAND]?.item?.item is AppleItem.GoldenAppleItem)

        connection.assertPacket(PositionRotationC2SP::class.java)
        connection.assertUseItem(Hands.MAIN)
        connection.assertPacket(PlayerActionC2SP(PlayerActionC2SP.Actions.RELEASE_ITEM))
        connection.assertNoPacket()
    }

    fun eatMultipleGoldenApple() {
        val connection = InteractionTestUtil.createConnection()
        connection.player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(AppleItem.GoldenAppleItem(), count = 19)
        val use = connection.camera.interactions.use

        use.unsafePress()
        use.tick(64)

        assertEquals(connection.player.using, ItemUsing(Hands.MAIN, 0))
        assertEquals(connection.player.items.inventory[EquipmentSlots.MAIN_HAND], ItemStack(AppleItem.GoldenAppleItem(), count = 17))


        for (i in 0 until 3) {
            connection.assertPacket(PositionRotationC2SP::class.java)
            connection.assertPacket(UseItemC2SP(Hands.MAIN, 1 + i))
        }
        connection.assertNoPacket()
    }

    fun eat5GoldenApples() {
        val connection = InteractionTestUtil.createConnection()
        connection.player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(AppleItem.GoldenAppleItem(), count = 5)
        val use = connection.camera.interactions.use

        use.unsafePress()
        use.tick(160)

        assertNull(connection.player.using)
        assertNull(connection.player.items.inventory[EquipmentSlots.MAIN_HAND])


        for (i in 0 until 5) {
            connection.assertPacket(PositionRotationC2SP::class.java)
            connection.assertPacket(UseItemC2SP(Hands.MAIN, 1 + i))
        }

        connection.assertNoPacket()
    }

    fun eatAndStopMultipleGoldenApple() {
        val connection = InteractionTestUtil.createConnection()
        connection.player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(AppleItem.GoldenAppleItem(), count = 19)
        val use = connection.camera.interactions.use

        use.unsafePress()
        use.tick(64)
        use.unsafeRelease()

        assertNull(connection.player.using)
        assertEquals(connection.player.items.inventory[EquipmentSlots.MAIN_HAND], ItemStack(AppleItem.GoldenAppleItem(), count = 17))


        for (i in 0 until 3) {
            connection.assertPacket(PositionRotationC2SP::class.java)
            connection.assertPacket(UseItemC2SP(Hands.MAIN, 1 + i))
        }
        connection.assertPacket(PlayerActionC2SP(PlayerActionC2SP.Actions.RELEASE_ITEM))
        connection.assertNoPacket()
    }

    fun shortUseInOffHand() {
        val connection = InteractionTestUtil.createConnection()
        connection.player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(ShieldItem())
        connection.player.items.inventory[EquipmentSlots.OFF_HAND] = ItemStack(EggItem(), count = 3)
        val use = connection.camera.interactions.use

        use.unsafePress()
        use.tick(10)


        connection.assertPacket(PositionRotationC2SP::class.java)
        connection.assertUseItem(Hands.MAIN)
        connection.assertNoPacket()

        use.unsafeRelease()

        connection.assertPacket(PlayerActionC2SP(PlayerActionC2SP.Actions.RELEASE_ITEM))
        connection.assertNoPacket()
    }

    fun shortUseInMainHand() {
        val connection = InteractionTestUtil.createConnection()
        connection.player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(EggItem(), count = 3)
        connection.player.items.inventory[EquipmentSlots.OFF_HAND] = ItemStack(ShieldItem())
        val use = connection.camera.interactions.use

        use.unsafePress()
        use.tick(8)


        for (i in 0 until 3) {
            connection.assertPacket(PositionRotationC2SP::class.java)
            connection.assertPacket(UseItemC2SP(Hands.MAIN, 1 + i))
            connection.assertPacket(SwingArmC2SP(Hands.MAIN))
        }
        connection.assertNoPacket()
    }

    fun bothHands() {
        val connection = InteractionTestUtil.createConnection()
        connection.player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(ShieldItem())
        connection.player.items.inventory[EquipmentSlots.OFF_HAND] = ItemStack(AppleItem.GoldenAppleItem(), count = 3)
        val use = connection.camera.interactions.use

        use.unsafePress()
        use.tick(10)

        connection.assertPacket(PositionRotationC2SP::class.java)
        connection.assertUseItem(Hands.MAIN)

        connection.assertNoPacket()
    }

    fun useAndRemoveItem() {
        val connection = InteractionTestUtil.createConnection()
        connection.player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(AppleItem.GoldenAppleItem(), count = 5)
        val use = connection.camera.interactions.use

        use.unsafePress()
        use.tick(10)
        connection.player.items.inventory[EquipmentSlots.MAIN_HAND] = null
        use.tick()

        assertNull(connection.player.using)
        assertNull(connection.player.items.inventory[EquipmentSlots.MAIN_HAND])


        connection.assertPacket(PositionRotationC2SP::class.java)
        connection.assertUseItem(Hands.MAIN)
        connection.assertNoPacket()
    }


    // TODO: test crossbow (charging and releasing)
    // TODO: test on entities, ...
}
