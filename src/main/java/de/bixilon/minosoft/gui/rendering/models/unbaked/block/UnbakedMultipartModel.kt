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

import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.models.baked.MultipartBakedModel
import de.bixilon.minosoft.gui.rendering.models.baked.block.BakedBlockModel
import de.bixilon.minosoft.gui.rendering.models.properties.AbstractFaceProperties
import de.bixilon.minosoft.gui.rendering.models.unbaked.AbstractUnbakedBlockModel
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.EMPTY
import java.util.*

class UnbakedMultipartModel(
    val models: Set<AbstractUnbakedBlockModel>,
) : AbstractUnbakedBlockModel {
    override val textures: Map<String, String> = emptyMap()

    override fun bake(context: RenderContext): BakedBlockModel {
        val baked: Array<BakedBlockModel?> = arrayOfNulls(this.models.size)
        val sizes: Array<MutableList<AbstractFaceProperties>> = Array(Directions.SIZE) { mutableListOf() }
        var particleTexture: AbstractTexture? = null

        for ((index, model) in this.models.withIndex()) {
            val bakedModel = model.bake(context)
            if (particleTexture == null) {
                val modelParticleTexture = bakedModel.getParticleTexture(RANDOM, Vec3i.EMPTY)
                if (modelParticleTexture != null) {
                    particleTexture = modelParticleTexture
                }
            }
            for (direction in Directions.VALUES) {
                sizes[direction.ordinal] += bakedModel.getTouchingFaceProperties(RANDOM, direction) ?: continue // There is no random here!
            }
            baked[index] = bakedModel
        }
        val finalFaces: Array<Array<AbstractFaceProperties>?> = arrayOfNulls(Directions.SIZE)
        for (index in 0 until Directions.SIZE) {
            val faces = sizes[index]
            if (faces.isEmpty()) {
                continue
            }
            finalFaces[index] = faces.toTypedArray()
        }


        return MultipartBakedModel(baked.unsafeCast(), finalFaces, particleTexture)
    }

    private companion object {
        private val RANDOM = Random(0L)
    }
}
