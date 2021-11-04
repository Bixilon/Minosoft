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

package de.bixilon.minosoft.gui.rendering.models.unbaked.block

import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.models.FaceSize
import de.bixilon.minosoft.gui.rendering.models.baked.block.BakedBlockModel
import de.bixilon.minosoft.gui.rendering.models.baked.block.BakedBlockStateModel
import de.bixilon.minosoft.gui.rendering.models.baked.block.BakedFace
import de.bixilon.minosoft.gui.rendering.models.unbaked.GenericUnbakedModel
import de.bixilon.minosoft.gui.rendering.models.unbaked.UnbakedBlockModel
import de.bixilon.minosoft.gui.rendering.models.unbaked.UnbakedModel
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.toVec3iN
import de.bixilon.minosoft.util.KUtil.toBoolean
import de.bixilon.minosoft.util.KUtil.toInt
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.KUtil.unsafeCast
import glm_.vec2.Vec2
import glm_.vec3.Vec3i

data class UnbakedBlockStateModel(
    val model: UnbakedBlockModel,
    val rotation: Vec3i?,
    val uvLock: Boolean,
    val weight: Int,
) : UnbakedModel {

    override fun bake(renderWindow: RenderWindow): BakedBlockModel {
        val textureArray = renderWindow.textureManager.staticTextures

        val resolvedTextures: MutableMap<String, AbstractTexture> = mutableMapOf()


        fun resolveTexture(key: String, value: String): AbstractTexture {
            resolvedTextures[key]?.let { return it }

            val variable = value.removePrefix("#")
            var texture: AbstractTexture? = null
            if (variable.length != value.length) {
                // resolve variable first
                texture = resolveTexture(variable, model.textures[variable]!!)
            }

            if (texture == null) {
                texture = textureArray.createTexture(value.toResourceLocation().texture())
            }

            resolvedTextures[key] = texture
            return texture
        }


        for ((key, value) in model.textures) {
            resolveTexture(key, value)
        }


        val faces: Array<MutableList<BakedFace>> = Array(Directions.VALUES.size) { mutableListOf() }
        val sizes: Array<MutableList<FaceSize>> = Array(Directions.VALUES.size) { mutableListOf() }

        for (element in model.elements) {
            for (face in element.faces) {
                val texture = resolvedTextures[face.texture.removePrefix("#")]!! // ToDo: Allow direct texture names?
                val positions = face.direction.getPositions(element.from, element.to)

                val texturePositions = arrayOf(
                    face.uvStart,
                    Vec2(face.uvStart.x, face.uvEnd.y),
                    Vec2(face.uvEnd.x, face.uvStart.y),
                    face.uvEnd,
                )

                faces[face.direction.ordinal] += BakedFace(
                    positions = positions,
                    uv = texturePositions,
                    shade = element.shade,
                    tintIndex = face.tintIndex,
                    cullFace = face.cullFace,
                    texture = texture,
                )
            }
        }

        val finalFaces: Array<Array<BakedFace>?> = Array(faces.size) { null }

        for ((index, faceArray) in faces.withIndex()) {
            finalFaces[index] = faceArray.toTypedArray()
        }

        val finalSizes: Array<Array<FaceSize>?> = Array(sizes.size) { null }

        for ((index, sizeArray) in sizes.withIndex()) {
            finalSizes[index] = sizeArray.toTypedArray()
        }

        return BakedBlockStateModel(finalFaces.unsafeCast(), finalSizes.unsafeCast())
    }

    companion object {
        operator fun invoke(models: Map<ResourceLocation, GenericUnbakedModel>, data: Map<String, Any>): UnbakedBlockStateModel {
            return UnbakedBlockStateModel(
                model = models[data["model"].toResourceLocation()].unsafeCast(),
                rotation = data.toVec3iN(),
                uvLock = data["uvlock"]?.toBoolean() ?: false,
                weight = data["weight"]?.toInt() ?: 1,
            )
        }
    }
}
