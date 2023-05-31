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

import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.state.builder.BlockStateSettings
import de.bixilon.minosoft.data.registries.blocks.state.error.StatelessBlockError
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.models.baked.block.BakedBlockModel

open class BlockState(
    val block: Block,
    val luminance: Int,
) {
    var blockModel: BakedBlockModel? = null

    constructor(block: Block, settings: BlockStateSettings) : this(block, settings.luminance)


    override fun hashCode(): Int {
        return block.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other is ResourceLocation) return other == block.identifier
        if (other is BlockState) return other.block == block && other.luminance == luminance
        return false
    }

    override fun toString(): String {
        return block.toString()
    }

    open fun withProperties(vararg properties: Pair<BlockProperties, Any>): BlockState {
        if (properties.isEmpty()) return this
        throw StatelessBlockError(this)
    }

    open fun withProperties(properties: Map<BlockProperties, Any>): BlockState {
        if (properties.isEmpty()) return this
        throw StatelessBlockError(this)
    }

    open fun cycle(property: BlockProperties): BlockState = throw StatelessBlockError(this)

    open operator fun <T> get(property: BlockProperties): T = throw StatelessBlockError(this)
}
