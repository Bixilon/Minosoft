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

package de.bixilon.minosoft.data.registries.registries.registry

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.exception.Broken
import de.bixilon.kutil.json.JsonObject
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.air.AirBlock
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap

class BlockStateRegistry(var flattened: Boolean) : AbstractRegistry<BlockState?> {
    override var parent: AbstractRegistry<BlockState?>? = null
    private val idMap: Int2ObjectOpenHashMap<BlockState> = Int2ObjectOpenHashMap()

    override val size: Int
        get() {
            val value = idMap.size
            parent?.let {
                return value + it.size
            }
            return value
        }

    override fun noParentIterator(): Iterator<BlockState?> {
        return idMap.values.iterator()
    }

    override fun clear() {
        idMap.clear()
    }

    override fun get(any: Any?): BlockState? {
        return when (any) {
            is Int -> getOrNull(any)
            else -> TODO("Not yet implemented")
        }
    }

    internal operator fun set(id: Int, state: BlockState) {
        idMap[id] = state
    }

    private fun _get(id: Int): BlockState? {
        return idMap[id] ?: parent.unsafeCast<BlockStateRegistry?>()?._get(id)
    }

    fun forceGet(id: Int): BlockState? {
        val state = _get(id)
        if (state != null) return state
        if (flattened) return null

        return _get((id shr 4) shl 4) // Remove meta and try again
    }

    @Deprecated("Use getOrNull", ReplaceWith("getOrNull(id)"))
    override fun get(id: Int): BlockState? {
        return getOrNull(id)
    }

    @Suppress("DEPRECATION")
    override fun getOrNull(id: Int): BlockState? {
        if (flattened) {
            if (id == ProtocolDefinition.AIR_BLOCK_ID) return null
        } else {
            if (id shr 4 == ProtocolDefinition.AIR_BLOCK_ID) return null
        }
        val state = forceGet(id) ?: return null
        if (state.block is AirBlock) {
            return null
        }
        return state
    }

    override fun getId(value: BlockState?): Int {
        TODO("Not yet implemented")
    }

    override fun addItem(resourceLocation: ResourceLocation, id: Int?, data: JsonObject, registries: Registries?) = Broken()

    override fun optimize() {
        idMap.trim()
    }
}
