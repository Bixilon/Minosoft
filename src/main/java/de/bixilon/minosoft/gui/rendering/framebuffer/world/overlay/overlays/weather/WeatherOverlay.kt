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

package de.bixilon.minosoft.gui.rendering.framebuffer.world.overlay.overlays.weather

import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.kutil.random.RandomUtil.nextFloat
import de.bixilon.minosoft.data.registries.biomes.BiomePrecipitation
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.framebuffer.world.overlay.Overlay
import de.bixilon.minosoft.gui.rendering.framebuffer.world.overlay.OverlayFactory
import de.bixilon.minosoft.gui.rendering.framebuffer.world.overlay.OverlayManager.Companion.OVERLAY_Z
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import java.util.*

class WeatherOverlay(private val context: RenderContext) : Overlay {
    private val world = context.session.world
    private val config = context.session.profiles.rendering.overlay.weather
    private val rain = context.textures.static.create(RAIN)
    private val snow = context.textures.static.create(SNOW)
    private val precipitation get() = context.session.player.physics.positionInfo.biome?.precipitation
    override val render: Boolean
        get() = world.dimension.effects.weather && world.weather.raining && when (precipitation) { // ToDo: Check if exposed to the sky
            null -> false
            BiomePrecipitation.RAIN -> config.rain
            BiomePrecipitation.SNOW -> config.snow
        }
    private val texture: Texture?
        get() = when (precipitation) {
            null -> null
            BiomePrecipitation.RAIN -> rain
            BiomePrecipitation.SNOW -> snow
        }

    private val shader = context.system.shader.create(minosoft("weather/overlay")) { WeatherOverlayShader(it) }
    private var mesh = WeatherOverlayMesh(context)
    private var windowSize = Vec2f.EMPTY


    private fun updateMesh(windowSize: Vec2f) {
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
                Vec2f(offset, 0f), OVERLAY_Z, Vec2f(offset + step, windowSize.y), Vec2f.EMPTY, texture.array.uvEnd ?: Vec2f.ONE
            ) { position, uv ->
                val transformed = Vec3f(
                    position.x / (windowSize.x / 2) - 1.0f,
                    position.y / (windowSize.y / 2) - 1.0f,
                    OVERLAY_Z,
                )
                mesh.addVertex(transformed, uv, timeOffset, offsetMultiplicator, alpha)
            }
            offset += step
            if (offset > windowSize.x) {
                break
            }
        }
        this.windowSize = windowSize
        mesh.load()
    }

    override fun postInit() {
        shader.load()
        shader.use()
        context.textures.static.use(shader)
    }

    private fun updateShader() {
        shader.intensity = world.weather.rain
        val offset = (System.currentTimeMillis() % 500.0f) / 500.0f
        shader.offset = -offset
        shader.textureIndexLayer = texture!!.shaderId
    }

    override fun draw() {
        val windowSize = Vec2f(context.window.size)
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
