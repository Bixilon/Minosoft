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

package de.bixilon.minosoft.gui.rendering.models.block.state.variant

import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.state.PropertyBlockState
import de.bixilon.minosoft.gui.rendering.models.block.state.apply.BlockStateApply

data class PropertyVariantBlockModel(
    val variants: Map<BlockVariant, BlockStateApply>,
) : VariantBlockModel {

    private fun BlockVariant.matches(state: PropertyBlockState): Boolean {
        for ((property, value) in this) {
            val stateProperty = state.properties[property] ?: return false
            if (stateProperty == value) continue

            return false
        }

        return true
    }

    override fun choose(state: BlockState): BlockStateApply? {
        if (state !is PropertyBlockState) return null

        for ((variant, apply) in this.variants) {
            if (!variant.matches(state)) continue

            return apply
        }
        return null
    }
}
