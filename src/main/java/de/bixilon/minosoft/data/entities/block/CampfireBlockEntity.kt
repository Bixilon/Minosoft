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

package de.bixilon.minosoft.data.entities.block

import glm_.vec3.Vec3d
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.exception.Broken
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.kutil.random.RandomUtil.chance
import de.bixilon.minosoft.data.container.ItemStackUtil
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.direction.DirectionUtil.rotateY
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties.getFacing
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties.isLit
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.entity.CampfireBlock
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.fire.SmokeParticle
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.invoke
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.listCast
import java.util.*

class CampfireBlockEntity(session: PlaySession) : BlockEntity(session) {
    val items: Array<ItemStack?> = arrayOfNulls(CampfireBlock.MAX_ITEMS)


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
            val stack = ItemStackUtil.of(
                item = session.registries.item[slot["id"].unsafeCast<String>()]!!,
                session = session,
                count = slot["Count"]?.toInt() ?: 1,
            )

            items[slot["Slot"]!!.toInt()] = stack
        }
    }


    override fun tick(session: PlaySession, state: BlockState, position: BlockPosition, random: Random) {
        val particle = session.world.particle ?: return
        if (state.block !is CampfireBlock || !state.isLit()) {
            return
        }

        if (random.nextFloat() < 0.11f) {
            for (i in 0 until random.nextInt(2) + 2) {
                state.block.spawnSmokeParticles(session, state, position, false, random)
            }
        }

        val facing = state.getFacing().campfireId

        for ((index, item) in items.withIndex()) {
            item ?: continue
            if (!random.chance(20)) {
                continue
            }
            val direction = HORIZONTAL[Math.floorMod(index + facing, Directions.SIDES.size)]

            val position = Vec3d(position) + Vec3d(
                0.5f - direction.vector.x * DIRECTION_OFFSET + direction.rotateY().vector.x * DIRECTION_OFFSET,
                0.5f,
                0.5f - direction.vector.z * DIRECTION_OFFSET + direction.rotateY().vector.z * DIRECTION_OFFSET,
            )

            for (i in 0 until 4) {
                particle += SmokeParticle(session, position, Vec3d(0.0, 5.0E-4, 0.0))
            }
        }
    }

    companion object : BlockEntityFactory<CampfireBlockEntity> {
        override val identifier: ResourceLocation = minecraft("campfire")
        private val HORIZONTAL = arrayOf(Directions.SOUTH, Directions.WEST, Directions.NORTH, Directions.EAST)
        const val DIRECTION_OFFSET = 0.3125


        private val Directions.campfireId:Int get() = when(this) {
            Directions.NORTH -> 2
            Directions.SOUTH -> 0
            Directions.WEST -> 1
            Directions.EAST -> 3
            else -> Broken()
        }


        override fun build(session: PlaySession): CampfireBlockEntity {
            return CampfireBlockEntity(session)
        }
    }
}
