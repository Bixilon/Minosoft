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

package de.bixilon.minosoft.gui.rendering.models.raw.block

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.json.JsonUtil.asJsonObject
import de.bixilon.kutil.json.JsonUtil.toJsonList
import de.bixilon.kutil.json.JsonUtil.toJsonObject
import de.bixilon.kutil.primitive.BooleanUtil.toBoolean
import de.bixilon.minosoft.gui.rendering.models.raw.block.element.ModelElement
import de.bixilon.minosoft.gui.rendering.models.raw.display.DisplayPositions
import de.bixilon.minosoft.gui.rendering.models.raw.display.ModelDisplay
import de.bixilon.minosoft.gui.rendering.models.raw.light.GUILights
import java.util.*

data class BlockModel(
    val guiLight: GUILights,
    val display: Map<DisplayPositions, ModelDisplay>?,
    val elements: List<ModelElement>?,
    val textures: Map<String, String>?,
    val ambientOcclusion: Boolean,
) {

    companion object {

        private fun display(data: JsonObject, parent: Map<DisplayPositions, ModelDisplay>?): Map<DisplayPositions, ModelDisplay>? {
            if (data.isEmpty()) return parent

            val display: MutableMap<DisplayPositions, ModelDisplay> = parent?.toMutableMap() ?: EnumMap(DisplayPositions::class.java)

            for ((key, value) in data) {
                display[DisplayPositions[key]] = ModelDisplay.deserialize(value.asJsonObject())
            }

            return display
        }


        private fun elements(data: List<JsonObject>): List<ModelElement>? {
            if (data.isEmpty()) return null

            val elements: MutableList<ModelElement> = mutableListOf()

            for (entry in data) {
                elements += ModelElement.deserialize(entry) ?: continue
            }

            if (elements.isEmpty()) return emptyList()

            return elements
        }

        private fun textures(data: JsonObject, parent: Map<String, String>?): Map<String, String>? {
            if (data.isEmpty()) return parent

            val textures: MutableMap<String, String> = parent?.toMutableMap() ?: mutableMapOf()

            for ((name, value) in data) {
                textures[name] = value.toString()
            }

            return textures
        }

        fun deserialize(parent: BlockModel?, data: JsonObject): BlockModel {
            val guiLight = data["gui_light"]?.let { GUILights[it] } ?: parent?.guiLight ?: GUILights.SIDE
            val display = data["display"]?.toJsonObject()?.let { display(it, parent?.display) } ?: parent?.display
            val elements = data["elements"]?.toJsonList()?.let { elements(it.unsafeCast()) } ?: parent?.elements
            val textures = data["textures"]?.toJsonObject()?.let { textures(it, parent?.textures) } ?: parent?.textures
            val ambientOcclusion = data["ambientocclusion"]?.toBoolean() ?: parent?.ambientOcclusion ?: true


            return BlockModel(guiLight, display, elements, textures, ambientOcclusion)
        }
    }
}
