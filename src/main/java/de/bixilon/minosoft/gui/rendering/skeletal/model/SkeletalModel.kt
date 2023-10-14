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

import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.skeletal.SkeletalMesh
import de.bixilon.minosoft.gui.rendering.skeletal.baked.BakedSkeletalModel
import de.bixilon.minosoft.gui.rendering.skeletal.baked.SkeletalTransform
import de.bixilon.minosoft.gui.rendering.skeletal.model.animations.SkeletalAnimation
import de.bixilon.minosoft.gui.rendering.skeletal.model.elements.SkeletalElement
import de.bixilon.minosoft.gui.rendering.skeletal.model.textures.SkeletalTexture
import de.bixilon.minosoft.gui.rendering.skeletal.model.textures.SkeletalTextureInstance
import de.bixilon.minosoft.gui.rendering.system.base.texture.shader.ShaderTexture
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture

data class SkeletalModel(
    val elements: Map<String, SkeletalElement>,
    val textures: Map<ResourceLocation, SkeletalTexture>,
    val animations: Map<ResourceLocation, SkeletalAnimation>,
) {
    val loadedTextures: MutableMap<ResourceLocation, SkeletalTextureInstance> = mutableMapOf()

    fun load(context: RenderContext) {
        for ((name, properties) in this.textures) {
            if (!properties.load) continue
            context.textures.staticTextures.createTexture(name.texture())
        }
    }

    fun bake(context: RenderContext, override: Map<ResourceLocation, ShaderTexture>): BakedSkeletalModel {
        val mesh = SkeletalMesh(context, 1000)

        val textures: MutableMap<ResourceLocation, SkeletalTextureInstance> = this.loadedTextures.toMutableMap()

        for ((name, texture) in override) {
            textures[name]?.texture = texture
        }

        val baked: MutableMap<String, SkeletalTransform> = mutableMapOf()

        for ((name, element) in elements) {
            baked[name] = element.bake(mesh, textures) ?: continue
        }

        return BakedSkeletalModel(mesh, baked)
    }
}
