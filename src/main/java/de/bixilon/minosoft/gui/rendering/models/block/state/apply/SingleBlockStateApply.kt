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

package de.bixilon.minosoft.gui.rendering.models.block.state.apply

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kutil.exception.Broken
import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.primitive.BooleanUtil.toBoolean
import de.bixilon.kutil.primitive.FloatUtil.toFloat
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.direction.DirectionUtil.rotateX
import de.bixilon.minosoft.data.direction.DirectionUtil.rotateY
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.gui.rendering.models.block.BlockModel
import de.bixilon.minosoft.gui.rendering.models.block.element.ElementRotation
import de.bixilon.minosoft.gui.rendering.models.block.element.FaceVertexData
import de.bixilon.minosoft.gui.rendering.models.block.element.ModelElement
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.BakedFace
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.BakedModel
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.BakingUtil.compact
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.BakingUtil.compactProperties
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.BakingUtil.pushRight
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.cull.side.FaceProperties
import de.bixilon.minosoft.gui.rendering.models.loader.BlockLoader
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureManager
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.util.KUtil.toResourceLocation

data class SingleBlockStateApply(
    val model: BlockModel,
    val uvLock: Boolean = false,
    val x: Int = 0,
    val y: Int = 0,
    val rotation: Vec2? = null,
) : BlockStateApply {
    private var particle: Texture? = null

    private fun FaceVertexData.rotateX(count: Int) {
        for (c in 0 until count) {
            for (i in 0 until 4) {
                val offset = i * 3
                val y = this[offset + 1]
                val z = this[offset + 2]

                this[offset + 1] = z
                this[offset + 2] = -y + 1.0f
            }
        }
    }

    private fun Directions.xRotations(): Int {
        if (axis == Axes.X) {
            return if (negative) -x else x
        }

        if (axis == Axes.Y && (x == 2 || x == 3)) {
            return if (negative) -1 else 1
        } else if (axis == Axes.Z && (x == 1 || x == 2)) {
            return if (negative) 1 else -1
        }

        return 0
    }

    private fun FaceVertexData.rotateX(direction: Directions): FaceVertexData {
        if (x == 0) return this
        rotateX(x)
        return pushRight(3, direction.xRotations())
    }


    private fun FaceVertexData.rotateY(count: Int) {
        for (c in 0 until count) {
            for (i in 0 until 4) {
                val offset = i * 3
                val x = this[offset + 0]
                val z = this[offset + 2]

                this[offset + 0] = -z + 1.0f // translates to origin and back; same as -(z-0.5f) + 0.5f
                this[offset + 2] = x
            }
        }
    }

    private fun Directions.yRotations(): Int {
        if (axis == Axes.Y) {
            return if (negative) -y else y
        }
        if ((axis == Axes.Z && (y == 2 || y == 3))) {
            return if (negative) -1 else 1
        } else if (axis == Axes.X && (y == 1 || y == 2)) {
            return if (negative) 1 else -1
        }

        return 0
    }

    private fun FaceVertexData.rotateY(direction: Directions): FaceVertexData {
        if (y == 0) return this
        rotateY(y)
        return pushRight(3, direction.yRotations())
    }

    private fun rotatedY(direction: Directions): Int {
        if (direction.axis != Axes.Y) return 0
        return if (direction.negative) -y else y
    }

    private fun rotatedX(direction: Directions, rotated: Directions): Int {
        if (direction.axis == Axes.X) {
            return if (direction.negative) -x else x
        }
        if (direction == Directions.NORTH || rotated == Directions.NORTH) return 2
        return 0
    }

    private fun getTextureRotation(direction: Directions, rotated: Directions): Int {
        if (x == 0 && y == 0) return 0

        if (x == 0) {
            return rotatedY(direction)
        }
        if (y == 0) {
            return rotatedX(direction, rotated)
        }
        return rotatedX(direction, direction.rotateX(x)) + rotatedY(direction.rotateX(x))
    }

    override fun load(textures: TextureManager) {
        if (model.elements == null) return
        particle = model.getOrNullTexture("#particle", textures)

        for (element in model.elements) {
            element.load(model, textures)
        }
    }


    override fun bake(): BakedModel? {
        if (model.elements == null) return null

        val bakedFaces: Array<MutableList<BakedFace>> = Array(Directions.SIZE) { mutableListOf() }
        val properties: Array<MutableList<FaceProperties>> = Array(Directions.SIZE) { mutableListOf() }

        for (element in model.elements) {
            element.bake(bakedFaces, properties)
        }

        return BakedModel(bakedFaces.compact(), properties.compactProperties(), this.particle)
    }

    private fun Vec2.applyRotation(axis: Axes, data: FaceVertexData) {
        val value = this[axis.ordinal]
        if (value == 0.0f) return
        ElementRotation(axis = axis, angle = value).apply(data)
    }

    private fun ModelElement.bake(faces: Array<MutableList<BakedFace>>, properties: Array<MutableList<FaceProperties>>) {
        for ((direction, face) in this.faces) {
            val texture = model.getTexture(face.texture) ?: continue

            val rotatedDirection = direction
                .rotateX(x)
                .rotateY(y)


            var positions = positions(direction)
                .rotateX(direction)

            this@SingleBlockStateApply.rotation?.applyRotation(Axes.X, positions)
            positions = positions.rotateY(direction.rotateX(x))
            this@SingleBlockStateApply.rotation?.applyRotation(Axes.Y, positions)


            var uv = face.getUV(uvLock, from, to, direction, rotatedDirection, positions, x, y).toArray(rotatedDirection, face.rotation)

            if (!uvLock) {
                val rotation = getTextureRotation(direction, rotatedDirection)
                uv = uv.pushRight(2, rotation)
            }
            val shade = rotatedDirection.shade

            val faceProperties = if (rotation == null && this@SingleBlockStateApply.rotation == null) positions.properties(rotatedDirection, texture) else null
            val bakedFace = BakedFace(positions, uv, shade, face.tintIndex, if (faceProperties == null) null else rotatedDirection, texture, faceProperties)

            faces[rotatedDirection.ordinal] += bakedFace
            properties[rotatedDirection.ordinal] += faceProperties ?: continue
        }
    }

    fun FaceVertexData.properties(direction: Directions, texture: Texture): FaceProperties? {
        // TODO: Bad code?

        val axis = direction.axis.ordinal
        val value = this[axis]
        if ((direction.negative && value != 0.0f) || (!direction.negative && value != 1.0f)) return null

        return FaceProperties(
            start = getVec2(0, axis),
            end = getVec2(6, axis),
            transparency = texture.transparency,
        )
    }

    private fun FaceVertexData.getVec2(offset: Int, axis: Int): Vec2 {
        return when (axis) {
            0 -> Vec2(this[offset + 1], this[offset + 2])
            1 -> Vec2(this[offset + 0], this[offset + 2])
            2 -> Vec2(this[offset + 0], this[offset + 1])
            else -> Broken()
        }
    }

    companion object {
        const val ROTATION_STEP = 90

        fun Int.rotation(): Int {
            var rotation = this

            rotation /= ROTATION_STEP
            return rotation and 0x03
        }

        fun deserialize(model: BlockModel, data: JsonObject): SingleBlockStateApply {
            val uvLock = data["uvlock"]?.toBoolean() ?: false

            val x = data["x"]?.toFloat() ?: 0.0f
            val y = data["y"]?.toFloat() ?: 0.0f

            val rotation = Vec2(x % ROTATION_STEP, y % ROTATION_STEP)

            return SingleBlockStateApply(model, uvLock, x.toInt().rotation(), y.toInt().rotation(), rotation = if (rotation.x == 0.0f && rotation.y == 0.0f) null else rotation)
        }

        fun deserialize(loader: BlockLoader, data: JsonObject): SingleBlockStateApply? {
            val model = loader.loadBlock(data["model"].toString().toResourceLocation()) ?: return null

            return deserialize(model, data)
        }

        val Directions.shade: Float
            get() = when (this) {
                Directions.UP -> 1.0f
                Directions.DOWN -> 0.5f
                Directions.NORTH, Directions.SOUTH -> 0.8f
                Directions.WEST, Directions.EAST -> 0.6f
            }
    }
}
