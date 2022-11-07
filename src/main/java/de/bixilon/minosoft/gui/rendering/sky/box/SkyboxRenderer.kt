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
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.math.MathConstants.PIf
import de.bixilon.kutil.time.TimeUtil.millis
import de.bixilon.kutil.watcher.DataWatcher.Companion.observe
import de.bixilon.kutil.watcher.DataWatcher.Companion.watched
import de.bixilon.kutil.watcher.set.SetDataWatcher.Companion.observeSet
import de.bixilon.minosoft.data.entities.entities.LightningBolt
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.data.text.formatting.color.RGBColor.Companion.asColor
import de.bixilon.minosoft.data.world.chunk.Chunk
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.ChunkPositionUtil.chunkPosition
import de.bixilon.minosoft.data.world.positions.ChunkPositionUtil.inChunkPosition
import de.bixilon.minosoft.data.world.time.DayPhases
import de.bixilon.minosoft.data.world.time.WorldTime
import de.bixilon.minosoft.gui.rendering.events.CameraPositionChangeEvent
import de.bixilon.minosoft.gui.rendering.sky.SkyChildRenderer
import de.bixilon.minosoft.gui.rendering.sky.SkyRenderer
import de.bixilon.minosoft.gui.rendering.sky.properties.DefaultSkyProperties
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.blockPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.interpolateLinear
import de.bixilon.minosoft.modding.event.events.blocks.chunk.ChunkDataChangeEvent
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import de.bixilon.minosoft.util.KUtil.minosoft
import de.bixilon.minosoft.util.KUtil.murmur64
import java.util.*
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sin

