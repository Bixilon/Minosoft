/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.models.unbaked

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.gui.rendering.models.display.ModelDisplay
import de.bixilon.minosoft.gui.rendering.models.display.ModelDisplayPositions
import de.bixilon.minosoft.gui.rendering.models.unbaked.element.UnbakedElement
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.compoundCast
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.listCast

abstract class GenericUnbakedModel(
    parent: GenericUnbakedModel?,
    json: Map<String, Any>,
) : UnbakedModel {
    val display: Map<ModelDisplayPositions, ModelDisplay>
    val elements: Set<UnbakedElement>


    init {
        val display = parent?.display?.toMutableMap() ?: mutableMapOf()

        json["display"]?.compoundCast()?.let {
            for ((name, value) in it) {
                display[ModelDisplayPositions[name]] = ModelDisplay(data = value.unsafeCast())
            }
        }

        this.display = display
    }

    val textures: Map<String, String>

    init {
        val textures = parent?.textures?.toMutableMap() ?: mutableMapOf()

        json["textures"]?.compoundCast()?.let {
            for ((name, value) in it) {
                textures[name] = value.toString()
            }
        }

        this.textures = textures
    }

    init {
        val elements: MutableSet<UnbakedElement>

        val elementJson = json["elements"]?.listCast<Map<String, Any>>()

        if (elementJson != null) {
            elements = mutableSetOf()
            for (element in elementJson) {
                elements += UnbakedElement(data = element)
            }
        } else {
            elements = parent?.elements?.toMutableSet() ?: mutableSetOf()
        }

        this.elements = elements
    }
}
