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

package de.bixilon.minosoft.gui.rendering.block.models

import com.google.gson.JsonObject
import glm_.glm
import glm_.vec3.Vec3

open class BlockModel(val parent: BlockModel? = null, json: JsonObject) {
    val textures: MutableMap<String, String> = parent?.textures?.toMutableMap() ?: mutableMapOf()
    var elements: MutableList<BlockModelElement> = parent?.elements?.toMutableList() ?: mutableListOf()
    var rotation: Vec3
    private var uvLock = false // ToDo
    private var rescale = false // ToDo

    init {
        json["textures"]?.asJsonObject?.let {
            for ((type, value) in it.entrySet()) {
                textures[type] = value.asString
            }
        }
        for ((type, texture) in textures) {
            getTextureByType(texture).let {
                textures[type] = it
            }
        }
        json["elements"]?.let { it ->
            elements.clear()
            for (element in it.asJsonArray) {
                val blockModelElement = BlockModelElement(element.asJsonObject)
                elements.add(blockModelElement)
            }
        }
        var rotateX = parent?.rotation?.x ?: 0.0f
        var rotateY = parent?.rotation?.y ?: 0.0f
        var rotateZ = parent?.rotation?.z ?: 0.0f
        json["x"]?.let {
            rotateX = it.asFloat
        }
        json["y"]?.let {
            rotateY = it.asFloat
        }
        json["z"]?.let {
            rotateZ = it.asFloat
        }
        json["uvlock"]?.let {
            uvLock = it.asBoolean
        }
        json["rescale"]?.let {
            rescale = it.asBoolean
        }
        rotation = glm.radians(Vec3(rotateX, rotateY, rotateZ))
    }

    private fun getTextureByType(type: String): String {
        var currentValue: String = type
        while (currentValue.startsWith("#")) {
            textures[currentValue.removePrefix("#")].let {
                if (it == null) {
                    return currentValue
                }
                currentValue = it
            }
        }
        return currentValue
    }
}
