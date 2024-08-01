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
import de.bixilon.minosoft.protocol.network.session.play.PacketTestUtil.assertNoPacket
import de.bixilon.minosoft.protocol.network.session.play.PacketTestUtil.assertPacket
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.move.PositionRotationC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.player.HotbarSlotC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.player.PlayerActionC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.player.SwingArmC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.item.UseItemC2SP
import de.bixilon.minosoft.test.ITUtil.todo
import org.testng.Assert.*
import org.testng.annotations.Test

@Test(groups = ["interaction"], dependsOnGroups = ["item", "block"])
class LongUseIT {

    fun pullBowWithArrows() {
        val session = InteractionTestUtil.createSession()
        session.player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(BowItem())
        val use = session.camera.interactions.use

        assertNull(session.player.using)

        use.unsafePress()
        use.tick(10)

        assertEquals(session.player.using, ItemUsing(Hands.MAIN, 10))
        assertTrue(session.player.items.inventory[EquipmentSlots.MAIN_HAND]?.item?.item is BowItem)


        session.assertPacket(PositionRotationC2SP::class.java)
        session.assertUseItem(Hands.MAIN)
        session.assertNoPacket()
    }

    fun pullBowOffhand() {
        val session = InteractionTestUtil.createSession()
        session.player.items.inventory[EquipmentSlots.OFF_HAND] = ItemStack(BowItem())
        val use = session.camera.interactions.use

        assertNull(session.player.using)

        use.unsafePress()
        use.tick(10)

        assertEquals(session.player.using, ItemUsing(Hands.OFF, 10))
        assertTrue(session.player.items.inventory[EquipmentSlots.OFF_HAND]?.item?.item is BowItem)


        session.assertPacket(PositionRotationC2SP::class.java)
        session.assertUseItem(Hands.OFF)
        session.assertNoPacket()
    }

    fun pullBowWithoutArrows() {
        todo()
        val session = InteractionTestUtil.createSession()
        session.player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(BowItem())
        val use = session.camera.interactions.use

        assertNull(session.player.using)

        use.unsafePress()
        use.tick(10)

        assertNull(session.player.using)

        // those 2 packets spam
        session.assertPacket(PositionRotationC2SP::class.java)
        session.assertUseItem(Hands.MAIN)
        session.assertPacket(PositionRotationC2SP::class.java)
        session.assertUseItem(Hands.MAIN)
        session.assertNoPacket()
    }

    fun shootBow() {
        val session = InteractionTestUtil.createSession()
        session.player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(BowItem())
        val use = session.camera.interactions.use

        use.unsafePress()
        use.tick(100)
        use.unsafeRelease()

        assertNull(session.player.using)
        assertTrue(session.player.items.inventory[EquipmentSlots.MAIN_HAND]?.item?.item is BowItem)


        session.assertPacket(PositionRotationC2SP::class.java)
        session.assertUseItem(Hands.MAIN)
        session.assertPacket(PlayerActionC2SP(PlayerActionC2SP.Actions.RELEASE_ITEM))
        session.assertNoPacket()
    }

    fun testSlotChangeBow() {
        val session = InteractionTestUtil.createSession()
        session.player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(BowItem())
        val use = session.camera.interactions.use

        use.unsafePress()
        use.tick(100)
        session.camera.interactions.hotbar.selectSlot(2)
        use.tick()

        assertNull(session.player.using)


        session.assertPacket(PositionRotationC2SP::class.java)
        session.assertUseItem(Hands.MAIN)
        session.assertPacket(HotbarSlotC2SP(2))
        session.assertNoPacket()
    }

    fun testOffhandSlotChange() {
        val session = InteractionTestUtil.createSession()
        session.player.items.inventory[EquipmentSlots.OFF_HAND] = ItemStack(BowItem())
        val use = session.camera.interactions.use

        use.unsafePress()
        use.tick(100)
        session.camera.interactions.hotbar.selectSlot(2)
        use.tick(10)

        assertEquals(session.player.using, ItemUsing(Hands.OFF, 110))
        assertTrue(session.player.items.inventory[EquipmentSlots.OFF_HAND]?.item?.item is BowItem)


        session.assertPacket(PositionRotationC2SP::class.java)
        session.assertUseItem(Hands.OFF)
        session.assertPacket(HotbarSlotC2SP(2))
        session.assertNoPacket()
    }


    fun eatSingleGoldenApple() {
        val session = InteractionTestUtil.createSession()
        session.player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(AppleItem.GoldenAppleItem())
        val use = session.camera.interactions.use

        use.unsafePress()
        use.tick(32)

        assertNull(session.player.using)
        assertNull(session.player.items.inventory[EquipmentSlots.MAIN_HAND])

        session.assertPacket(PositionRotationC2SP::class.java)
        session.assertUseItem(Hands.MAIN)
        session.assertNoPacket()
    }

    fun eatingSlotChange() {
        val session = InteractionTestUtil.createSession()
        session.player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(AppleItem.GoldenAppleItem())
        val use = session.camera.interactions.use

        use.unsafePress()
        use.tick(25)
        session.camera.interactions.hotbar.selectSlot(2)
        use.tick(7)


        assertNull(session.player.using)
        assertNull(session.player.items.inventory[EquipmentSlots.MAIN_HAND])

        session.assertPacket(PositionRotationC2SP::class.java)
        session.assertUseItem(Hands.MAIN)
        session.assertPacket(HotbarSlotC2SP(2))
        session.assertNoPacket()
    }

