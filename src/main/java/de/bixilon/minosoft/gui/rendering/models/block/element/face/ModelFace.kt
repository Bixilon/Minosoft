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

package de.bixilon.minosoft.gui.rendering.models.block.element.face

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.models.block.BlockModel
import de.bixilon.minosoft.gui.rendering.models.block.element.ModelElement.Companion.BLOCK_SIZE
import de.bixilon.minosoft.gui.rendering.models.block.state.apply.SingleBlockStateApply.Companion.rotation
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureManager
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.tint.TintManager
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.listCast
import java.util.*

data class ModelFace(
    val texture: String,
    val uv: FaceUV?,
    val rotation: Int,
    val tintIndex: Int = -1,
) {
    var loadedTexture: Texture? = null

    private fun createTexture(model: BlockModel, manager: TextureManager): Texture {
        if (!this.texture.startsWith("#")) {
            return manager.staticTextures.createTexture(texture.toResourceLocation())
        }
        val name = model.textures?.get(this.texture.substring(1))
        if (name == null || name !is ResourceLocation) {
            Log.log(LogMessageType.LOADING, LogLevels.WARN) { "Can not find mapped texture ${this.texture}, please check for broken resource packs!" }
            return manager.debugTexture
        }
        return manager.staticTextures.createTexture(name)
    }

    fun load(model: BlockModel, manager: TextureManager) {
        this.loadedTexture = createTexture(model, manager)
    }

    companion object {

        fun deserialize(direction: Directions, from: Vec3, to: Vec3, data: JsonObject): ModelFace {
            val texture = data["texture"].toString()

            val uv = data["uv"]?.listCast<Number>()?.let {
                // auto transform (flip) y coordinate (in minosoft 0|0 is left up, not like in minecraft/opengl where it is left down)
                FaceUV(
                    start = Vec2(it[0], it[3].toFloat()) / BLOCK_SIZE,
                    end = Vec2(it[2], it[1].toFloat()) / BLOCK_SIZE,
                )
            }

            val rotation = data["rotation"]?.toInt()?.rotation() ?: 0
            val tintIndex = data["tintindex"]?.toInt() ?: TintManager.DEFAULT_TINT_INDEX

            return ModelFace(texture, uv, rotation, tintIndex)
        }

        fun deserialize(from: Vec3, to: Vec3, data: Map<String, JsonObject>): Map<Directions, ModelFace>? {
            val map: MutableMap<Directions, ModelFace> = EnumMap(Directions::class.java)

            for ((key, value) in data) {
                val direction = Directions[key]
                val face = deserialize(direction, from, to, value)

                map[direction] = face
            }

            if (map.isEmpty()) return null

            return map
        }
    }
}
