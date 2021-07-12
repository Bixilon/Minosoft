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

package de.bixilon.minosoft.data.registries.registries.registry

import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.toSynchronizedMap

class BlockStateRegistry(var flattened: Boolean) : AbstractRegistry<BlockState> {
    override var parent: AbstractRegistry<BlockState>? = null
    private val idMap: MutableMap<Int, BlockState> = mutableMapOf()

    override val size: Int
        get() {
            val value = idMap.size
            parent?.let {
                return value + it.size
            }
            return value
        }

    override fun iterator(): Iterator<BlockState> {
        return idMap.toSynchronizedMap().values.iterator()
    }

    override fun clear() {
        idMap.clear()
    }

    override fun get(any: Any?): BlockState? {
        return when (any) {
            is Int -> get(any)
            else -> TODO("Not yet implemented")
        }
    }

    internal operator fun set(id: Int, state: BlockState) {
        idMap[id] = state
    }

    fun forceGet(id: Int): BlockState? {
        return idMap[id] ?: parent?.get(id) ?: let {
            if (flattened) {
                null
            } else {
                idMap[(id shr 4) shl 4] // Remove meta data and test again
            }
        }
    }

    override fun get(id: Int): BlockState? {
        if (id == ProtocolDefinition.NULL_BLOCK_ID) {
            return null
        }
        return forceGet(id)
    }

    override fun getId(value: BlockState): Int {
        TODO("Not yet implemented")
    }
}
