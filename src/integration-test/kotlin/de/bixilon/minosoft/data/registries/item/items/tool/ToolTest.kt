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

package de.bixilon.minosoft.data.registries.item.items.tool

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.registries.identified.Identified
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil
import de.bixilon.minosoft.test.IT
import org.testng.SkipException

abstract class ToolTest {

    protected fun mine(item: Identified, block: ResourceLocation): Pair<Boolean, Float?> {
        val connection = ConnectionTestUtil.createConnection()
        val item: ToolItem = IT.REGISTRIES.item[item]?.unsafeCast() ?: throw SkipException("tool not available")
        val block = IT.REGISTRIES.block[block]?.states?.default ?: throw SkipException("block not available")

        val suitable = item.isSuitableFor(connection, block, ItemStack(item))
        val speed = item.getMiningSpeed(connection, block, ItemStack(item))

        return Pair(suitable, speed)
    }
}
