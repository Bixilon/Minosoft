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
import de.bixilon.minosoft.gui.rendering.models.baked.block.BakedBlockModel
import de.bixilon.minosoft.gui.rendering.models.baked.block.BakedBlockStateModel
import de.bixilon.minosoft.gui.rendering.models.baked.block.BakedFace
import de.bixilon.minosoft.gui.rendering.models.unbaked.GenericUnbakedModel
import de.bixilon.minosoft.gui.rendering.models.unbaked.UnbakedBlockModel
import de.bixilon.minosoft.gui.rendering.models.unbaked.UnbakedModel
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.toVec2iN
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.get
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.set
import de.bixilon.minosoft.util.KUtil.toBoolean
import de.bixilon.minosoft.util.KUtil.toInt
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.KUtil.unsafeCast
import glm_.func.rad
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import glm_.vec4.swizzle.xyz
import glm_.vec4.swizzle.xz

data class UnbakedBlockStateModel(
    val model: UnbakedBlockModel,
    val rotation: Vec2i?,
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


        val faces: Array<MutableList<BakedFace>> = Array(Directions.SIZE) { mutableListOf() }

        for (element in model.elements) {
            val rescale = element.rotation?.rescale ?: false
            for (face in element.faces) {
                val texture = resolvedTextures[face.texture.removePrefix("#")]!! // ToDo: Allow direct texture names?
                val positions = face.direction.getPositions(element.from, element.to)
                val rotationMatrix = Mat4()
                element.rotation?.let {
                    rotationMatrix.rotateAssign(it.angle.rad, Vec3.EMPTY.apply { this[it.axis] = 1.0f })
                }
                rotation?.let {
                    rotationMatrix.rotateAssign(-rotation.y.rad, Vec3(0.0f, 1.0f, 0.0f))
                    rotationMatrix.rotateAssign(-rotation.x.rad, Vec3(1.0f, 0.0f, 0.0f))
                }

                val direction = Directions.byDirection((rotationMatrix * Vec4(face.direction.vectorf, 1.0f)).xyz)

                for ((index, position) in positions.withIndex()) {
                    positions[index] = (rotationMatrix * Vec4(position - 0.5f, 1.0f)).xyz + 0.5f
                }

                val texturePositions = arrayOf(
                    Vec2(face.uvEnd.x, face.uvStart.y),
                    face.uvStart,
                    Vec2(face.uvStart.x, face.uvEnd.y),
                    face.uvEnd,
                ).rotateLeft((face.rotation % 360) / 90).toTypedArray()

                if (this.uvLock && this.rotation != null) {
                    val matrix = Mat4()
                    //matrix.rotateAssign(this.rotation.x.rad, Vec3(1,0,0))
                    val rotationVec3 = Vec3(this.rotation, 0.0f)
                    val angle = rotationVec3[face.direction.axis]
                    matrix.rotateAssign(-angle.rad, direction.vectorf)
                    //matrix.rotateAssign(this.rotation.x.rad, Vec3(0,1,0))
                    for ((index, position) in texturePositions.withIndex()) {
                        texturePositions[index] = (matrix * Vec4(position.x - 0.5f, 0.0f, position.y - 0.5f, 0.0f)).xz + 0.5f
                    }
                }


                faces[direction.ordinal] += BakedFace(
                    faceSize = face.direction.getSize(element.from, element.to),
                    positions = positions,
                    uv = texturePositions,
                    shade = element.shade,
                    tintIndex = face.tintIndex,
                    cullFace = face.cullFace,
                    texture = texture,
                )
            }
        }

        val finalFaces: Array<Array<BakedFace>?> = arrayOfNulls(faces.size)

        for ((index, faceArray) in faces.withIndex()) {
            finalFaces[index] = faceArray.toTypedArray()
        }

        return BakedBlockStateModel(finalFaces.unsafeCast())
    }

    companion object {
        operator fun invoke(models: Map<ResourceLocation, GenericUnbakedModel>, data: Map<String, Any>): UnbakedBlockStateModel {
            return UnbakedBlockStateModel(
                model = models[data["model"].toResourceLocation()].unsafeCast(),
                rotation = data.toVec2iN(),
                uvLock = data["uvlock"]?.toBoolean() ?: false,
                weight = data["weight"]?.toInt() ?: 1,
            )
        }

        fun <T> Array<T>.rotateLeft(n: Int) = drop(n) + take(n)
        fun <T> Array<T>.rotateRight(n: Int) = takeLast(n) + dropLast(n)
    }
}
