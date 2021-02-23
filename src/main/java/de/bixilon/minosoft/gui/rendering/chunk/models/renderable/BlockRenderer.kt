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

package de.bixilon.minosoft.gui.rendering.chunk.models.renderable

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.Directions
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.data.world.BlockInfo
import de.bixilon.minosoft.gui.rendering.chunk.models.loading.BlockModel
import de.bixilon.minosoft.gui.rendering.textures.Texture
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3

class BlockRenderer(data: JsonObject, parent: BlockModel) {
    private val transparentFaces: MutableSet<Directions> = mutableSetOf()
    private val cullFaces: MutableSet<Directions> = mutableSetOf()
    val textures: MutableMap<String, String> = mutableMapOf()
    private val fullFaceDirections: MutableSet<Directions> = mutableSetOf()
    private val elements: MutableSet<ElementRenderer> = mutableSetOf()
    private val textureMapping: MutableMap<String, Texture> = mutableMapOf()

    init {
        val newElements = ElementRenderer.createElements(data, parent)
        this.elements.addAll(newElements)
        textures.putAll(parent.textures)
    }


    fun resolveTextures(indexed: MutableList<Texture>, textureMap: MutableMap<String, Texture>) {
        for ((key, textureName) in textures) {
            if (!textureName.startsWith("#")) {
                var texture: Texture? = null
                var index: Int? = textureMap[textureName]?.let {
                    texture = it
                    indexed.indexOf(it)
                }
                if (index == null || index == -1) {
                    index = textureMap.size
                    texture = Texture(textureName, index)
                    textureMap[textureName] = texture!!
                    indexed.add(texture!!)
                }
                textureMapping[key] = texture!!
            }
        }
        for (direction in Directions.DIRECTIONS) {
            for (element in elements) {
                if (element.isCullFace(direction)) {
                    cullFaces.add(direction)
                }
                if (textureMapping[element.getTexture(direction)]?.isTransparent == true) { // THIS IS BROKEN!
                    transparentFaces.add(direction)
                }
                if (element.isFullTowards(direction)) {
                    fullFaceDirections.add(direction)
                }
            }
        }
    }

    fun render(blockInfo: BlockInfo, position: Vec3, data: MutableList<Float>, neighbourBlocks: Array<BlockInfo?>) {
        val modelMatrix = Mat4().translate(position)

        for (direction in Directions.DIRECTIONS) {
            for (element in elements) {
                val cullFace = cullFaces.contains(direction)

                var neighbourBlockFullFace = false
                neighbourBlocks[direction.ordinal]?.block?.renders?.let { // ToDo: Improve this
                    val testDirection = direction.inverse()
                    for (model in it) {
                        if (model.fullFaceDirections.contains(testDirection) && !model.transparentFaces.contains(testDirection)) {
                            neighbourBlockFullFace = true
                            break
                        }
                    }
                }
                if (neighbourBlockFullFace && cullFace) {
                    continue
                }

                val tintColor: RGBColor? = blockInfo.block.tintColor

                element.render(tintColor, textureMapping, modelMatrix, direction, data)
            }
        }
    }
}
