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

package de.bixilon.minosoft.gui.rendering.models.block.element

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.json.JsonUtil.toJsonObject
import de.bixilon.kutil.primitive.BooleanUtil.toBoolean
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.gui.rendering.models.block.BlockModel
import de.bixilon.minosoft.gui.rendering.models.block.element.face.ModelFace
import de.bixilon.minosoft.gui.rendering.models.util.CuboidUtil
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureManager
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.toVec3

data class ModelElement(
    val from: Vec3,
    val to: Vec3,
    val faces: Map<Directions, ModelFace>,
    val shade: Boolean = true,
    val rotation: ElementRotation? = null,
) {

    fun positions(direction: Directions): FaceVertexData {
        val positions = CuboidUtil.positions(direction, from, to)
        rotation?.apply(positions)

        return positions
    }

    fun load(model: BlockModel, textures: TextureManager) {
        for ((_, face) in faces) {
            face.load(model, textures)
        }
    }

    companion object {
        const val BLOCK_SIZE = 16.0f


        fun deserialize(data: JsonObject): ModelElement? {
            val faces = ModelFace.deserialize(data["faces"].unsafeCast()) ?: return null
            val from = data["from"].toVec3() / BLOCK_SIZE
            val to = data["to"].toVec3() / BLOCK_SIZE

            val shade = data["shade"]?.toBoolean() ?: true
            val rotation = data["rotation"]?.toJsonObject()?.let { ElementRotation.deserialize(it) }



            return ModelElement(from, to, faces, shade, rotation)
        }
    }
}
