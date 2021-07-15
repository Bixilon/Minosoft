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

package de.bixilon.minosoft.gui.rendering.block.renderable.block

import com.google.common.collect.HashBiMap
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.block.models.BlockModel
import de.bixilon.minosoft.gui.rendering.block.models.FaceSize
import de.bixilon.minosoft.gui.rendering.block.renderable.BlockLikeRenderContext
import de.bixilon.minosoft.gui.rendering.block.renderable.WorldEntryRenderer
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureManager
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureTransparencies
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import de.bixilon.minosoft.gui.rendering.textures.Texture
import de.bixilon.minosoft.gui.rendering.util.VecUtil.plus
import de.bixilon.minosoft.gui.rendering.util.VecUtil.rad
import de.bixilon.minosoft.gui.rendering.util.VecUtil.toVec3
import glm_.vec3.Vec3

class BlockRenderer(data: Map<String, Any>, model: BlockModel) : WorldEntryRenderer {
    private val cullFaces: Array<Directions?> = arrayOfNulls(Directions.VALUES.size)
    val textures: MutableMap<String, String> = mutableMapOf()
    private val elements: Array<ElementRenderer>
    val textureMapping: MutableMap<String, AbstractTexture> = mutableMapOf()
    override val faceBorderSizes: Array<Array<FaceSize>?> = arrayOfNulls(Directions.VALUES.size)
    override val transparentFaces: BooleanArray = BooleanArray(Directions.VALUES.size)
    val directionMapping: HashBiMap<Directions, Directions> = HashBiMap.create()

    init {
        val rotation = data.toVec3().rad
        createDirectionMapping(rotation)
        val newElements = ElementRenderer.createElements(data, model, rotation, directionMapping)
        this.elements = (newElements.reversed()).toTypedArray() // reverse drawing order (for e.g. grass block side overlays
        textures.putAll(model.textures)
    }

    private fun createDirectionMapping(rotation: Vec3) {
        for (direction in Directions.VALUES) {
            directionMapping[direction] = ElementRenderer.getRotatedDirection(rotation, direction)
        }
    }

    override fun resolveTextures(textureManager: TextureManager) {
        for ((key, textureName) in this.textures) {
            if (!textureName.startsWith("#")) {
                textureMapping[key] = WorldEntryRenderer.resolveTexture(textureManager, textureResourceLocation = Texture.getResourceTextureIdentifier(textureName = textureName))
            }
        }
    }

    override fun postInit() {
        for (direction in Directions.VALUES) {
            var directionIsCullFace: Boolean? = null
            var directionIsNotTransparent: Boolean? = null
            val faceBorderSites: MutableList<FaceSize> = mutableListOf()
            for (element in elements) {
                if (element.isCullFace(direction)) {
                    directionIsCullFace = true
                }
                element.faceBorderSize[direction.ordinal]?.let {
                    faceBorderSites.add(it)

                    if (textureMapping[element.getTexture(direction)]?.transparency != TextureTransparencies.OPAQUE) {
                        if (directionIsNotTransparent == null) {
                            directionIsNotTransparent = false
                        }
                    } else {
                        directionIsNotTransparent = true
                    }
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

    override fun render(context: BlockLikeRenderContext) {
        if (!RenderConstants.RENDER_BLOCKS) {
            return
        }
        var tintColor: RGBColor? = null
        var biome: Biome? = null

        for (direction in Directions.VALUES) {
            val rotatedDirection = directionMapping[direction] ?: direction
            val invertedDirection = direction.inverted
            var isNeighbourTransparent = false
            var neighbourFaceSize: Array<FaceSize>? = null
            val neighbourBlock = context.neighbourBlocks[direction.ordinal]
            neighbourBlock?.getBlockRenderer(context.blockPosition + direction)?.let {
                val itDirection = if (it is BlockRenderer) {
                    it.directionMapping[invertedDirection] ?: invertedDirection
                } else {
                    invertedDirection
                }

                if (it.transparentFaces[itDirection.ordinal]) {
                    isNeighbourTransparent = true
                }
                neighbourFaceSize = it.faceBorderSizes[itDirection.ordinal]
            }

            // ToDo: Should we preserve the cullface attribute? It seems to has no point here and only makes the results worse. Otherwise it could improve the performance...

            for (element in elements) {
                var drawElementFace = true

                neighbourFaceSize?.let {
                    val elementFaceBorderSize = element.faceBorderSize[rotatedDirection.ordinal] ?: return@let
                    for (size in it) {
                        if (size.start.x <= elementFaceBorderSize.start.x && size.start.y <= elementFaceBorderSize.start.y && size.end.x >= elementFaceBorderSize.end.x && size.end.y >= elementFaceBorderSize.end.y) {
                            drawElementFace = false
                            break
                        }
                    }
                }

                if (!drawElementFace) {
                    // force draw transparent faces
                    if (isNeighbourTransparent && !transparentFaces[direction.ordinal]) {
                        drawElementFace = true
                    } else if (isNeighbourTransparent && transparentFaces[direction.ordinal] && neighbourBlock != context.blockState) {
                        drawElementFace = true
                    }
                }

                if (!drawElementFace) {
                    continue
                }

                if (biome == null) {
                    biome = context.world.getBiome(context.blockPosition)
                    tintColor = context.renderWindow.tintColorCalculator.getAverageTint(biome, context.blockState, context.blockPosition)
                }
                element.render(tintColor, textureMapping, direction, context)
            }
        }
    }
}
