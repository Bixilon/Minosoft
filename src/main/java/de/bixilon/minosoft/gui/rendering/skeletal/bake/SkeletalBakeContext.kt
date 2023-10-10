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

package de.bixilon.minosoft.gui.rendering.skeletal.bake

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.skeletal.SkeletalVertexConsumer
import de.bixilon.minosoft.gui.rendering.skeletal.model.elements.SkeletalElement
import de.bixilon.minosoft.gui.rendering.skeletal.model.textures.SkeletalTextureInstance
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY
import java.util.concurrent.atomic.AtomicInteger

data class SkeletalBakeContext(
    val offset: Vec3 = Vec3.EMPTY,
    val rotation: Vec3 = Vec3.EMPTY,
    val inflate: Float = 0.0f,
    val transparency: Boolean = true,
    val texture: ResourceLocation? = null,

    val textures: Map<ResourceLocation, SkeletalTextureInstance>,
    val transform: AtomicInteger = AtomicInteger(),
    val consumer: SkeletalVertexConsumer,
) {

    fun copy(element: SkeletalElement): SkeletalBakeContext {
        val offset = this.offset + element.pivot
        val rotation = this.rotation + element.rotation
        val inflate = this.inflate + element.inflate
        val transparency = this.transparency && element.transparency
        val texture = element.texture ?: texture

        return copy(offset = offset, rotation = rotation, inflate = inflate, transparency = transparency, texture = texture)
    }
}
