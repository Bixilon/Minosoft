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

import de.bixilon.minosoft.data.world.vec.vec3.f.Vec3f
import de.bixilon.minosoft.data.world.vec.vec3.d.Vec3d
import de.bixilon.kutil.observer.DataObserver
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.camera.target.targets.BlockTarget
import de.bixilon.minosoft.data.container.equipment.EquipmentSlots
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.entities.entities.player.Hands
import de.bixilon.minosoft.data.registries.item.items.fire.FireChargeItem
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import de.bixilon.minosoft.input.interaction.InteractionTestUtil
import de.bixilon.minosoft.input.interaction.InteractionTestUtil.assertUseItem
import de.bixilon.minosoft.input.interaction.InteractionTestUtil.unsafePress
import de.bixilon.minosoft.input.interaction.InteractionTestUtil.unsafeRelease
import de.bixilon.minosoft.protocol.network.session.play.PacketTestUtil.assertNoPacket
import de.bixilon.minosoft.protocol.network.session.play.PacketTestUtil.assertPacket
import de.bixilon.minosoft.protocol.packets.c2s.play.block.BlockInteractC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.move.PositionRotationC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.player.SwingArmC2SP
import de.bixilon.minosoft.test.IT
import org.testng.SkipException
import org.testng.annotations.Test

@Test(groups = ["interaction"], dependsOnGroups = ["item", "block"])
class BlockPlaceIT {

    fun fireChargePlace() { // bedwars
        val session = InteractionTestUtil.createSession()
        val item = session.registries.item[FireChargeItem] ?: throw SkipException("fire charge")
        session.camera.target::target.forceSet(DataObserver(BlockTarget(Vec3d.EMPTY, 1.0, Directions.UP, IT.BLOCK_1, null, BlockPosition.EMPTY)))
        session.player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(item, 2)
        val use = session.camera.interactions.use

        use.unsafePress()
        use.unsafeRelease()

        session.assertPacket(BlockInteractC2SP(BlockPosition.EMPTY, Directions.UP, Vec3f.EMPTY, ItemStack(item, 2), Hands.MAIN, false, 1))
        session.assertPacket(SwingArmC2SP(Hands.MAIN))
        // TODO: check placed fire?
        session.assertNoPacket()

        session.camera.target::target.forceSet(DataObserver(null))

        use.unsafePress()
        use.unsafeRelease()

        session.assertPacket(PositionRotationC2SP::class.java)
        session.assertUseItem(Hands.MAIN)
        session.assertNoPacket()
    }
}
