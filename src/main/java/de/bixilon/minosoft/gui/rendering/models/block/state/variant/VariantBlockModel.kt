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

import de.bixilon.kutil.json.JsonObject
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.gui.rendering.models.block.state.DirectBlockModel
import de.bixilon.minosoft.gui.rendering.models.block.state.apply.BlockStateApply
import de.bixilon.minosoft.gui.rendering.models.loader.BlockLoader

interface VariantBlockModel : DirectBlockModel {

    companion object {

        private fun parseVariant(variant: String): BlockVariant {
            val properties: MutableMap<BlockProperties, Any> = mutableMapOf()
            for (pair in variant.split(',')) {
                val (key, rawValue) = pair.split('=', limit = 2)

                val (property, value) = BlockProperties.parseProperty(key, rawValue)
                properties[property] = value
            }

            return properties
        }

        fun deserialize(loader: BlockLoader, data: JsonObject): VariantBlockModel? {
            if (data.isEmpty()) return null

            val variants: MutableMap<BlockVariant, BlockStateApply> = linkedMapOf()


            for ((variant, entry) in data) {
                val apply = BlockStateApply.deserialize(loader, entry) ?: continue
                if (variant == "" || variant == "normal") {
                    // no further conditions
                    return SingleVariantBlockModel(apply)
                }
                variants[parseVariant(variant)] = apply
            }

            if (variants.isEmpty()) return null

            return PropertyVariantBlockModel(variants)
        }
    }
}
