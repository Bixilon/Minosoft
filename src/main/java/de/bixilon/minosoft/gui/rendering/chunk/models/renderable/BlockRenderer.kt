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
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.light.LightAccessor
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.chunk.ChunkMeshCollection
import de.bixilon.minosoft.gui.rendering.chunk.models.FaceSize
import de.bixilon.minosoft.gui.rendering.chunk.models.loading.BlockModel
import de.bixilon.minosoft.gui.rendering.textures.Texture
import de.bixilon.minosoft.gui.rendering.textures.TextureTransparencies
import de.bixilon.minosoft.gui.rendering.util.VecUtil.plus
import de.bixilon.minosoft.gui.rendering.util.VecUtil.toVec3
import glm_.Java
import glm_.vec3.Vec3
import glm_.vec3.Vec3i

class BlockRenderer : BlockRenderInterface {
    private val cullFaces: Array<Directions?> = arrayOfNulls(Directions.DIRECTIONS.size)
    val textures: MutableMap<String, String> = mutableMapOf()
    private val elements: MutableSet<ElementRenderer> = mutableSetOf()
    private val textureMapping: MutableMap<String, Texture> = mutableMapOf()
    override val faceBorderSizes: Array<Array<FaceSize>?> = arrayOfNulls(Directions.DIRECTIONS.size)
    override val transparentFaces: BooleanArray = BooleanArray(Directions.DIRECTIONS.size)
    val directionMapping: HashBiMap<Directions, Directions> = HashBiMap.create()

    constructor(data: JsonObject, parent: BlockModel) {
        val rotation = Java.glm.radians(data.toVec3())
        createDirectionMapping(rotation)
        val newElements = ElementRenderer.createElements(data, parent, rotation, directionMapping)
        // reverse drawing order (for e.g. grass block side overlays
        this.elements.addAll(newElements.reversed())
        textures.putAll(parent.textures)
    }

    private fun createDirectionMapping(rotation: Vec3) {
        for (direction in Directions.DIRECTIONS) {
            try {
                directionMapping[direction] = ElementRenderer.getRotatedDirection(rotation, direction)
            } catch (_: IllegalArgumentException) {
            }
        }
    }

    constructor(data: List<JsonObject>, models: Map<ResourceLocation, BlockModel>) {
        for (state in data) {
            val rotation = Java.glm.radians(state.toVec3())
            createDirectionMapping(rotation)
            val parent = models[ResourceLocation(state["model"].asString)]!!
            val newElements = ElementRenderer.createElements(state, parent, rotation, directionMapping)
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
            val faceBorderSites: MutableList<FaceSize> = mutableListOf()
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

    override fun render(blockState: BlockState, lightAccessor: LightAccessor, tintColor: RGBColor?, blockPosition: Vec3i, meshCollection: ChunkMeshCollection, neighbourBlocks: Array<BlockState?>, world: World) {
        if (!RenderConstants.RENDER_BLOCKS) {
            return
        }
        for (direction in Directions.DIRECTIONS) {
            val rotatedDirection = directionMapping[direction]!!
            val invertedDirection = direction.inversed
            var isNeighbourTransparent = false
            var neighbourFaceSize: Array<FaceSize>? = null
            val neighbourBlock = neighbourBlocks[direction.ordinal]
            neighbourBlock?.getBlockRenderer(blockPosition + direction)?.let {
                val itDirection = if (it is BlockRenderer) {
                    it.directionMapping[invertedDirection]!!
                } else {
                    invertedDirection
                }

                if (it.transparentFaces[itDirection.ordinal]) {
                    isNeighbourTransparent = true
                }
                neighbourFaceSize = it.faceBorderSizes[itDirection.ordinal]
            }

            // ToDo: Should we preserve the cullface attribute? It seems to has no point here.

            for (element in elements) {
                var drawElementFace = true

                neighbourFaceSize?.let {
                    val elementFaceBorderSize = element.faceBorderSize[rotatedDirection.ordinal] ?: return@let
                    for (size in it) {
                        if (elementFaceBorderSize.start.x < size.start.x || elementFaceBorderSize.start.y < size.start.y) {
                            return@let
                        }
                        if (elementFaceBorderSize.end.x > size.end.x || elementFaceBorderSize.end.y > size.end.y) {
                            return@let
                        }
                    }
                    drawElementFace = false
                }

                if (!drawElementFace) {
                    // force draw transparent faces
                    if (isNeighbourTransparent && !transparentFaces[direction.ordinal]) {
                        drawElementFace = true
                    } else if (isNeighbourTransparent && transparentFaces[direction.ordinal] && neighbourBlock != blockState) {
                        drawElementFace = true
                    }
                }

                if (!drawElementFace) {
                    continue
                }

                element.render(tintColor, blockPosition, lightAccessor, textureMapping, direction, meshCollection)
            }
        }
    }
}
