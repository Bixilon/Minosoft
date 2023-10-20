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

package de.bixilon.minosoft.gui.rendering.skeletal.model.elements

import de.bixilon.kotlinglm.GLM
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.models.block.element.ModelElement.Companion.BLOCK_SIZE
import de.bixilon.minosoft.gui.rendering.models.block.element.face.FaceUV
import de.bixilon.minosoft.gui.rendering.models.block.legacy.ModelBakeUtil
import de.bixilon.minosoft.gui.rendering.skeletal.baked.SkeletalBakeContext
import de.bixilon.minosoft.gui.rendering.system.base.MeshUtil.buffer
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.rotateAssign

data class SkeletalFace(
    val uv: FaceUV,
    val texture: ResourceLocation? = null,
) {

    fun bake(context: SkeletalBakeContext, direction: Directions, element: SkeletalElement, transform: Int) {
        val positions = direction.getPositions(context.offset + (element.from - context.inflate) / BLOCK_SIZE, context.offset + (element.to + context.inflate) / BLOCK_SIZE)

        val texture = context.textures[texture ?: context.texture ?: throw IllegalStateException("Element has no texture set!")] ?: throw IllegalStateException("Texture not found!")

        val texturePositions = ModelBakeUtil.getTextureCoordinates(uv.start / texture.properties.resolution, uv.end / texture.properties.resolution)


        for (rotation in context.rotations) {
            val origin = rotation.origin!! / BLOCK_SIZE

            val rad = -GLM.radians(rotation.value)
            for ((index, position) in positions.withIndex()) {
                val out = Vec3(position)
                out.rotateAssign(rad[0], Axes.X, origin, false)
                out.rotateAssign(rad[1], Axes.Y, origin, false)
                out.rotateAssign(rad[2], Axes.Z, origin, false)
                positions[index] = out
            }
        }


        val transform = transform.buffer()
        val textureShaderId = texture.texture.shaderId.buffer()

        for (index in 0 until context.consumer.order.size step 2) {
            val indexPosition = positions[context.consumer.order[index]].array
            val transformedUV = texture.texture.transformUV(texturePositions[context.consumer.order[index + 1]])
            context.consumer.addVertex(indexPosition, transformedUV, transform, textureShaderId)
        }
    }
}
