/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.skeletal.model.outliner

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.TextNode
import com.fasterxml.jackson.module.kotlin.convertValue
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY
import de.bixilon.minosoft.util.json.Jackson
import java.util.*

class SkeletalOutliner(
    val name: String,
    val origin: Vec3 = Vec3.EMPTY,
    val uuid: UUID,
    children: List<JsonNode>,
) {
    val children: List<Any> // List<UUID | SkeletalOutliner>

    init {
        val _children: MutableList<Any> = mutableListOf()

        for (child in children) {
            _children += if (child is TextNode) {
                Jackson.MAPPER.convertValue<UUID>(child)
            } else {
                Jackson.MAPPER.convertValue<SkeletalOutliner>(child)
            }
        }

        this.children = _children
    }
}
