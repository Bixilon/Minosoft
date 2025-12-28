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

package de.bixilon.minosoft.gui.rendering.sky.box.texture

import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.data.registries.dimension.effects.DefaultDimensionEffects
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.sky.SkyRenderer
import de.bixilon.minosoft.gui.rendering.sky.box.SkyboxType
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture

class SkyboxTextureType(val sky: SkyRenderer) : SkyboxType {
    private val cache: MutableMap<ResourceLocation, Texture> = mutableMapOf()
    private val shader = sky.context.system.shader.create(minosoft("sky/skybox/texture")) { SkyboxTextureShader(it) }
    private val mesh = SkyboxTextureMeshBuilder(sky.context).bake()
    private var updateMatrix = true


    override fun init() {
        for (properties in DefaultDimensionEffects) {
            val texture = properties.fixedTexture ?: continue
            cache[texture] = sky.context.textures.static.create(texture)
        }
    }

    override fun postInit() {
        mesh.load()
        shader.load()
        sky::matrix.observe(this) { updateMatrix = true }
    }

    override fun draw() {
        shader.use()

        val texture = this.cache[sky.effects.fixedTexture] ?: throw IllegalStateException("Texture not loaded!")
        shader.texture = texture.shaderId

        if (updateMatrix) {
            shader.skyViewProjectionMatrix = sky.matrix
            updateMatrix = false
        }

        mesh.draw()
    }

}
