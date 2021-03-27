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
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.blocks.BlockState
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.data.world.BlockPosition
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.light.LightAccessor
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.chunk.ChunkMeshCollection
import de.bixilon.minosoft.gui.rendering.chunk.models.FaceSize
import de.bixilon.minosoft.gui.rendering.chunk.models.loading.BlockModel
import de.bixilon.minosoft.gui.rendering.textures.Texture
import de.bixilon.minosoft.gui.rendering.textures.TextureTransparencies

class BlockRenderer : BlockRenderInterface {
    private val cullFaces: Array<Directions?> = arrayOfNulls(Directions.DIRECTIONS.size)
    val textures: MutableMap<String, String> = mutableMapOf()
    private val elements: MutableSet<ElementRenderer> = mutableSetOf()
    private val textureMapping: MutableMap<String, Texture> = mutableMapOf()
    override val faceBorderSizes: Array<Array<FaceSize>?> = arrayOfNulls(Directions.DIRECTIONS.size)
    override val transparentFaces: BooleanArray = BooleanArray(Directions.DIRECTIONS.size)

    constructor(data: JsonObject, parent: BlockModel) {
        val newElements = ElementRenderer.createElements(data, parent)
        // reverse drawing order (for e.g. grass block side overlays
        this.elements.addAll(newElements.reversed())
        textures.putAll(parent.textures)
    }

    constructor(data: List<JsonObject>, models: Map<ResourceLocation, BlockModel>) {
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
            var directionIsCullFace: Boolean? = null
            var directionIsNotTransparent: Boolean? = null
            var faceBorderSites: MutableList<FaceSize> = mutableListOf()
            for (element in elements) {
                if (element.isCullFace(direction)) {
                    directionIsCullFace = true
                }
                if (textureMapping[element.getTexture(direction)]?.transparency != TextureTransparencies.OPAQUE) {
                    if (directionIsNotTransparent == null) {
                        directionIsNotTransparent = false
                    }
                } else {
                    directionIsNotTransparent = true
                }
                element.faceBorderSize[direction.ordinal]?.let {
                    faceBorderSites.add(it)
                }
            }

            if (directionIsCullFace == true) {
                cullFaces[direction.ordinal] = direction
            }
            if (directionIsNotTransparent == false) {
                transparentFaces[direction.ordinal] = true
            }
            if (faceBorderSites.isNotEmpty()) {
                faceBorderSizes[direction.ordinal] = faceBorderSites.toTypedArray()
            }
        }
    }

    override fun render(blockState: BlockState, lightAccessor: LightAccessor, tintColor: RGBColor?, position: BlockPosition, meshCollection: ChunkMeshCollection, neighbourBlocks: Array<BlockState?>, world: World) {
        if (!RenderConstants.RENDER_BLOCKS) {
            return
        }
        for (direction in Directions.DIRECTIONS) {
            val invertedDirection = direction.inverse
            var isNeighbourTransparent = false
            var neighbourFaceSize: Array<FaceSize>? = null
            neighbourBlocks[direction.ordinal]?.getBlockRenderer(position + direction)?.let {
                if (it.transparentFaces[invertedDirection.ordinal]) {
                    isNeighbourTransparent = true
                }
                neighbourFaceSize = it.faceBorderSizes[invertedDirection.ordinal]
            }

            // ToDo: Should we preserve the cullface attribute? It seems to has no point here.

            for (element in elements) {
                var drawElementFace = true


                neighbourFaceSize?.let {
                    // force draw transparent faces
                    if (transparentFaces[direction.ordinal] || isNeighbourTransparent) {
                        return@let
                    }

                    val elementFaceBorderSize = element.faceBorderSize[direction.ordinal] ?: return@let
                    for (size in it) {
                        if (elementFaceBorderSize.start.x < size.start.x || elementFaceBorderSize.start.y < size.start.y) {
                            return@let
                        }
                        if (elementFaceBorderSize.end.x > size.end.x || elementFaceBorderSize.end.y > size.end.y) {
                            return@let
                        }
                        drawElementFace = false
                        break
                    }
                }

                if (!drawElementFace) {
                    continue
                }

                element.render(tintColor, position, lightAccessor, textureMapping, direction, meshCollection)
            }
        }
    }
}
