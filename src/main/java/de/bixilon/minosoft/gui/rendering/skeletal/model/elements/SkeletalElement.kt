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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.skeletal.baked.BakedSkeletalTransform
import de.bixilon.minosoft.gui.rendering.skeletal.baked.SkeletalBakeContext
import de.bixilon.minosoft.gui.rendering.skeletal.mesh.AbstractSkeletalMesh
import de.bixilon.minosoft.gui.rendering.skeletal.model.textures.SkeletalTextureInstance
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY
import de.bixilon.minosoft.util.json.SkeletalFaceDeserializer
import de.bixilon.minosoft.util.json.SkeletalRotationDeserializer

data class SkeletalElement(
    val from: Vec3,
    val to: Vec3,
    val offset: Vec3 = Vec3.EMPTY,
    @JsonDeserialize(using = SkeletalRotationDeserializer::class) val rotation: SkeletalRotation? = null,
    val inflate: Float = 0.0f,
    val texture: ResourceLocation? = null,
    val uv: Vec2i? = null,
    val transform: String? = null,
    @JsonDeserialize(using = SkeletalFaceDeserializer::class)
    val faces: Map<Directions, SkeletalFace>,
    val children: Map<String, SkeletalElement> = emptyMap(),
) {

    fun bake(consumer: AbstractSkeletalMesh, textures: Map<ResourceLocation, SkeletalTextureInstance>, transform: BakedSkeletalTransform, path: String) {
        val context = SkeletalBakeContext(transform = transform, textures = textures, consumer = consumer)
        return bake(context, path)
    }

    private fun bake(context: SkeletalBakeContext, path: String) {
        val context = context.copy(this)


        val transform = context.transform.id

        for ((direction, face) in faces) {
            face.bake(context, direction, this, transform, path)
        }

        for ((name, child) in children) {
            child.bake(context, "$path.$name")
        }
    }
}
