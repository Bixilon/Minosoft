/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.mappings.blocks

import de.bixilon.minosoft.data.mappings.ModIdentifier
import de.bixilon.minosoft.data.world.BlockPosition
import de.bixilon.minosoft.gui.rendering.chunk.models.BlockModel
import java.util.*
import kotlin.random.Random

class Block : ModIdentifier {
    var rotation: BlockRotations = BlockRotations.NONE
    var properties: Set<BlockProperties> = setOf()
    val blockModels: MutableList<BlockModel> = mutableListOf()

    constructor(mod: String, identifier: String, properties: HashSet<BlockProperties>, rotation: BlockRotations) : super(mod, identifier) {
        this.properties = properties
        this.rotation = rotation
    }

    constructor(mod: String, identifier: String, properties: HashSet<BlockProperties>) : super(mod, identifier) {
        this.properties = properties
    }

    constructor(fullIdentifier: String, vararg properties: BlockProperties) : super(fullIdentifier) {
        this.properties = setOf(*properties)
    }

    constructor(mod: String, identifier: String, rotation: BlockRotations) : super(mod, identifier) {
        this.rotation = rotation
    }

    constructor(mod: String, identifier: String) : super(mod, identifier)

    constructor(fullIdentifier: String?) : super(fullIdentifier)

    override fun hashCode(): Int {
        return Objects.hash(mod, identifier, properties, rotation)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null) {
            return false
        }
        if (hashCode() != other.hashCode()) {
            return false
        }
        if (other is Block) {
            return identifier == other.identifier && rotation == other.rotation && properties == other.properties && mod == other.mod
        }
        if (other is ModIdentifier) {
            return super.equals(other)
        }
        return false
    }

    fun bareEquals(obj: Any): Boolean {
        if (this === obj) {
            return true
        }
        if (obj is Block) {
            if (getMod() != obj.getMod() || getIdentifier() != obj.getIdentifier()) {
                return false
            }
            if (obj.rotation != BlockRotations.NONE) {
                if (obj.rotation != rotation) {
                    return false
                }
            }
            for (property in obj.properties) {
                if (!properties.contains(property)) {
                    return false
                }
            }
            return true
        }
        return if (obj is ModIdentifier) {
            super.equals(obj)
        } else false
    }

    override fun toString(): String {
        val out = StringBuilder()
        if (rotation != BlockRotations.NONE) {
            out.append(" (")
            out.append("rotation=")
            out.append(rotation)
        }
        if (properties.isNotEmpty()) {
            if (out.isNotEmpty()) {
                out.append(", ")
            } else {
                out.append(" (")
            }
            out.append("properties=")
            out.append(properties)
        }
        if (out.isNotEmpty()) {
            out.append(")")
        }
        return String.format("%s%s", getFullIdentifier(), out)
    }

    fun getBlockModel(position: BlockPosition): BlockModel {
        // ToDo: Support weight attribute
        return blockModels.random(Random(position.hashCode()))
    }
}
