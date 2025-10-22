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

package de.bixilon.minosoft.data.container.types

import de.bixilon.minosoft.data.registries.enchantment.armor.MovementEnchantment
import de.bixilon.minosoft.protocol.network.session.play.SessionTestUtil.createSession
import de.bixilon.minosoft.test.IT
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["containers"])
class EnchantingContainerTest {

    private fun create(): EnchantingContainer {
        val session = createSession()
        val type = IT.REGISTRIES.containerType[EnchantingContainer]!!

        return EnchantingContainer(session, type, null, 1)
    }

    fun `read enchantment 1`() {
        val container = create()
        container.readProperty(4, IT.REGISTRIES.enchantment.getId(MovementEnchantment.DepthStrider))
        assertEquals(container.enchantments[0], MovementEnchantment.DepthStrider)
    }

    fun `read enchantment 2`() {
        val container = create()
        container.readProperty(5, IT.REGISTRIES.enchantment.getId(MovementEnchantment.DepthStrider))
        assertEquals(container.enchantments[1], MovementEnchantment.DepthStrider)
    }
}
