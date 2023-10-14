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

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.skeletal.SkeletalMesh
import de.bixilon.minosoft.gui.rendering.skeletal.baked.SkeletalBakeContext
import de.bixilon.minosoft.gui.rendering.skeletal.baked.SkeletalTransform
import de.bixilon.minosoft.gui.rendering.skeletal.model.textures.SkeletalTextureInstance
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY

data class SkeletalElement(
    val from: Vec3,
    val to: Vec3,
    val origin: Vec3 = (from - to) / 2.0f,
    val pivot: Vec3 = Vec3.EMPTY,
    val rotation: Vec3 = Vec3.EMPTY,
    val inflate: Float = 0.0f,
    val transparency: Boolean = true,
    val enabled: Boolean = true,
    val texture: ResourceLocation? = null,
    val transform: Boolean = true,
    val faces: Map<Directions, SkeletalFace>,
    val children: Map<String, SkeletalElement>,
) {

    fun bake(mesh: SkeletalMesh, textures: Map<ResourceLocation, SkeletalTextureInstance>): SkeletalTransform? {
        if (!enabled) return null

        val context = SkeletalBakeContext(textures = textures, consumer = mesh)
        return bake(context)
    }

    private fun bake(context: SkeletalBakeContext): SkeletalTransform? {
        if (!enabled) return null
        val context = context.copy(this)

        val transform = if (!this.transform) context.transform.get() else context.transform.getAndIncrement()
        val transforms: MutableMap<String, SkeletalTransform> = mutableMapOf()

        for ((direction, face) in faces) {
            face.bake(context, direction, this, transform)
        }

        for ((name, child) in children) {
            transforms[name] = child.bake(context) ?: continue
        }
        if (!this.transform) return null

        return SkeletalTransform(transform, transforms)
    }
}
