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

package de.bixilon.minosoft.gui.rendering.skeletal.model

import com.fasterxml.jackson.annotation.JsonIgnore
import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.skeletal.baked.BakedSkeletalModel
import de.bixilon.minosoft.gui.rendering.skeletal.baked.BakedSkeletalTransform
import de.bixilon.minosoft.gui.rendering.skeletal.baked.SkeletalBakeContext
import de.bixilon.minosoft.gui.rendering.skeletal.mesh.AbstractSkeletalMeshBuilder
import de.bixilon.minosoft.gui.rendering.skeletal.model.animations.SkeletalAnimation
import de.bixilon.minosoft.gui.rendering.skeletal.model.elements.SkeletalElement
import de.bixilon.minosoft.gui.rendering.skeletal.model.textures.*
import de.bixilon.minosoft.gui.rendering.skeletal.model.transforms.SkeletalTransform
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import java.util.concurrent.atomic.AtomicInteger

data class SkeletalModel(
    val elements: Map<String, SkeletalElement>,
    val textures: Map<ResourceLocation, SkeletalTexture>,
    val animations: Map<String, SkeletalAnimation> = emptyMap(),
    val transforms: Map<String, SkeletalTransform> = emptyMap(),
) {
    @JsonIgnore
    val loadedTextures: MutableSkeletalInstanceTextureMap = mutableMapOf()

    fun load(context: RenderContext, skip: Set<ResourceLocation>) {
        for ((name, properties) in this.textures) {
            if (name in skip) continue

            val file = name.texture()
            if (file in skip) continue

            val texture = context.textures.static.create(file)
            this.loadedTextures[name] = SkeletalTextureInstance(properties, texture)
        }
    }

    private fun buildTextures(override: SkeletalTextureMap): SkeletalInstanceTextureMap {
        val textures: MutableSkeletalInstanceTextureMap = this.loadedTextures.toMutableMap()

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
        val baseTransform = BakedSkeletalTransform(0, Vec3f.EMPTY, transforms)

        return Pair(baseTransform, transformId.get())
    }

    private fun buildElements(consumer: AbstractSkeletalMeshBuilder, textures: SkeletalInstanceTextureMap, transform: BakedSkeletalTransform) {
        val context = SkeletalBakeContext(transform = transform, textures = textures, consumer = consumer)

        for ((name, element) in elements) {
            element.bake(context, name)
        }
    }

    fun bake(override: SkeletalTextureMap, mesh: AbstractSkeletalMeshBuilder): BakedSkeletalModel {
        val textures = buildTextures(override)
        val (transform, count) = buildTransforms()
        buildElements(mesh, textures, transform)

        return BakedSkeletalModel(mesh.bake(), transform, count, animations)
    }
}
