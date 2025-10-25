/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.skeletal.baked

import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.models.block.element.ModelElement.Companion.BLOCK_SIZE
import de.bixilon.minosoft.gui.rendering.skeletal.mesh.AbstractSkeletalMeshBuilder
import de.bixilon.minosoft.gui.rendering.skeletal.model.elements.SkeletalElement
import de.bixilon.minosoft.gui.rendering.skeletal.model.elements.SkeletalRotation
import de.bixilon.minosoft.gui.rendering.skeletal.model.textures.SkeletalInstanceTextureMap

data class SkeletalBakeContext(
    val offset: Vec3f = Vec3f.EMPTY,
    val inflate: Float = 0.0f,
    val texture: ResourceLocation? = null,
    val transform: BakedSkeletalTransform,
    val rotation: SkeletalRotation? = null,

    val textures: SkeletalInstanceTextureMap,
    val consumer: AbstractSkeletalMeshBuilder,
) {

    fun copy(element: SkeletalElement): SkeletalBakeContext {
        val offset = this.offset + (element.offset / BLOCK_SIZE)
        val inflate = this.inflate + element.inflate
        val texture = element.texture ?: texture
        val rotation = element.rotation ?: this.rotation
        val transform = element.transform?.let { transform.children[element.transform] ?: throw IllegalArgumentException("Can not find transform ${element.transform}") } ?: transform

        return copy(offset = offset, inflate = inflate, texture = texture, transform = transform, rotation = rotation)
    }
}
