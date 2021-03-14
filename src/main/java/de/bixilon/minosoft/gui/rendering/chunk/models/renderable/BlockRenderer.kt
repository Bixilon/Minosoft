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

import com.google.common.collect.HashBiMap
import com.google.gson.JsonObject
import de.bixilon.minosoft.data.Directions
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.blocks.BlockState
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.data.world.BlockPosition
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.light.LightAccessor
import de.bixilon.minosoft.gui.rendering.chunk.SectionArrayMesh
import de.bixilon.minosoft.gui.rendering.chunk.models.loading.BlockModel
import de.bixilon.minosoft.gui.rendering.textures.Texture
import de.bixilon.minosoft.gui.rendering.textures.TextureTransparencies
import glm_.mat4x4.Mat4

class BlockRenderer: BlockRenderInterface {
    private val cullFaces: MutableSet<Directions> = mutableSetOf()
    val textures: MutableMap<String, String> = mutableMapOf()
    private val elements: MutableSet<ElementRenderer> = mutableSetOf()
    private val textureMapping: MutableMap<String, Texture> = mutableMapOf()
    override val fullFaceDirections: MutableSet<Directions> = mutableSetOf()
    override val transparentFaces: MutableSet<Directions> = mutableSetOf()

    constructor(data: JsonObject, parent: BlockModel) {
        val newElements = ElementRenderer.createElements(data, parent)
        // reverse drawing order (for e.g. grass block side overlays
        this.elements.addAll(newElements.reversed())
        textures.putAll(parent.textures)
    }

    constructor(data: List<JsonObject>, models: HashBiMap<ResourceLocation, BlockModel>) {
        for (state in data) {
            val parent = models[ResourceLocation(state["model"].asString)]!!
            val newElements = ElementRenderer.createElements(state, parent)
            this.elements.addAll(newElements)
            textures.putAll(parent.textures)
        }
    }

    override fun resolveTextures(indexed: MutableList<Texture>, textureMap: MutableMap<String, Texture>) {
        for ((key, textureName) in textures) {
            if (!textureName.startsWith("#")) {
                textureMapping[key] = BlockRenderInterface.resolveTexture(indexed, textureMap, textureName = textureName)!!
            }
        }
    }

    override fun postInit() {
        for (direction in Directions.DIRECTIONS) {
            var directionIsCullface: Boolean? = null
            var directionIsNotTransparent: Boolean? = null
            var directionIsFull: Boolean? = null
            for (element in elements) {
                if (element.isCullFace(direction)) {
                    directionIsCullface = true
                }
                if (textureMapping[element.getTexture(direction)]?.transparency != TextureTransparencies.OPAQUE) {
                    if (directionIsNotTransparent == null) {
                        directionIsNotTransparent = false
                    }
                } else {
                    directionIsNotTransparent = true
                }
                if (element.isFullTowards(direction)) {
                    directionIsFull = true
                }
            }

            if (directionIsCullface == true) {
                cullFaces.add(direction)
            }
            if (directionIsNotTransparent == false) {
                transparentFaces.add(direction)
            }
            if (directionIsFull == true) {
                fullFaceDirections.add(direction)
            }
        }
    }

    override fun render(blockState: BlockState, lightAccessor: LightAccessor, tintColor: RGBColor?, position: BlockPosition, mesh: SectionArrayMesh, neighbourBlocks: Array<BlockState?>, world: World) {
        val modelMatrix = Mat4().translate(position.toVec3())

        for (direction in Directions.DIRECTIONS) {
            for (element in elements) {
                val cullFace = cullFaces.contains(direction)

                var neighbourBlockFullFace = false
                neighbourBlocks[direction.ordinal]?.renders?.let { // ToDo: Improve this
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
                element.render(tintColor, position, lightAccessor, textureMapping, modelMatrix, direction, mesh)
            }
        }
    }
}
