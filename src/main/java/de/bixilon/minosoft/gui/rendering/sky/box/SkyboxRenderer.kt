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
import de.bixilon.kutil.math.MathConstants.PIf
import de.bixilon.kutil.time.TimeUtil.millis
import de.bixilon.kutil.watcher.DataWatcher.Companion.observe
import de.bixilon.kutil.watcher.DataWatcher.Companion.watched
import de.bixilon.kutil.watcher.set.SetDataWatcher.Companion.observeSet
import de.bixilon.minosoft.data.entities.entities.LightningBolt
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

    private var lastStrike = -1L
    private var strikeDuration = -1L

    init {
        sky::matrix.observe(this) { updateMatrix = true }
        this::color.observe(this) { updateColor = true }

        // ToDo: Sync with lightmap, lightnings, etc
        sky.renderWindow.connection.world.entities::entities.observeSet(this) {
            val lightnings = it.adds.filterIsInstance<LightningBolt>()
            if (lightnings.isEmpty()) return@observeSet
            lastStrike = millis()
            strikeDuration = lightnings.maxOf(LightningBolt::duration)
        }
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

    private fun calculateLightingStrike(original: Vec3): Vec3 {
        val duration = this.strikeDuration
        val delta = millis() - lastStrike
        if (delta > duration) {
            return original
        }
        val progress = delta / duration.toFloat()

        val sine = abs(sin(progress * PIf * (duration / 80).toInt()))
        return interpolateLinear(sine, original, Vec3(1.0f))
    }

    private fun calculateThunder(time: WorldTime, rain: Float, thunder: Float): Vec3? {
        val rainColor = calculateRain(time, rain) ?: return null
        val brightness = minOf(rainColor.length() * 2, 1.0f)

        val thunderColor = interpolateLinear(brightness / 8, THUNDER_BASE_COLOR, rainColor)
        thunderColor *= brightness

        return calculateLightingStrike(interpolateLinear(thunder, rainColor, thunderColor))
    }

    private fun calculateRain(time: WorldTime, rain: Float): Vec3? {
        val clearColor = calculateClear(time) ?: return null
        val brightness = minOf(clearColor.length(), 1.0f)

        val rainColor = interpolateLinear(brightness / 8, RAIN_BASE_COLOR, clearColor)
        rainColor *= brightness

        return interpolateLinear(rain, clearColor, rainColor)
    }

    private fun calculateSunrise(progress: Float): Vec3? {
        val night = calculateNight(1.0f) ?: return null
        val day = calculateDaytime(0.0f) ?: return null

        var color = interpolateLinear(progress, night, day)

        // make a bit more red/yellow
        val delta = (abs(progress - 0.5f) * 2.0f)
        val sine = maxOf(sin(delta.pow(2) * PI.toFloat() / 2.0f), 0.6f)

        color = interpolateLinear(sine, SUNRISE_BASE_COLOR, color)

        return color
    }

    private fun calculateDaytime(progress: Float): Vec3? {
        val base = calculateBiomeAvg { it.skyColor }?.toVec3() ?: return null

        return interpolateLinear((abs(progress - 0.5f) * 2.0f).pow(2), base, base * 0.9f)
    }

    private fun calculateSunset(progress: Float): Vec3? {
        val night = calculateNight(0.0f) ?: return null
        val day = calculateDaytime(1.0f) ?: return null

        var color = interpolateLinear(progress, day, night)

        // make a bit more red
        val delta = (abs(progress - 0.5f) * 2.0f)
        val sine = maxOf(sin(delta.pow(3) * PI.toFloat() / 2.0f), 0.4f)

        color = interpolateLinear(sine, SUNSET_BASE_COLOR, color)

        return color
    }

    private fun calculateNight(progress: Float): Vec3? {
        val base = calculateBiomeAvg { it.skyColor }?.toVec3() ?: return null
        base *= 0.1

        return interpolateLinear((abs(progress - 0.5f) * 2.0f), NIGHT_BASE_COLOR, base) * time.moonPhase.light
    }

    private fun calculateClear(time: WorldTime): Vec3? {
        return when (time.phase) {
            DayPhases.SUNRISE -> calculateSunrise(time.progress)
            DayPhases.DAY -> calculateDaytime(time.progress)
            DayPhases.SUNSET -> calculateSunset(time.progress)
            DayPhases.NIGHT -> calculateNight(time.progress)
        }
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
        // TODO: Check if wither is present

        val weather = sky.renderWindow.connection.world.weather

        if (weather.thunder > 0.0f) {
            return calculateThunder(time, weather.rain, weather.thunder)?.let { RGBColor(it) }
        }
        if (weather.raining) {
            return calculateRain(time, weather.rain)?.let { RGBColor(it) }
        }
        return calculateClear(time)?.let { RGBColor(it) }
    }

    companion object {
        private val DEFAULT_SKY_COLOR = "#ecff89".asColor()
        private val THUNDER_BASE_COLOR = Vec3(0.16f, 0.18f, 0.21f)
        private val RAIN_BASE_COLOR = Vec3(0.39f, 0.45f, 0.54f)
        private val SUNRISE_BASE_COLOR = Vec3(0.95f, 0.78f, 0.56f)
        private val SUNSET_BASE_COLOR = Vec3(0.95f, 0.68f, 0.56f)
        private val NIGHT_BASE_COLOR = Vec3(0.02f, 0.04f, 0.09f)
    }
}
