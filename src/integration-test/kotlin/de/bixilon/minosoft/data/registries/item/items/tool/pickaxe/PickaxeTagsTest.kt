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

package de.bixilon.minosoft.data.registries.item.items.tool.pickaxe

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.registries.blocks.MinecraftBlocks
import de.bixilon.minosoft.data.registries.identified.Identified
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.items.tool.ToolItem
import de.bixilon.minosoft.data.registries.item.items.tool.ToolLevels
import de.bixilon.minosoft.data.registries.item.items.tool.materials.DiamondTool
import de.bixilon.minosoft.data.registries.item.items.tool.materials.IronTool
import de.bixilon.minosoft.data.registries.item.items.tool.materials.StoneTool
import de.bixilon.minosoft.data.registries.item.items.tool.materials.WoodenTool
import de.bixilon.minosoft.data.registries.registries.registry.RegistryItem
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil.createConnection
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.tags.MinecraftTagTypes
import de.bixilon.minosoft.tags.Tag
import de.bixilon.minosoft.tags.TagList
import de.bixilon.minosoft.tags.TagManager
import de.bixilon.minosoft.test.IT
import org.testng.Assert.*
import org.testng.annotations.Test

@Test(groups = ["digging", "item"])
class PickaxeTagsTest {


    protected fun mine(connection: PlayConnection, item: Identified, block: ResourceLocation): Pair<Boolean, Float?> {
        val item: ToolItem = IT.REGISTRIES.item[item]!!.unsafeCast()
        val block = IT.REGISTRIES.block[block]!!.states.default

        val suitable = item.isSuitableFor(connection, block, ItemStack(item))
        val speed = item.getMiningSpeed(connection, block, ItemStack(item))

        return Pair(suitable, speed)
    }

    fun notMineable() {
        val connection = createConnection()
        connection.tags = TagManager(mapOf(MinecraftTagTypes.BLOCK to TagList(mapOf(PickaxeItem.TAG to Tag(setOf(connection.registries.block[MinecraftBlocks.BEACON]!!))))))
        val (suitable, speed) = mine(connection, WoodenTool.WoodenPickaxe, MinecraftBlocks.BONE_BLOCK)
        assertFalse(suitable)
        assertEquals(speed, 1.0f)
    }

    fun mineable() {
        val connection = createConnection()
        connection.tags = TagManager(mapOf(MinecraftTagTypes.BLOCK to TagList(mapOf(PickaxeItem.TAG to Tag(setOf(connection.registries.block[MinecraftBlocks.BEACON]!!))))))
        val (suitable, speed) = mine(connection, WoodenTool.WoodenPickaxe, MinecraftBlocks.BEACON)
        assertTrue(suitable)
        assertEquals(speed, 2.0f)
    }

    fun stone() {
        val connection = createConnection()
        val tag: Tag<RegistryItem> = Tag(setOf(connection.registries.block[MinecraftBlocks.BEACON]!!))
        connection.tags = TagManager(mapOf(MinecraftTagTypes.BLOCK to TagList(mapOf(PickaxeItem.TAG to tag, ToolLevels.STONE.tag!! to tag))))
        val (suitable, speed) = mine(connection, WoodenTool.WoodenPickaxe, MinecraftBlocks.BEACON)
        assertFalse(suitable)
        assertEquals(speed, 2.0f)
    }

    fun stoneStone() {
        val connection = createConnection()
        val tag: Tag<RegistryItem> = Tag(setOf(connection.registries.block[MinecraftBlocks.BEACON]!!))
        connection.tags = TagManager(mapOf(MinecraftTagTypes.BLOCK to TagList(mapOf(PickaxeItem.TAG to tag, ToolLevels.STONE.tag!! to tag))))
        val (suitable, speed) = mine(connection, StoneTool.StonePickaxe, MinecraftBlocks.BEACON)
        assertTrue(suitable)
        assertEquals(speed, 4.0f)
    }

    fun iron() {
        val connection = createConnection()
        val tag: Tag<RegistryItem> = Tag(setOf(connection.registries.block[MinecraftBlocks.BEACON]!!))
        connection.tags = TagManager(mapOf(MinecraftTagTypes.BLOCK to TagList(mapOf(PickaxeItem.TAG to tag, ToolLevels.IRON.tag!! to tag))))
        val (suitable, speed) = mine(connection, StoneTool.StonePickaxe, MinecraftBlocks.BEACON)
        assertFalse(suitable)
        assertEquals(speed, 4.0f)
    }

    fun ironIron() {
        val connection = createConnection()
        val tag: Tag<RegistryItem> = Tag(setOf(connection.registries.block[MinecraftBlocks.BEACON]!!))
        connection.tags = TagManager(mapOf(MinecraftTagTypes.BLOCK to TagList(mapOf(PickaxeItem.TAG to tag, ToolLevels.IRON.tag!! to tag))))
        val (suitable, speed) = mine(connection, IronTool.IronPickaxe, MinecraftBlocks.BEACON)
        assertTrue(suitable)
        assertEquals(speed, 6.0f)
    }

    fun ironStone() {
        val connection = createConnection()
        val tag: Tag<RegistryItem> = Tag(setOf(connection.registries.block[MinecraftBlocks.BEACON]!!))
        connection.tags = TagManager(mapOf(MinecraftTagTypes.BLOCK to TagList(mapOf(PickaxeItem.TAG to tag, ToolLevels.STONE.tag!! to tag))))
        val (suitable, speed) = mine(connection, IronTool.IronPickaxe, MinecraftBlocks.BEACON)
        assertTrue(suitable)
        assertEquals(speed, 6.0f)
    }


    fun diamond() {
        val connection = createConnection()
        val tag: Tag<RegistryItem> = Tag(setOf(connection.registries.block[MinecraftBlocks.BEACON]!!))
        connection.tags = TagManager(mapOf(MinecraftTagTypes.BLOCK to TagList(mapOf(PickaxeItem.TAG to tag, ToolLevels.DIAMOND.tag!! to tag))))
        val (suitable, speed) = mine(connection, IronTool.IronPickaxe, MinecraftBlocks.BEACON)
        assertFalse(suitable)
        assertEquals(speed, 6.0f)
    }

    fun diamondDiamond() {
        val connection = createConnection()
        val tag: Tag<RegistryItem> = Tag(setOf(connection.registries.block[MinecraftBlocks.BEACON]!!))
        connection.tags = TagManager(mapOf(MinecraftTagTypes.BLOCK to TagList(mapOf(PickaxeItem.TAG to tag, ToolLevels.DIAMOND.tag!! to tag))))
        val (suitable, speed) = mine(connection, DiamondTool.DiamondPickaxe, MinecraftBlocks.BEACON)
        assertTrue(suitable)
        assertEquals(speed, 8.0f)
    }

    fun diamondIron() {
        val connection = createConnection()
        val tag: Tag<RegistryItem> = Tag(setOf(connection.registries.block[MinecraftBlocks.BEACON]!!))
        connection.tags = TagManager(mapOf(MinecraftTagTypes.BLOCK to TagList(mapOf(PickaxeItem.TAG to tag, ToolLevels.IRON.tag!! to tag))))
        val (suitable, speed) = mine(connection, DiamondTool.DiamondPickaxe, MinecraftBlocks.BEACON)
        assertTrue(suitable)
        assertEquals(speed, 8.0f)
    }
}
