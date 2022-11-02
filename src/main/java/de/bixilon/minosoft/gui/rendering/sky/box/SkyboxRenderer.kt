/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
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

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kutil.watcher.DataWatcher.Companion.observe
import de.bixilon.kutil.watcher.DataWatcher.Companion.watched
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.data.text.formatting.color.RGBColor.Companion.asColor
import de.bixilon.minosoft.data.world.positions.ChunkPositionUtil.chunkPosition
import de.bixilon.minosoft.data.world.time.DayPhases
import de.bixilon.minosoft.data.world.time.WorldTime
import de.bixilon.minosoft.gui.rendering.sky.SkyChildRenderer
import de.bixilon.minosoft.gui.rendering.sky.SkyRenderer
import de.bixilon.minosoft.gui.rendering.sky.properties.DefaultSkyProperties
import de.bixilon.minosoft.gui.rendering.sky.properties.SkyProperties
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.blockPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.interpolateLinear
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.interpolateSine
import de.bixilon.minosoft.util.KUtil.minosoft
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sin

class SkyboxRenderer(
    private val sky: SkyRenderer,
) : SkyChildRenderer {
    private val textureCache: MutableMap<SkyProperties, AbstractTexture> = mutableMapOf()
    private val colorShader = sky.renderSystem.createShader(minosoft("sky/skybox"))
    private val mesh = SkyboxMesh(sky.renderWindow)
    private var updateColor = true
    private var updateMatrix = true
    private var color: RGBColor by watched(ChatColors.BLUE)

    private var time: WorldTime = sky.renderWindow.connection.world.time

    init {
        sky::matrix.observe(this) { updateMatrix = true }
        this::color.observe(this) { updateColor = true }
    }

    override fun onTimeUpdate(time: WorldTime) {
        this.time = time
    }

    override fun init() {
        colorShader.load()

        for (properties in DefaultSkyProperties) {
            val textureName = properties.fixedTexture ?: continue
            textureCache[properties] = sky.renderWindow.textureManager.staticTextures.createTexture(textureName)
        }
    }

    override fun postInit() {
        mesh.load()
    }

    private fun updateUniforms() {
        if (updateColor) {
            colorShader.setRGBColor("uSkyColor", color)
            updateColor = false
        }
        if (updateMatrix) {
            colorShader.setMat4("uSkyViewProjectionMatrix", sky.matrix)
            updateMatrix = false
        }
    }

    override fun updateAsync() {
        color = calculateSkyColor() ?: DEFAULT_SKY_COLOR
    }

    override fun draw() {
        colorShader.use()
        updateUniforms()

        mesh.draw()
    }

    private fun calculateBiomeAvg(average: (Biome) -> RGBColor?): RGBColor? {
        val radius = sky.profile.biomeRadius

        var red = 0
        var green = 0
        var blue = 0
        var count = 0

        val connection = sky.renderWindow.connection

        val cameraPosition = sky.renderWindow.camera.matrixHandler.eyePosition.blockPosition
        val chunk = connection.world[cameraPosition.chunkPosition] ?: return null

        for (xOffset in -radius..radius) {
            for (yOffset in -radius..radius) {
                for (zOffset in -radius..radius) {
                    val x = cameraPosition.x + xOffset
                    val y = cameraPosition.y + yOffset
                    val z = cameraPosition.z + zOffset
                    val neighbour = chunk.traceChunk(Vec2i(x shr 4, z shr 4))
                    val biome = neighbour?.getBiome(x and 0x0F, y, z and 0x0F) ?: continue

                    val color = average(biome) ?: continue
                    red += color.red
                    green += color.green
                    blue += color.blue
                    count++
                }
            }
        }

        if (count == 0) {
            return null
        }
        return RGBColor(red / count, green / count, blue / count)
    }

    private fun calculateThunder(rain: Float, thunder: Float): RGBColor {
        return ChatColors.DARK_GRAY
    }

    private fun calculateRain(rain: Float): RGBColor {
        return ChatColors.GRAY
    }

    private fun calculateSunrise(progress: Float): RGBColor? {
        val night = calculateNight(1.0f)?.toVec3() ?: return null
        val day = calculateDaytime(0.0f)?.toVec3() ?: return null

        var color = interpolateLinear(progress, night, day)

        // make a bit more red/yellow
        val modifier = Vec3(0.95f, 0.78, 0.56f)

        val delta = (abs(progress - 0.5f) * 2.0f)
        val sine = maxOf(sin(delta.pow(2) * PI.toFloat() / 2.0f), 0.6f)

        color = interpolateLinear(sine, modifier, color)

        return RGBColor(color)
    }

    private fun calculateDaytime(progress: Float): RGBColor? {
        val base = calculateBiomeAvg { it.skyColor }?.toVec3() ?: return null

        return RGBColor(interpolateLinear((abs(progress - 0.5f) * 2.0f).pow(2), base, base * 0.9f))
    }

    private fun calculateSunset(progress: Float): RGBColor? {
        val night = calculateNight(0.0f)?.toVec3() ?: return null
        val day = calculateDaytime(1.0f)?.toVec3() ?: return null

        val color = interpolateLinear(progress, day, night)

        // make a bit more red
        color.r *= 0.2f
        color.g *= 0.1f

        return RGBColor(color)
    }

    private fun calculateNight(progress: Float): RGBColor? {
        val base = calculateBiomeAvg { it.skyColor }?.toVec3() ?: return null
        base *= 0.2

        val minColor = Vec3(0.02f, 0.04f, 0.09f) * time.moonPhase.light

        return RGBColor(interpolateSine(abs(progress - 0.5f) * 2.0f, minColor, base))
    }

    private fun calculateSkyColor(): RGBColor? {
        val properties = sky.properties
        val time = time
        if (properties.fixedTexture != null) {
            // sky is a texture, no color (e.g. end)
            return null
        }
        if (!properties.daylightCycle) {
            // no daylight cycle (e.g. nether)
            return calculateBiomeAvg { it.fogColor }
        }
        val weather = sky.renderWindow.connection.world.weather

        if (weather.thunder > 0.0f) {
            return calculateThunder(weather.rain, weather.thunder)
        }
        if (weather.raining) {
            return calculateRain(weather.rain)
        }

        return when (time.phase) {
            DayPhases.SUNRISE -> calculateSunrise(time.progress)
            DayPhases.DAY -> calculateDaytime(time.progress)
            DayPhases.SUNSET -> calculateSunset(time.progress)
            DayPhases.NIGHT -> calculateNight(time.progress)
        }
    }

    companion object {
        val DEFAULT_SKY_COLOR = "#ecff89".asColor()
    }
}
