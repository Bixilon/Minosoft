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
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperty
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.gui.rendering.models.block.state.DirectBlockModel
import de.bixilon.minosoft.gui.rendering.models.block.state.apply.BlockStateApply
import de.bixilon.minosoft.gui.rendering.models.loader.BlockLoader
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

interface VariantBlockModel : DirectBlockModel {

    companion object {

        private fun parseVariant(block: Block, variant: String): BlockVariant {
            val properties: MutableMap<BlockProperty<*>, Any> = mutableMapOf()
            for (pair in variant.split(',')) {
                val equals = pair.indexOf('=')
                if (equals <= 0) continue

                val (key, rawValue) = pair.split('=', limit = 2)

                try {
                    val (property, value) = BlockProperties.parseProperty(block, key, rawValue)
                    properties[property] = value
                } catch (error: Throwable) {
                    Log.log(LogMessageType.LOADING, LogLevels.WARN) { error }
                }
            }

            return properties
        }

        fun deserialize(loader: BlockLoader, block: Block, data: JsonObject): VariantBlockModel? {
            if (data.isEmpty()) return null

            val variants: MutableMap<BlockVariant, BlockStateApply> = linkedMapOf()


            for ((variant, entry) in data) {
                val apply = BlockStateApply.deserialize(loader, entry) ?: continue
                if (data.size == 1 && (variant == "" || variant == "normal")) {
                    // no further conditions
                    return SingleVariantBlockModel(apply)
                }
                variants[parseVariant(block, variant)] = apply
            }

            if (variants.isEmpty()) return null

            return PropertyVariantBlockModel(variants)
        }
    }
}
