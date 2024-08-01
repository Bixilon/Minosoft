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

package de.bixilon.minosoft.input.interaction.short

import de.bixilon.minosoft.data.container.equipment.EquipmentSlots
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.entities.entities.player.Hands
import de.bixilon.minosoft.data.registries.items.CoalTest0
import de.bixilon.minosoft.input.interaction.InteractionTestUtil
import de.bixilon.minosoft.input.interaction.InteractionTestUtil.unsafePress
import de.bixilon.minosoft.protocol.network.session.play.PacketTestUtil.assertOnlyPacket
import de.bixilon.minosoft.protocol.network.session.play.PacketTestUtil.assertPacket
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.move.PositionRotationC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.item.UseItemC2SP
import org.testng.annotations.Test

@Test(groups = ["interaction"], dependsOnGroups = ["item", "block"])
class OnAirUseIT {

    fun coalOnAir() {
        val session = InteractionTestUtil.createSession()
        session.player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(CoalTest0.item)
        val use = session.camera.interactions.use

        use.unsafePress()

        session.assertPacket(PositionRotationC2SP::class.java)
        session.assertOnlyPacket(UseItemC2SP(Hands.MAIN))
    }

    fun coalOnAir2() {
        val session = InteractionTestUtil.createSession()
        session.player.items.inventory[EquipmentSlots.OFF_HAND] = ItemStack(CoalTest0.item)
        val use = session.camera.interactions.use

        use.unsafePress()

        session.assertPacket(PositionRotationC2SP::class.java)
        session.assertOnlyPacket(UseItemC2SP(Hands.OFF))
    }

    fun testCoalOnAir3() {
        val session = InteractionTestUtil.createSession()
        session.player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(CoalTest0.item)
        session.player.items.inventory[EquipmentSlots.OFF_HAND] = ItemStack(CoalTest0.item)
        val use = session.camera.interactions.use

        use.unsafePress()

        session.assertPacket(PositionRotationC2SP::class.java)
        session.assertPacket(UseItemC2SP(Hands.MAIN, 1))
        session.assertPacket(PositionRotationC2SP::class.java)
        session.assertOnlyPacket(UseItemC2SP(Hands.OFF, 2))
    }
}
