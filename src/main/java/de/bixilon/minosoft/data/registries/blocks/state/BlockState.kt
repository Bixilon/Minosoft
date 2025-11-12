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
package de.bixilon.minosoft.data.registries.blocks.state

import de.bixilon.kutil.array.ArrayUtil.next
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.enums.inline.enums.IntInlineEnumSet
import de.bixilon.minosoft.data.registries.blocks.light.LightProperties
import de.bixilon.minosoft.data.registries.blocks.light.OpaqueProperty
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperty
import de.bixilon.minosoft.data.registries.blocks.state.manager.PropertyStateManager
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.PixLyzerBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.collision.CollidableBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.outline.OutlinedBlock
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.data.registries.shapes.shape.Shape
import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.gui.rendering.models.block.state.render.BlockRender

class BlockState(
    val block: Block,
    val properties: Map<BlockProperty<*>, Any>,
    flags: IntInlineEnumSet<BlockStateFlags>,
    val luminance: Int = 0,
    @Deprecated("access only via block") internal val collisionShape: Shape? = null,
    @Deprecated("access only via block") internal val outlineShape: Shape? = null,
    @Deprecated("access only via block") internal val lightProperties: LightProperties? = null,
) {
    val flags = updateFlags(flags)
    private val hashCode = _hashCode()
    var model: BlockRender? = null

    private fun updateFlags(flags: IntInlineEnumSet<BlockStateFlags>): IntInlineEnumSet<BlockStateFlags> {
        var flags = flags

        if (BlockStateFlags.FULL_OPAQUE !in flags && block.getLightProperties(this) == OpaqueProperty) flags += BlockStateFlags.FULL_OPAQUE
        // TODO: we don't know if there is no collision or we need to provide more data
        // if (BlockStateFlags.FULL_COLLISION !in flags && BlockStateFlags.COLLISIONS in flags && block is CollidableBlock && block.getCollisionShape(this) == null) flags -= BlockStateFlags.COLLISIONS
        // if (BlockStateFlags.FULL_OUTLINE !in flags && BlockStateFlags.OUTLINE in flags && block is OutlinedBlock && block.getOutlineShape(this) == null) flags -= BlockStateFlags.OUTLINE

        if (BlockStateFlags.COLLISIONS in flags && BlockStateFlags.FULL_COLLISION !in flags && block is CollidableBlock && block.getCollisionShape(this) == AABB.BLOCK) flags += BlockStateFlags.FULL_COLLISION
        if (BlockStateFlags.OUTLINE in flags && BlockStateFlags.FULL_OUTLINE !in flags && block is OutlinedBlock && block.getOutlineShape(this) == AABB.BLOCK) flags += BlockStateFlags.FULL_OUTLINE

        if (block is PixLyzerBlock) {
            if (block.getCollisionShape(this) == null) {
                flags -= BlockStateFlags.COLLISIONS
                flags -= BlockStateFlags.FULL_COLLISION
            }
            if (block.getOutlineShape(this) == null) {
                flags -= BlockStateFlags.OUTLINE
                flags -= BlockStateFlags.FULL_OUTLINE
            }
        }

        return flags
    }

    private fun _hashCode(): Int {
        var result = 1
        result = 31 * result + block.hashCode()
        result = 31 * result + properties.hashCode()
        return result
    }

    override fun hashCode() = hashCode

    override fun equals(other: Any?) = when (other) {
        is BlockState -> hashCode == other.hashCode && other.block == block && other.luminance == luminance && properties == other.properties
        is Block -> block == other
        is ResourceLocation -> block.identifier == other
        else -> false
    }


    fun withProperties(vararg properties: Pair<BlockProperty<*>, Any>): BlockState {
        val nextProperties = this.properties.toMutableMap()

        for ((key, value) in properties) {
            nextProperties[key] = value
        }

        return getStateWith(nextProperties)
    }

    fun withProperties(properties: Map<BlockProperty<*>, Any>): BlockState {
        val nextProperties = this.properties.toMutableMap()

        for ((key, value) in properties) {
            nextProperties[key] = value
        }

        return getStateWith(nextProperties)
    }

    fun getStateWith(properties: Map<BlockProperty<*>, Any>): BlockState {
        for (state in this.block.states) {
            if (state.properties != properties) {
                continue
            }

            return state
        }

        throw IllegalArgumentException("Can not find ${this.block} with properties: $properties")
    }

    fun cycle(property: BlockProperty<*>): BlockState {
        val value: Any = this[property]!!
        return withProperties(property to block.states.unsafeCast<PropertyStateManager>().properties[property]!!.next(value))
    }

    operator fun <T> get(property: BlockProperty<T>): T {
        val value = this.properties[property] ?: throw IllegalArgumentException("$this has no property $property")
        return value.unsafeCast()
    }

    fun <T> getOrNull(property: BlockProperty<T>): T? {
        return this.properties[property]?.unsafeCast()
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

    override fun toString(): String {
        if (properties.isEmpty()) return block.toString()

        return block.toString() + "[${properties.map { "${it.key}=${it.value}" }.joinToString(",")}]"
    }
}
