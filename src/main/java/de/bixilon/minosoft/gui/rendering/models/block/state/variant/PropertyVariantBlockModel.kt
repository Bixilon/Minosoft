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

import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperty
import de.bixilon.minosoft.gui.rendering.models.block.BlockModelPrototype
import de.bixilon.minosoft.gui.rendering.models.block.state.apply.BlockStateApply
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureManager

data class PropertyVariantBlockModel(
    val variants: Map<BlockVariant, BlockStateApply>,
) : VariantBlockModel {

    private fun BlockVariant.matches(properties: Map<BlockProperty<*>, Any>): Boolean {
        for ((property, value) in this) {
            val stateProperty = properties[property] ?: return false
            if (stateProperty == value) continue

            return false
        }

        return true
    }

    override fun choose(properties: Map<BlockProperty<*>, Any>): BlockStateApply? {
        if (properties.isEmpty()) return null

        for ((variant, apply) in this.variants) {
            if (!variant.matches(properties)) continue

            return apply
        }
        return null
    }

    override fun load(textures: TextureManager): BlockModelPrototype {
        for ((_, variant) in variants) {
            variant.load(textures)
        }
        return super.load(textures)
    }
}
