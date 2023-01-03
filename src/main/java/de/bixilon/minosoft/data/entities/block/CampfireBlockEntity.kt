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

package de.bixilon.minosoft.data.entities.block

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.kutil.random.RandomUtil.chance
import de.bixilon.minosoft.data.container.ItemStackUtil
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.types.entity.CampfireBlock
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.fire.SmokeParticle
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.listCast
import java.util.*

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
            val itemStack = ItemStackUtil.of(
                item = connection.registries.item[slot["id"].unsafeCast<String>()]!!,
                connection = connection,
                count = slot["Count"]?.toInt() ?: 1,
            )

            items[slot["Slot"]!!.toInt()] = itemStack
        }
    }


    override fun tick(connection: PlayConnection, blockState: BlockState, blockPosition: Vec3i, random: Random) {
        if (blockState.properties[BlockProperties.LIT] != true) {
            return
        }
        if (blockState.block !is CampfireBlock) {
            return
        }

        if (random.nextFloat() < 0.11f) {
            for (i in 0 until random.nextInt(2) + 2) {
                blockState.block.spawnSmokeParticles(connection, blockState, blockPosition, false, random)
            }
        }

        val facing = blockState.properties[BlockProperties.FACING].unsafeCast<Directions>().horizontalId

        for ((index, item) in items.withIndex()) {
            item ?: continue
            if (!random.chance(20)) {
                continue
            }
            val direction = Directions.byHorizontal(Math.floorMod(index + facing, Directions.SIDES.size))

            val position = Vec3d(blockPosition) + Vec3d(
                0.5f - direction.vector.x * DIRECTION_OFFSET + direction.rotateYC().vector.x * DIRECTION_OFFSET,
                0.5f,
                0.5f - direction.vector.z * DIRECTION_OFFSET + direction.rotateYC().vector.z * DIRECTION_OFFSET,
            )

            for (i in 0 until 4) {
                connection.world.addParticle(SmokeParticle(connection, position, Vec3d(0.0, 5.0E-4, 0.0)))
            }
        }
    }

    companion object : BlockEntityFactory<CampfireBlockEntity> {
        override val identifier: ResourceLocation = KUtil.minecraft("campfire")
        const val DIRECTION_OFFSET = 0.3125


        override fun build(connection: PlayConnection): CampfireBlockEntity {
            return CampfireBlockEntity(connection)
        }
    }
}
