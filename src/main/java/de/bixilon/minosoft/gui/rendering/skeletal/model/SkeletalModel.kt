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

package de.bixilon.minosoft.gui.rendering.skeletal.model

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.skeletal.baked.BakedSkeletalModel
import de.bixilon.minosoft.gui.rendering.skeletal.baked.BakedSkeletalTransform
import de.bixilon.minosoft.gui.rendering.skeletal.mesh.AbstractSkeletalMesh
import de.bixilon.minosoft.gui.rendering.skeletal.model.animations.SkeletalAnimation
import de.bixilon.minosoft.gui.rendering.skeletal.model.elements.SkeletalElement
import de.bixilon.minosoft.gui.rendering.skeletal.model.textures.SkeletalTexture
import de.bixilon.minosoft.gui.rendering.skeletal.model.textures.SkeletalTextureInstance
import de.bixilon.minosoft.gui.rendering.skeletal.model.transforms.SkeletalTransform
import de.bixilon.minosoft.gui.rendering.system.base.texture.shader.ShaderTexture
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY
import java.util.concurrent.atomic.AtomicInteger

data class SkeletalModel(
    val elements: Map<String, SkeletalElement>,
    val textures: Map<ResourceLocation, SkeletalTexture>,
    val animations: Map<String, SkeletalAnimation> = emptyMap(),
    val transforms: Map<String, SkeletalTransform> = emptyMap(),
) {
    val loadedTextures: MutableMap<ResourceLocation, SkeletalTextureInstance> = mutableMapOf()

    fun load(context: RenderContext, skip: Set<ResourceLocation>) {
        for ((name, properties) in this.textures) {
            if (name in skip) continue
            val file = name.texture()
            if (file in skip) continue
            val texture = context.textures.staticTextures.create(file)
            this.loadedTextures[name] = SkeletalTextureInstance(properties, texture)
        }
    }

    private fun buildTextures(override: Map<ResourceLocation, ShaderTexture>): Map<ResourceLocation, SkeletalTextureInstance> {
        val textures: MutableMap<ResourceLocation, SkeletalTextureInstance> = this.loadedTextures.toMutableMap()

        for ((name, texture) in override) {
            val instance = textures[name]
            if (instance != null) {
                instance.texture = texture
            } else {
                val properties = this.textures[name] ?: continue
                textures[name] = SkeletalTextureInstance(properties, texture)
            }
        }

        return textures
    }

    private fun buildTransforms(): Pair<BakedSkeletalTransform, Int> {
        val transforms: MutableMap<String, BakedSkeletalTransform> = mutableMapOf()

        val transformId = AtomicInteger(1)
        for ((name, transform) in this.transforms) {
            transforms[name] = transform.bake(transformId)
        }
        val baseTransform = BakedSkeletalTransform(0, Vec3.EMPTY, transforms)

        return Pair(baseTransform, transformId.get())
    }

    private fun buildElements(consumer: AbstractSkeletalMesh, textures: Map<ResourceLocation, SkeletalTextureInstance>, transform: BakedSkeletalTransform) {
        for ((name, element) in elements) {
            element.bake(consumer, textures, transform, name)
        }
    }

    fun bake(context: RenderContext, override: Map<ResourceLocation, ShaderTexture>, mesh: AbstractSkeletalMesh): BakedSkeletalModel {
        val textures = buildTextures(override)
        val (transform, count) = buildTransforms()
        buildElements(mesh, textures, transform)

        return BakedSkeletalModel(mesh, transform, count, animations)
    }
}
