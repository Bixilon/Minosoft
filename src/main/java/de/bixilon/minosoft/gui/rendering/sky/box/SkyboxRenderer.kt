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

package de.bixilon.minosoft.gui.rendering.sky.box

import de.bixilon.kutil.hash.HashUtil.murmur64
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.observer.set.SetObserver.Companion.observeSet
import de.bixilon.kutil.random.RandomUtil.nextFloat
import de.bixilon.minosoft.data.entities.entities.LightningBolt
import de.bixilon.minosoft.data.registries.dimension.effects.DefaultDimensionEffects
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.text.formatting.color.RGBColor.Companion.asColor
import de.bixilon.minosoft.data.world.chunk.update.WorldUpdateEvent
import de.bixilon.minosoft.data.world.chunk.update.chunk.ChunkCreateUpdate
import de.bixilon.minosoft.data.world.chunk.update.chunk.NeighbourChangeUpdate
import de.bixilon.minosoft.data.world.time.WorldTime
import de.bixilon.minosoft.gui.rendering.events.CameraPositionChangeEvent
import de.bixilon.minosoft.gui.rendering.sky.SkyChildRenderer
import de.bixilon.minosoft.gui.rendering.sky.SkyRenderer
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import java.util.*

class SkyboxRenderer(
    private val sky: SkyRenderer,
) : SkyChildRenderer {
    val color = SkyboxColor(sky)
    private val textureCache: MutableMap<ResourceLocation, Texture> = mutableMapOf()
    private val colorShader = sky.renderSystem.createShader(minosoft("sky/skybox")) { SkyboxColorShader(it) }
    private val textureShader = sky.renderSystem.createShader(minosoft("sky/skybox/texture")) { SkyboxTextureShader(it) }
    private val colorMesh = SkyboxMesh(sky.context)
    private val textureMesh = SkyboxTextureMesh(sky.context)
    private var updateTexture = true
    private var updateMatrix = true

    private var time: WorldTime = sky.context.connection.world.time


    private var day = -1L
    var intensity = 1.0f
        private set

    private var texture = false

    init {
        sky::matrix.observe(this) { updateMatrix = true }

        // ToDo: Sync with lightmap, lightnings, etc
        sky.context.connection.world.entities::entities.observeSet(this) {
            val lightnings = it.adds.filterIsInstance<LightningBolt>()
            if (lightnings.isEmpty()) return@observeSet
            color.onStrike(lightnings.maxOf(LightningBolt::duration))
        }

        sky.context.connection.events.listen<WorldUpdateEvent> {
            if (it.update !is NeighbourChangeUpdate && it.update !is ChunkCreateUpdate) return@listen
            if (!it.update.chunk.neighbours.complete) return@listen
            color.updateBase()
        }
        sky.context.connection.events.listen<CameraPositionChangeEvent> {
            color.updateBase()
        }
    }

    override fun onTimeUpdate(time: WorldTime) {
        this.time = time
        if (day != time.day) {
            this.day = time.day
            this.intensity = Random(time.day.murmur64()).nextFloat(0.3f, 1.0f)
        }
    }

    override fun init() {
        for (properties in DefaultDimensionEffects) {
            val texture = properties.fixedTexture ?: continue
            textureCache[texture] = sky.context.textures.staticTextures.create(texture)
        }
    }

    override fun postInit() {
        colorShader.load()
        textureShader.load()

        colorMesh.load()
        textureMesh.load()
        sky.context.textures.staticTextures.use(textureShader)
    }

    private fun updateColorShader() {
        colorShader.skyColor = color.color ?: DEFAULT_SKY_COLOR
        if (updateMatrix) {
            colorShader.skyViewProjectionMatrix = sky.matrix
            updateMatrix = false
        }
    }

    private fun updateTextureShader(texture: ResourceLocation) {
        if (updateTexture) {
            val cache = this.textureCache[texture] ?: throw IllegalStateException("Texture not loaded!")
            textureShader.textureIndexLayer = cache.shaderId
            updateTexture = false
        }
        if (updateMatrix) {
            textureShader.skyViewProjectionMatrix = sky.matrix
            updateMatrix = false
        }
    }

    override fun updateAsync() {
        color.update()
    }

    override fun draw() {
        val texture = sky.effects.fixedTexture
        if (this.texture != (texture != null)) {
            this.texture = (texture != null)
            updateMatrix = true
        }
        if (texture != null) {
            textureShader.use()
            updateTextureShader(texture)
            textureMesh.draw()
        } else {
            colorShader.use()
            updateColorShader()
            colorMesh.draw()
        }
    }

    companion object {
        private val DEFAULT_SKY_COLOR = "#ecff89".asColor()
    }
}
