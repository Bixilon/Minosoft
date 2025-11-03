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

package de.bixilon.minosoft.data.container.types.generic

import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.data.container.TestItem1
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity
import de.bixilon.minosoft.data.entities.entities.player.local.PlayerItemManager
import de.bixilon.minosoft.data.registries.blocks.types.entity.storage.ShulkerBoxBlock
import de.bixilon.minosoft.data.registries.containers.ContainerType
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.test.IT
import de.bixilon.minosoft.test.ITUtil.allocate
import org.testng.Assert.assertFalse
import org.testng.Assert.assertTrue
import org.testng.annotations.Test

@Test(groups = ["container"])
class ShulkerBoxContainerTest {

    private fun create(): ShulkerBoxContainer {
        val session = PlaySession::class.java.allocate()
        val player = LocalPlayerEntity::class.java.allocate()
        player::session.forceSet(session)
        player::items.forceSet(PlayerItemManager(player))
        session::player.forceSet(player)

        val container = ShulkerBoxContainer(session, ContainerType(ShulkerBoxContainer.identifier, ShulkerBoxContainer), null, 1)

        return container
    }

    fun `can put any item in slot`() {
        val slot = ShulkerBoxContainer.ShulkerBoxSlotType
        val stack = ItemStack(TestItem1)

        assertTrue(slot.canPut(create(), 1, stack))
    }

    fun `can not put shulker boxes into slot`() {
        val slot = ShulkerBoxContainer.ShulkerBoxSlotType
        val stack = ItemStack(IT.REGISTRIES.item[ShulkerBoxBlock.identifier]!!)

        assertFalse(slot.canPut(create(), 1, stack))
    }

    fun `can not put white shulker boxes into slot`() {
        val slot = ShulkerBoxContainer.ShulkerBoxSlotType
        val stack = ItemStack(IT.REGISTRIES.item[ShulkerBoxBlock.White.identifier]!!)

        assertFalse(slot.canPut(create(), 1, stack))
    }
}
