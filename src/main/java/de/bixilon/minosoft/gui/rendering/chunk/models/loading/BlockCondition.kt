/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger, Lukas Eisenhauer
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.chunk.models.loading

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.mappings.blocks.Block
import de.bixilon.minosoft.data.mappings.blocks.BlockProperties
import de.bixilon.minosoft.data.mappings.blocks.BlockRotations

open class BlockCondition {
    private var properties: MutableSet<BlockProperties> = mutableSetOf()
    private var rotation: BlockRotations = BlockRotations.NONE

    constructor(json: JsonObject) {
        for ((key, value) in json.entrySet()) {
            val value = value.asString
            kotlin.run {
                BlockRotations.ROTATION_MAPPING[value]?.let {
                    rotation = it
                    return@run
                }

                BlockProperties.PROPERTIES_MAPPING[key]?.get(value)?.let {
                    properties.add(it)
                }
            }
        }
    }

    constructor()

    open operator fun contains(block: Block): Boolean {
        return if (rotation != BlockRotations.NONE && rotation != block.rotation) {
            false
        } else {
            block.properties.containsAll(properties)
        }
    }

    companion object {
        val TRUE_CONDITION: BlockCondition = object : BlockCondition() {
            override fun contains(block: Block): Boolean {
                return true
            }
        }
    }
}
