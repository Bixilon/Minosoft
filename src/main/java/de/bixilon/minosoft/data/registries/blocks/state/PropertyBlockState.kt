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

package de.bixilon.minosoft.data.registries.blocks.state

import com.google.common.base.Objects
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.state.builder.BlockStateSettings
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.util.KUtil.next

open class PropertyBlockState(
    block: Block,
    val properties: Map<BlockProperties, Any>,
    luminance: Int,
) : BlockState(block, luminance) {
    private val hash = Objects.hashCode(block, properties)

    constructor(block: Block, settings: BlockStateSettings) : this(block, settings.properties ?: emptyMap(), settings.luminance)


    override fun hashCode(): Int {
        return hash
    }

    override fun equals(other: Any?): Boolean {
        if (other is ResourceLocation) return other == block.identifier
        if (other is PropertyBlockState) return other.hash == hash && other.block == block && other.luminance == luminance && other.properties == this.properties

        return false
    }


    override fun withProperties(vararg properties: Pair<BlockProperties, Any>): BlockState {
        val nextProperties = this.properties.toMutableMap()

        for ((key, value) in properties) {
            nextProperties[key] = value
        }

        return getStateWith(nextProperties)
    }

    override fun withProperties(properties: Map<BlockProperties, Any>): BlockState {
        val nextProperties = this.properties.toMutableMap()

        for ((key, value) in properties) {
            nextProperties[key] = value
        }

        return getStateWith(nextProperties)
    }

    private fun getStateWith(properties: Map<BlockProperties, Any>): BlockState {
        for (state in this.block.states) {
            if (state !is PropertyBlockState) continue

            if (state.properties != properties) {
                continue
            }

            return state
        }

        throw IllegalArgumentException("Can not find ${this.block} with properties: $properties")
    }

    override fun cycle(property: BlockProperties): BlockState {
        val value: Any = this[property]
        return withProperties(property to block.properties[property]!!.next(value))
    }

    override fun <T> get(property: BlockProperties): T {
        val value = this.properties[property] ?: throw IllegalArgumentException("$this has not property $property")
        return value.unsafeCast()
    }


    fun withProperties(): BaseComponent {
        val component = BaseComponent()
        var first = true
        for ((property, value) in properties) {
            if (first) {
                first = false
            } else {
                component += "\n"
            }
            component += property
            component += ": "
            component += value
        }

        return component
    }
}
