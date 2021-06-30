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
import de.bixilon.minosoft.gui.rendering.util.VecUtil.rad
import glm_.vec3.Vec3

open class BlockModel(
    val parent: BlockModel? = null,
    data: JsonObject,
) {
    val textures: Map<String, String>
    val elements: List<BlockModelElement>
    val rotation: Vec3 = Vec3(data["x"]?.asFloat ?: parent?.rotation?.x ?: 0.0f, data["y"]?.asFloat ?: parent?.rotation?.y ?: 0.0f, data["z"]?.asFloat ?: parent?.rotation?.z ?: 0.0f).rad
    val uvLock: Boolean = data["uvlock"]?.asBoolean ?: parent?.uvLock ?: false
    val rescale: Boolean = data["rescale"]?.asBoolean ?: parent?.rescale ?: false
    val ambientOcclusion: Boolean = data["ambientocclusion"]?.asBoolean ?: parent?.ambientOcclusion ?: true

    init {
        textures = data["textures"]?.asJsonObject?.let {
            val textures: MutableMap<String, String> = parent?.textures?.toMutableMap() ?: mutableMapOf()
            for ((type, value) in it.entrySet()) {
                textures[type] = value.asString
            }
            for ((type, texture) in textures) {
                textures[type] = getTextureByType(textures, texture)
            }
            textures.toMap()
        } ?: parent?.textures ?: mapOf()


        elements = data["elements"]?.asJsonArray?.let {
            val elements: MutableList<BlockModelElement> = mutableListOf()
            for (element in it) {
                elements += BlockModelElement(element.asJsonObject)
            }
            elements.toList()
        } ?: parent?.elements ?: listOf()
    }

    private fun getTextureByType(textures: Map<String, String>, type: String): String {
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