    fun abortEating() {
        val session = InteractionTestUtil.createSession()
        session.player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(AppleItem.GoldenAppleItem())
        val use = session.camera.interactions.use

        use.unsafePress()
        use.tick(31)
        use.unsafeRelease()

        assertNull(session.player.using)
        assertTrue(session.player.items.inventory[EquipmentSlots.MAIN_HAND]?.item?.item is AppleItem.GoldenAppleItem)

        session.assertPacket(PositionRotationC2SP::class.java)
        session.assertUseItem(Hands.MAIN)
        session.assertPacket(PlayerActionC2SP(PlayerActionC2SP.Actions.RELEASE_ITEM))
        session.assertNoPacket()
    }

    fun eatMultipleGoldenApple() {
        val session = InteractionTestUtil.createSession()
        session.player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(AppleItem.GoldenAppleItem(), count = 19)
        val use = session.camera.interactions.use

        use.unsafePress()
        use.tick(64)

        assertEquals(session.player.using, ItemUsing(Hands.MAIN, 0))
        assertEquals(session.player.items.inventory[EquipmentSlots.MAIN_HAND], ItemStack(AppleItem.GoldenAppleItem(), count = 17))


        for (i in 0 until 3) {
            session.assertPacket(PositionRotationC2SP::class.java)
            session.assertPacket(UseItemC2SP(Hands.MAIN, 1 + i))
        }
        session.assertNoPacket()
    }

    fun eat5GoldenApples() {
        val session = InteractionTestUtil.createSession()
        session.player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(AppleItem.GoldenAppleItem(), count = 5)
        val use = session.camera.interactions.use

        use.unsafePress()
        use.tick(160)

        assertNull(session.player.using)
        assertNull(session.player.items.inventory[EquipmentSlots.MAIN_HAND])


        for (i in 0 until 5) {
            session.assertPacket(PositionRotationC2SP::class.java)
            session.assertPacket(UseItemC2SP(Hands.MAIN, 1 + i))
        }

        session.assertNoPacket()
    }

    fun eatAndStopMultipleGoldenApple() {
        val session = InteractionTestUtil.createSession()
        session.player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(AppleItem.GoldenAppleItem(), count = 19)
        val use = session.camera.interactions.use

        use.unsafePress()
        use.tick(64)
        use.unsafeRelease()

        assertNull(session.player.using)
        assertEquals(session.player.items.inventory[EquipmentSlots.MAIN_HAND], ItemStack(AppleItem.GoldenAppleItem(), count = 17))


        for (i in 0 until 3) {
            session.assertPacket(PositionRotationC2SP::class.java)
            session.assertPacket(UseItemC2SP(Hands.MAIN, 1 + i))
        }
        session.assertPacket(PlayerActionC2SP(PlayerActionC2SP.Actions.RELEASE_ITEM))
        session.assertNoPacket()
    }

    fun shortUseInOffHand() {
        val session = InteractionTestUtil.createSession()
        session.player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(ShieldItem())
        session.player.items.inventory[EquipmentSlots.OFF_HAND] = ItemStack(EggItem(), count = 3)
        val use = session.camera.interactions.use

        use.unsafePress()
        use.tick(10)


        session.assertPacket(PositionRotationC2SP::class.java)
        session.assertUseItem(Hands.MAIN)
        session.assertNoPacket()

        use.unsafeRelease()

        session.assertPacket(PlayerActionC2SP(PlayerActionC2SP.Actions.RELEASE_ITEM))
        session.assertNoPacket()
    }

    fun shortUseInMainHand() {
        val session = InteractionTestUtil.createSession()
        session.player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(EggItem(), count = 3)
        session.player.items.inventory[EquipmentSlots.OFF_HAND] = ItemStack(ShieldItem())
        val use = session.camera.interactions.use

        use.unsafePress()
        use.tick(8)


        for (i in 0 until 3) {
            session.assertPacket(PositionRotationC2SP::class.java)
            session.assertPacket(UseItemC2SP(Hands.MAIN, 1 + i))
            session.assertPacket(SwingArmC2SP(Hands.MAIN))
        }
        session.assertNoPacket()
    }

    fun bothHands() {
        val session = InteractionTestUtil.createSession()
        session.player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(ShieldItem())
        session.player.items.inventory[EquipmentSlots.OFF_HAND] = ItemStack(AppleItem.GoldenAppleItem(), count = 3)
        val use = session.camera.interactions.use

        use.unsafePress()
        use.tick(10)

        session.assertPacket(PositionRotationC2SP::class.java)
        session.assertUseItem(Hands.MAIN)

        session.assertNoPacket()
    }

    fun useAndRemoveItem() {
        val session = InteractionTestUtil.createSession()
        session.player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(AppleItem.GoldenAppleItem(), count = 5)
        val use = session.camera.interactions.use

        use.unsafePress()
        use.tick(10)
        session.player.items.inventory[EquipmentSlots.MAIN_HAND] = null
        use.tick()

        assertNull(session.player.using)
        assertNull(session.player.items.inventory[EquipmentSlots.MAIN_HAND])


        session.assertPacket(PositionRotationC2SP::class.java)
        session.assertUseItem(Hands.MAIN)
        session.assertNoPacket()
    }


    // TODO: test crossbow (charging and releasing)
    // TODO: test on entities, ...
}
