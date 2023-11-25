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

package de.bixilon.minosoft.gui.rendering.models.block

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.json.JsonUtil.asJsonObject
import de.bixilon.kutil.json.JsonUtil.toJsonList
import de.bixilon.kutil.json.JsonUtil.toJsonObject
import de.bixilon.kutil.primitive.BooleanUtil.toBoolean
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.models.block.element.ModelElement
import de.bixilon.minosoft.gui.rendering.models.raw.display.DisplayPositions
import de.bixilon.minosoft.gui.rendering.models.raw.display.ModelDisplay
import de.bixilon.minosoft.gui.rendering.models.raw.light.GUILights
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureManager
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.util.*

data class BlockModel(
    val guiLight: GUILights = GUILights.SIDE,
    val display: Map<DisplayPositions, ModelDisplay>? = null,
    val elements: List<ModelElement>?,
    val textures: Map<String, Any>?, // either String or ResourceLocation
    val ambientOcclusion: Boolean = true,
) {
    val loadedTextures: HashMap<String, Texture> = hashMapOf()

    fun createTexture(name: String, textures: TextureManager): Texture? {
        if (!name.startsWith("#")) {
            return textures.staticTextures.create(name.toResourceLocation())
        }
        val texture = this.textures?.get(name.substring(1))
        if (texture == null || texture !is ResourceLocation) {
            return null
        }

        return textures.staticTextures.create(texture)
    }

    fun getOrNullTexture(name: String, textures: TextureManager): Texture? {
        this.loadedTextures[name]?.let { return it }

        val texture = createTexture(name, textures) ?: return null

        this.loadedTextures[name] = texture
        return texture
    }

    fun getTexture(name: String, textures: TextureManager): Texture {
        val texture = getOrNullTexture(name, textures)
        if (texture == null) {
            Log.log(LogMessageType.LOADING, LogLevels.WARN) { "Can not find mapped texture ${name}, please check for broken resource packs!" }
            return textures.debugTexture
        }

        return texture
    }

    fun getTexture(name: String): Texture? {
        return this.loadedTextures[name]
    }

    companion object {

        fun display(data: JsonObject, parent: Map<DisplayPositions, ModelDisplay>?): Map<DisplayPositions, ModelDisplay>? {
            if (data.isEmpty()) return parent

            val display: MutableMap<DisplayPositions, ModelDisplay> = parent?.toMutableMap() ?: EnumMap(DisplayPositions::class.java)

            for ((key, value) in data) {
                display[DisplayPositions[key]] = ModelDisplay.deserialize(value.asJsonObject())
            }

            return display
        }


        private fun elements(data: List<JsonObject>): List<ModelElement>? {
            if (data.isEmpty()) return null

            val elements: MutableList<ModelElement> = ArrayList(data.size)

            for (entry in data.reversed()) {
                elements += ModelElement.deserialize(entry) ?: continue
            }

            if (elements.isEmpty()) return emptyList()

            return elements
        }

        fun textures(data: JsonObject, parent: Map<String, Any>?): Map<String, Any>? {
            if (data.isEmpty()) return parent

            val textures: MutableMap<String, Any> = parent?.toMutableMap() ?: mutableMapOf()

            for ((name, value) in data) {
                val string = value.toString()
                if (!string.startsWith('#')) {
                    // not a variable
                    textures[name] = string.toResourceLocation().texture()
                } else {
                    textures[name] = string.substring(1)
                }
            }

            return textures.resolveTextures()
        }

        private fun Map<String, Any>.resolve(value: Any, output: MutableMap<String, Any>): Any {
            if (value !is String) return value // texture identifier
            val resolved = this[value] ?: return value
            output[value] = resolved // cache result, even if not needed
            return resolved
        }

        private fun Map<String, Any>.resolveTextures(): Map<String, Any> {
            if (size <= 1) return this // if it has just one element, we can not resolve anything

            val output: MutableMap<String, Any> = mutableMapOf()

            for ((entry, value) in this) {
                output[entry] = resolve(value, output)
            }

            return output
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
