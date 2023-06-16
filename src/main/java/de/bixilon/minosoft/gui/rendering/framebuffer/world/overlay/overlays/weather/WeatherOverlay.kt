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

package de.bixilon.minosoft.gui.rendering.framebuffer.world.overlay.overlays.weather

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kutil.random.RandomUtil.nextFloat
import de.bixilon.kutil.time.TimeUtil.millis
import de.bixilon.minosoft.data.registries.biomes.BiomePrecipitation
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.framebuffer.world.overlay.Overlay
import de.bixilon.minosoft.gui.rendering.framebuffer.world.overlay.OverlayFactory
import de.bixilon.minosoft.gui.rendering.framebuffer.world.overlay.OverlayManager.Companion.OVERLAY_Z
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util.EMPTY
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import java.util.*

class WeatherOverlay(private val context: RenderContext) : Overlay {
    private val world = context.connection.world
    private val config = context.connection.profiles.rendering.overlay.weather
    private val rain = context.textureManager.staticTextures.createTexture(RAIN)
    private val snow = context.textureManager.staticTextures.createTexture(SNOW)
    private val precipitation get() = context.connection.player.physics.positionInfo.biome?.precipitation ?: BiomePrecipitation.NONE
    override val render: Boolean
        get() = world.dimension.effects.weather && world.weather.raining && when (precipitation) { // ToDo: Check if exposed to the sky
            BiomePrecipitation.NONE -> false
            BiomePrecipitation.RAIN -> config.rain
            BiomePrecipitation.SNOW -> config.snow
        }
    private val texture: Texture?
        get() = when (precipitation) {
            BiomePrecipitation.NONE -> null
            BiomePrecipitation.RAIN -> rain
            BiomePrecipitation.SNOW -> snow
        }

    private val shader = context.renderSystem.createShader(minosoft("weather/overlay")) { WeatherOverlayShader(it) }
    private var mesh = WeatherOverlayMesh(context)
    private var windowSize = Vec2.EMPTY


    private fun updateMesh(windowSize: Vec2) {
        if (mesh.state == Mesh.MeshStates.LOADED) {
            mesh.unload()
        }
        mesh = WeatherOverlayMesh(context)

        val texture = texture!!
        val scale = windowSize.y / texture.size.y
        val step = texture.size.x * scale
        var offset = 0.0f
        val random = Random()
        while (true) {
            val timeOffset = random.nextFloat(0.0f, 1.0f)
            val offsetMultiplicator = random.nextFloat(0.8f, 1.2f)
            val alpha = random.nextFloat(0.8f, 1.0f)
            mesh.addZQuad(
                Vec2(offset, 0), OVERLAY_Z, Vec2(offset + step, windowSize.y), Vec2(0.0f), texture.array.uvEnd
            ) { position, uv ->
                val transformed = Vec2()
                transformed.x = position.x / (windowSize.x / 2) - 1.0f
                transformed.y = position.y / (windowSize.y / 2) - 1.0f
                mesh.addVertex(Vec3(transformed.x, transformed.y, OVERLAY_Z), uv, timeOffset, offsetMultiplicator, alpha)
            }
            offset += step
            if (offset > windowSize.x) {
                break
            }
        }
        this.windowSize = windowSize
        mesh.load()
    }

    override fun init() {
        shader.load()
    }

    override fun postInit() {
        shader.use()
        context.textureManager.staticTextures.use(shader)
    }

    private fun updateShader() {
        shader.intensity = world.weather.rain
        val offset = (millis() % 500L) / 500.0f
        shader.offset = -offset
        shader.textureIndexLayer = texture!!.shaderId
    }

    override fun draw() {
        val windowSize = context.window.sizef
        if (this.windowSize != windowSize) {
            updateMesh(windowSize)
        }
        shader.use()
        updateShader()
        mesh.draw()
    }

    companion object : OverlayFactory<WeatherOverlay> {
        private val RAIN = "environment/rain".toResourceLocation().texture()
        private val SNOW = "environment/snow".toResourceLocation().texture()

        override fun build(context: RenderContext): WeatherOverlay {
            return WeatherOverlay(context)
        }
    }
}
