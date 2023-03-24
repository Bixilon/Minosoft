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

import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.primitive.BooleanUtil.toBoolean
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.gui.rendering.models.block.BlockModel
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.BakedFace
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.BakedModel
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.BakingUtil.positions
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.SideSize
import de.bixilon.minosoft.gui.rendering.models.loader.BlockLoader
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureManager
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import java.util.*

data class SingleBlockStateApply(
    val model: BlockModel,
    val uvLock: Boolean = false,
    val weight: Int = 1,
    val x: Int = 0,
    val y: Int = 0,
) : BlockStateApply {


    override fun bake(textures: TextureManager): BakedModel? {
        if (model.elements == null) return null

        val bakedFaces: MutableMap<Directions, MutableList<BakedFace>> = EnumMap(Directions::class.java) // TODO: use array
        val sizes: MutableMap<Directions, MutableList<SideSize.FaceSize>> = EnumMap(Directions::class.java)

        for (element in model.elements) {
            for ((direction, face) in element.faces) {
                val texture = face.createTexture(model, textures)


                val positions = positions(direction, element.from, element.to)
            }
        }
        TODO()
    }


    companion object {
        fun deserialize(model: BlockModel, data: JsonObject): SingleBlockStateApply {
            val uvLock = data["uvlock"]?.toBoolean() ?: false
            val weight = data["weight"]?.toInt() ?: 1
            val x = data["x"]?.toInt() ?: 0
            val y = data["y"]?.toInt() ?: 0

            return SingleBlockStateApply(model, uvLock, weight, x, y)
        }

        fun deserialize(loader: BlockLoader, data: JsonObject): SingleBlockStateApply {
            val model = loader.loadBlock(data["model"].toString().toResourceLocation())

            return deserialize(model, data)
        }
    }
}
