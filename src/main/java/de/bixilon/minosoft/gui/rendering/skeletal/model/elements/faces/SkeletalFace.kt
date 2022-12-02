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

package de.bixilon.minosoft.gui.rendering.skeletal.model.elements.faces

import de.bixilon.kotlinglm.GLM
import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec4.Vec4
import de.bixilon.kotlinglm.vec4.swizzle.xy
import de.bixilon.kotlinglm.vec4.swizzle.zw
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.gui.rendering.models.unbaked.ModelBakeUtil
import de.bixilon.minosoft.gui.rendering.skeletal.SkeletalVertexConsumer
import de.bixilon.minosoft.gui.rendering.skeletal.baked.BakedSkeletalModel.Companion.fromBlockCoordinates
import de.bixilon.minosoft.gui.rendering.skeletal.model.SkeletalModel
import de.bixilon.minosoft.gui.rendering.skeletal.model.elements.SkeletalElement
import de.bixilon.minosoft.gui.rendering.system.base.MeshUtil.buffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.ShaderTexture
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.rotateAssign
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap

data class SkeletalFace(
    val uv: Vec4,
    val texture: Int,
    val transparency: Boolean = true,
) {
    val uvStart = uv.xy
    val uvEnd = uv.zw


    fun bake(model: SkeletalModel, element: SkeletalElement, direction: Directions, inflate: Float, outlinerId: Int, textures: Int2ObjectOpenHashMap<ShaderTexture>, consumer: SkeletalVertexConsumer) {
        val positions = direction.getPositions(element.from.fromBlockCoordinates() - inflate, element.to.fromBlockCoordinates() + inflate)

        val uvDivider = Vec2(model.resolution.width, model.resolution.height)
        val texturePositions = ModelBakeUtil.getTextureCoordinates(uvStart / uvDivider, uvEnd / uvDivider)

        val origin = element.origin.fromBlockCoordinates()

        element.rotation.let {
            val rad = -GLM.radians(it)
            for ((index, position) in positions.withIndex()) {
                val out = Vec3(position)
                out.rotateAssign(rad[0], Axes.X, origin, element.rescale)
                out.rotateAssign(rad[1], Axes.Y, origin, element.rescale)
                out.rotateAssign(rad[2], Axes.Z, origin, element.rescale)
                positions[index] = out
            }
        }

        var flags = 0
        if (element.transparency && transparency) {
            flags = flags or 0x01
        }

        val texture = textures[texture]!!
        val transform = outlinerId.buffer()
        val textureShaderId = texture.shaderId.buffer()
        val floatFlags = flags.buffer()

        for ((index, textureIndex) in consumer.order) {
            val indexPosition = positions[index].array
            val transformedUV = texture.transformUV(texturePositions[textureIndex])
            consumer.addVertex(indexPosition, transformedUV, transform, textureShaderId, floatFlags)
        }
    }
}