class SkyboxRenderer(
    private val sky: SkyRenderer,
) : SkyChildRenderer {
    private val textureCache: MutableMap<ResourceLocation, AbstractTexture> = mutableMapOf()
    private val colorShader = sky.renderSystem.createShader(minosoft("sky/skybox"))
    private val textureShader = sky.renderSystem.createShader(minosoft("sky/skybox/texture"))
    private val colorMesh = SkyboxMesh(sky.renderWindow)
    private val textureMesh = SkyboxTextureMesh(sky.renderWindow)
    private var updateColor = true
    private var updateTexture = true
    private var updateMatrix = true
    private var color: RGBColor by watched(ChatColors.BLUE)

    private var time: WorldTime = sky.renderWindow.connection.world.time

    private var lastStrike = -1L
    private var strikeDuration = -1L

    private var cameraPosition: Vec3i? = null
    private var chunkPosition: ChunkPosition? = null
    private var chunk: Chunk? = null
    private var baseColor: RGBColor? = null

    private var day = -1L
    private var intensity = 1.0f

    private var texture = false

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
        sky.renderWindow.connection.events.listen<CameraPositionChangeEvent> {
            val blockPosition = it.newPosition.blockPosition
            if (blockPosition == this.cameraPosition) {
                return@listen
            }
            this.cameraPosition = blockPosition
            val chunkPosition = blockPosition.chunkPosition
            if (chunkPosition != this.chunkPosition) {
                this.chunkPosition = chunkPosition
                this.chunk = sky.renderWindow.connection.world[chunkPosition]
            }
            recalculateBaseColor()
        }

        sky.renderWindow.connection.events.listen<ChunkDataChangeEvent> {
            if (!it.chunk.neighbours.complete) {
                return@listen
            }
            if (it.chunkPosition == chunkPosition || this.chunk == it.chunk) {
                this.chunk = it.chunk
                recalculateBaseColor()
            }
        }
    }

    private fun recalculateBaseColor() {
        baseColor = calculateBiomeAvg(Biome::skyColor)
    }

    override fun onTimeUpdate(time: WorldTime) {
        this.time = time
        if (day != time.day) {
            this.day = time.day
            this.intensity = Random(time.day.murmur64()).nextFloat(0.3f, 1.0f)
        }
    }

    override fun init() {
        colorShader.load()
        textureShader.load()

        for (properties in DefaultSkyProperties) {
            val texture = properties.fixedTexture ?: continue
            textureCache[texture] = sky.renderWindow.textureManager.staticTextures.createTexture(texture)
        }
    }

    override fun postInit() {
        colorMesh.load()
        textureMesh.load()
        sky.renderWindow.textureManager.staticTextures.use(textureShader)
    }

    private fun updateColorShader() {
        if (updateColor) {
            colorShader.setRGBColor("uSkyColor", color)
            updateColor = false
        }
        if (updateMatrix) {
            colorShader.setMat4("uSkyViewProjectionMatrix", sky.matrix)
            updateMatrix = false
        }
    }

    private fun updateTextureShader(texture: ResourceLocation) {
        if (updateTexture) {
            val cache = this.textureCache[texture] ?: throw IllegalStateException("Texture not loaded!")
            textureShader.setUInt("uIndexLayer", cache.shaderId)
            updateTexture = false
        }
        if (updateMatrix) {
            textureShader.setMat4("uSkyViewProjectionMatrix", sky.matrix)
            updateMatrix = false
        }
    }

    override fun updateAsync() {
        color = calculateSkyColor() ?: DEFAULT_SKY_COLOR
    }

    override fun draw() {
        val texture = sky.properties.fixedTexture
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

    private fun calculateBiomeAvg(average: (Biome) -> RGBColor?): RGBColor? {
        var radius = sky.profile.biomeRadius
        radius *= radius

        var red = 0
        var green = 0
        var blue = 0
        var count = 0

        val cameraPosition = this.cameraPosition?.inChunkPosition ?: return null
        val offset = Vec3i(cameraPosition)
        val chunk = this.chunk ?: return null

        val dimension = sky.connection.world.dimension ?: return null
        val yRange: IntRange

        if (dimension.supports3DBiomes) {
            if (offset.y - radius < dimension.minY) {
                offset.y = dimension.minY
                yRange = IntRange(0, radius)
            } else if (offset.y + radius > dimension.maxY) {
                offset.y = dimension.maxY
                yRange = IntRange(-radius, 0)
            } else {
                yRange = IntRange(-radius, radius)
            }
        } else {
            yRange = 0..1
        }

        for (xOffset in -radius..radius) {
            for (yOffset in yRange) {
                for (zOffset in -radius..radius) {
                    if (xOffset * xOffset + yOffset * yOffset + zOffset * zOffset > radius) {
                        continue
                    }
                    val x = offset.x + xOffset
                    val y = offset.y + yOffset
                    val z = offset.z + zOffset
                    val neighbour = chunk.traceChunk(Vec2i(x shr 4, z shr 4)) ?: continue
                    val biome = neighbour.getBiome(x and 0x0F, y, z and 0x0F) ?: continue

                    count++
                    val color = average(biome) ?: continue
                    red += color.red
                    green += color.green
                    blue += color.blue
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

        val baseColor = interpolateLinear(progress, night, day)
        var color = Vec3(baseColor)

        // make a bit more red/yellow
        val delta = (abs(progress - 0.5f) * 2.0f)
        val sine = maxOf(sin(delta.pow(2) * PI.toFloat() / 2.0f), 0.6f)


        color = interpolateLinear(sine, SUNRISE_BASE_COLOR, color)
        color = interpolateLinear(intensity, baseColor, color)

        return color
    }

    private fun calculateDaytime(progress: Float): Vec3? {
        val base = this.baseColor?.toVec3() ?: return null

        return interpolateLinear((abs(progress - 0.5f) * 2.0f).pow(2), base, base * 0.9f)
    }

    private fun calculateSunset(progress: Float): Vec3? {
        val night = calculateNight(0.0f) ?: return null
        val day = calculateDaytime(1.0f) ?: return null

        val baseColor = interpolateLinear(progress, day, night)
        var color = Vec3(baseColor)

        // make a bit more red
        val delta = (abs(progress - 0.5f) * 2.0f)
        val sine = maxOf(sin(delta.pow(3) * PI.toFloat() / 2.0f), 0.4f)

        color = interpolateLinear(sine, SUNSET_BASE_COLOR, color)
        color = interpolateLinear(intensity, baseColor, color)

        return color
    }

    private fun calculateNight(progress: Float): Vec3? {
        val base = this.baseColor?.toVec3() ?: return null
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
            return calculateBiomeAvg { it.fogColor } // ToDo: Optimize
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
