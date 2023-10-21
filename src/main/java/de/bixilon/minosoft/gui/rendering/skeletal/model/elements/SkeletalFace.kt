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
import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.models.block.element.ModelElement.Companion.BLOCK_SIZE
import de.bixilon.minosoft.gui.rendering.models.block.element.face.FaceUV
import de.bixilon.minosoft.gui.rendering.models.util.CuboidUtil
import de.bixilon.minosoft.gui.rendering.skeletal.baked.SkeletalBakeContext
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.rotateAssign

data class SkeletalFace(
    val uv: FaceUV? = null,
    val texture: ResourceLocation? = null,
) {


    fun bake(context: SkeletalBakeContext, direction: Directions, element: SkeletalElement, transform: Int) {
        val from = context.offset + (element.from - context.inflate) / BLOCK_SIZE
        val to = context.offset + (element.to + context.inflate) / BLOCK_SIZE
        val positions = CuboidUtil.positions(direction, from, to)

        val texture = context.textures[texture ?: context.texture ?: throw IllegalStateException("Element has no texture set!")] ?: throw IllegalStateException("Texture not found!")

        // TODO: why flip on x?
        val uv = this.uv ?: CuboidUtil.cubeUV(element.uv!!, element.from, element.to, direction)

        val uvData = FaceUV(
            texture.texture.transformUV(Vec2(uv.end.x, uv.start.y) / texture.properties.resolution),
            texture.texture.transformUV(Vec2(uv.start.x, uv.end.y) / texture.properties.resolution),
        ).toArray(direction, 0)


        for (rotation in context.rotations) {
            val origin = rotation.origin!! / BLOCK_SIZE

            val rad = -GLM.radians(rotation.value)
            val vec = Vec3(0, positions)
            for (i in 0 until 4) {
                vec.ofs = i * Vec3.length
                vec.rotateAssign(rad[0], Axes.X, origin, false)
                vec.rotateAssign(rad[1], Axes.Y, origin, false)
                vec.rotateAssign(rad[2], Axes.Z, origin, false)
            }
        }

        context.consumer.addQuad(positions, uvData, transform, texture.texture)
    }
}
