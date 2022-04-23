/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
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

import de.bixilon.kotlinglm.func.rad
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec3.swizzle.xz
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.primitive.BooleanUtil.toBoolean
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.models.ModelLoader
import de.bixilon.minosoft.gui.rendering.models.baked.block.BakedBlockModel
import de.bixilon.minosoft.gui.rendering.models.baked.block.BakedBlockStateModel
import de.bixilon.minosoft.gui.rendering.models.baked.block.BakedFace
import de.bixilon.minosoft.gui.rendering.models.properties.AbstractFaceProperties
import de.bixilon.minosoft.gui.rendering.models.unbaked.AbstractUnbakedBlockModel
import de.bixilon.minosoft.gui.rendering.models.unbaked.ModelBakeUtil
import de.bixilon.minosoft.gui.rendering.models.unbaked.UnbakedBlockModel
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.get
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.rad
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.toVec2iN
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.get
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.rotateAssign
import de.bixilon.minosoft.util.KUtil.toResourceLocation

data class UnbakedBlockStateModel(
    val model: UnbakedBlockModel,
    val rotation: Vec2i?,
    val uvLock: Boolean,
    val weight: Int,
) : AbstractUnbakedBlockModel {
    var baked: BakedBlockModel? = null
    override val textures: Map<String, String>
        get() = model.textures

    @Synchronized
    override fun bake(renderWindow: RenderWindow): BakedBlockModel {
        baked?.let { return it }

        val textures = TextureUtil.resolveTextures(renderWindow.textureManager.staticTextures, model.textures)

        val particleTexture = textures["particle"]


        val faces: Array<MutableList<BakedFace>> = Array(Directions.SIZE) { mutableListOf() }
        val touchingFaceProperties: Array<MutableList<AbstractFaceProperties>> = Array(Directions.SIZE) { mutableListOf() }

        for (element in model.elements) {
            for (face in element.faces) {
                val texture = textures[face.texture.removePrefix("#")] ?: throw IllegalArgumentException("Can not find texture variable ${face.texture}")// ToDo: Allow direct texture names?
                val positions = face.direction.getPositions(element.from, element.to)

                element.rotation?.let {
                    val rad = it.angle.rad
                    for ((index, position) in positions.withIndex()) {
                        positions[index] = Vec3(position).apply { rotateAssign(rad, it.axis, it.origin, it.rescale) }
                    }
                }

                var direction = face.direction
                var cullFace = face.cullFace
                rotation?.let {
                    val rad = it.rad

                    direction = Directions.byDirection(Vec3(face.direction.vectorf).apply { rotateAssign(rad) })
                    cullFace = face.cullFace?.vectorf?.let { cullFace -> Directions.byDirection(Vec3(cullFace).apply { rotateAssign(rad) }) }

                    for ((index, position) in positions.withIndex()) {
                        positions[index] = Vec3(position).apply { rotateAssign(rad, true) }
                    }
                }

                var texturePositions = ModelBakeUtil.getTextureCoordinates(face.uvStart, face.uvEnd)
                if (face.rotation != 0) {
                    texturePositions = texturePositions.rotateLeft((face.rotation % 360) / 90).toTypedArray()
                }

                if (this.uvLock && this.rotation != null) {
                    // ToDo: Not working correct: Take a look at the (top) texture of vines
                    val axis = when (face.direction) {
                        Directions.UP, Directions.DOWN -> Axes.Y
                        else -> Axes.X
                    }
                    val rad = this.rotation[axis].rad
                    for ((index, position) in texturePositions.withIndex()) {
                        texturePositions[index] = (Vec3(position.x - 0.5f, 0.0f, position.y - 0.5f).apply { rotateAssign(rad, Axes.Y) }).xz + 0.5f
                    }
                }

                val (sizeStart, sizeEnd) = face.direction.getSize(element.from, element.to)

                var touching = true
                rotation?.let {
                    if (it.x % 90 != 0 || it.y % 90 != 0) {
                        touching = false
                    }
                }

                touching = touching && (if (face.direction.negative) element.from[face.direction.axis] else element.to[face.direction.axis] - 1.0f) == 0.0f

                var shade = 1.0f
                if (element.shade) {
                    shade = when (direction) {
                        Directions.DOWN -> 0.5f
                        Directions.UP -> 1.0f
                        Directions.NORTH, Directions.SOUTH -> 0.8f
                        Directions.WEST, Directions.EAST -> 0.6f
                    }
                }
                val bakedFace = BakedFace(
                    sizeStart = sizeStart,
                    sizeEnd = sizeEnd,
                    positions = positions,
                    uv = texturePositions,
                    shade = shade,
                    tintIndex = face.tintIndex,
                    cullFace = cullFace,
                    texture = texture,
                    touching = touching,
                )

                faces[direction.ordinal] += bakedFace
                if (touching) {
                    touchingFaceProperties[direction.ordinal] += bakedFace
                }
            }
        }

        val finalFaces: Array<Array<BakedFace>?> = arrayOfNulls(faces.size)
        for ((index, faceArray) in faces.withIndex()) {
            finalFaces[index] = faceArray.toTypedArray()
        }

        val finalTouchingProperties: Array<Array<AbstractFaceProperties>?> = arrayOfNulls(faces.size)
        for ((index, sizeArray) in touchingFaceProperties.withIndex()) {
            if (sizeArray.isEmpty()) {
                continue
            }
            finalTouchingProperties[index] = sizeArray.toTypedArray()
        }

        val baked = BakedBlockStateModel(finalFaces.unsafeCast(), finalTouchingProperties, particleTexture)
        this.baked = baked
        return baked
    }

    companion object {
        operator fun invoke(modeLoader: ModelLoader, data: Map<String, Any>): UnbakedBlockStateModel {
            return UnbakedBlockStateModel(
                model = modeLoader.loadBlockModel(data["model"].toResourceLocation()).unsafeCast(),
                rotation = data.toVec2iN(),
                uvLock = data["uvlock"]?.toBoolean() ?: false,
                weight = data["weight"]?.toInt() ?: 1,
            )
        }

        fun <T> Array<T>.rotateLeft(n: Int) = drop(n) + take(n)
    }
}
