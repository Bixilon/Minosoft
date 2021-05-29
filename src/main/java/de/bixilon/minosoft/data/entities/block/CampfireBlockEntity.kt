/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.entities.block

import de.bixilon.minosoft.data.Directions
import de.bixilon.minosoft.data.inventory.ItemStack
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.blocks.BlockState
import de.bixilon.minosoft.data.mappings.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.mappings.blocks.types.CampfireBlock
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.fire.SmokeParticle
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.util.KUtil.chance
import de.bixilon.minosoft.util.KUtil.nullCast
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.listCast
import glm_.vec3.Vec3
import glm_.vec3.Vec3i
import kotlin.random.Random

class CampfireBlockEntity(connection: PlayConnection) : BlockEntity(connection) {
    val items: Array<ItemStack?> = arrayOfNulls(RenderConstants.CAMPFIRE_ITEMS)


    override fun updateNBT(nbt: Map<String, Any>) {
        val itemArray = nbt["Items"]?.listCast<Map<String, Any>>() ?: let {
            for (index in items.indices) {
                items[index] = null
            }
            return
        }
        // ToDo: If 1 item gets removed, the index is wrong
        for (index in items.indices) {
            val slot = itemArray.getOrNull(index)
            if (slot == null) {
                items[index] = null
                continue
            }
            val itemStack = ItemStack(
                item = connection.registries.itemRegistry[slot["id"]?.nullCast<String>()!!]!!,
                connection = connection,
                count = slot["Count"]?.nullCast<Number>()?.toInt() ?: 1,
            )

            items[slot["Slot"]?.nullCast<Number>()?.toInt()!!] = itemStack
        }
    }


    override fun realTick(connection: PlayConnection, blockState: BlockState, blockPosition: Vec3i) {
        if (blockState.properties[BlockProperties.LIT] != true) {
            return
        }
        if (blockState.block !is CampfireBlock) {
            return
        }

        if (Random.nextFloat() < 0.11f) {
            for (i in 0 until Random.nextInt(2) + 2) {
                blockState.block.spawnSmokeParticles(connection, blockState, blockPosition, false)
            }
        }

        val facing = (blockState.properties[BlockProperties.FACING] as Directions).horizontalId

        for ((index, item) in items.withIndex()) {
            item ?: continue
            if (!Random.chance(20)) {
                continue
            }
            val direction = Directions.byHorizontal(Math.floorMod(index + facing, Directions.SIDES.size))

            val a = 0.3125f

            val position = Vec3(blockPosition) + Vec3(
                0.5f - direction.vector.x * a + direction.rotateYC().vector.x * a,
                0.5f,
                0.5f - direction.vector.z * a + direction.rotateYC().vector.z * a,
            )

            for (i in 0 until 4) {
                connection.world.addParticle(SmokeParticle(connection, position, Vec3(0.0f, 5.0E-4f, 0.0f)))
            }

        }
    }

    companion object : BlockEntityFactory<CampfireBlockEntity> {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("minecraft:campfire")

        override fun build(connection: PlayConnection): CampfireBlockEntity {
            return CampfireBlockEntity(connection)
        }
    }
}
