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

package de.bixilon.minosoft.gui.rendering.sky.clouds

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec4.Vec4
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.time.TimeUtil.millis
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.data.world.time.DayPhases
import de.bixilon.minosoft.data.world.time.MoonPhases
import de.bixilon.minosoft.data.world.time.WorldTime
import de.bixilon.minosoft.data.world.weather.WorldWeather
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.renderer.renderer.AsyncRenderer
import de.bixilon.minosoft.gui.rendering.renderer.renderer.Renderer
import de.bixilon.minosoft.gui.rendering.renderer.renderer.RendererBuilder
import de.bixilon.minosoft.gui.rendering.sky.SkyRenderer
import de.bixilon.minosoft.gui.rendering.system.base.RenderSystem
import de.bixilon.minosoft.gui.rendering.system.base.RenderingCapabilities
import de.bixilon.minosoft.gui.rendering.system.base.phases.OpaqueDrawable
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.interpolateLinear
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnectionStates
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sin

class CloudRenderer(
    private val sky: SkyRenderer,
    val connection: PlayConnection,
    override val context: RenderContext,
) : Renderer, OpaqueDrawable, AsyncRenderer {
    override val renderSystem: RenderSystem = context.renderSystem
    val shader = renderSystem.createShader(minosoft("sky/clouds")) { CloudShader(it) }
    val matrix = CloudMatrix()
    private val layers: MutableList<CloudLayer> = mutableListOf()
    private var position = Vec2i(Int.MIN_VALUE)
    private var color: Vec3 = Vec3.EMPTY
    private var maxDistance = 0.0f
    private var baseHeight = 0
    private var nextLayers = 0
    var flat: Boolean = false
        private set
    private var toUnload: MutableSet<CloudLayer> = mutableSetOf()

    private var time = millis()
    var delta = 0.0f
        private set

    override val skipOpaque: Boolean
        get() = !sky.effects.clouds || !sky.profile.clouds.enabled || connection.profiles.block.viewDistance < 3 || layers.isEmpty()


    override fun asyncInit(latch: CountUpAndDownLatch) {
        matrix.load(connection.assetsManager)
    }

    private fun getCloudHeight(index: Int): IntRange {
        val base = sky.effects.getCloudHeight(connection)
        this.baseHeight = base.first
        val cloudHeight = base.last - base.first

        return IntRange(baseHeight + index * cloudHeight, baseHeight + (index + 1) * cloudHeight)
    }

    override fun postInit(latch: CountUpAndDownLatch) {
        shader.load()
        sky.profile.clouds::movement.observe(this, instant = true) {
            for (layer in layers) {
                layer.movement = it
            }
        }
        sky.profile.clouds::maxDistance.observe(this, instant = true) { this.maxDistance = it }
        connection::state.observe(this) {
            if (it == PlayConnectionStates.SPAWNING) {
                if (!sky.effects.clouds) {
                    return@observe
                }
                // reset clouds
                position = Vec2i(Int.MIN_VALUE)
                for ((index, layer) in this.layers.withIndex()) {
                    layer.height = getCloudHeight(index)
                }
            }
        }
        sky.profile.clouds::layers.observe(this, instant = true) { this.nextLayers = it }
        sky.profile.clouds::flat.observe(this, instant = true) { this.flat = it }
    }

    private fun updateLayers(layers: Int) {
        while (layers < this.layers.size) {
            toUnload += this.layers.removeLast()
        }
        for (index in this.layers.size until layers) {
            val layer = CloudLayer(sky, this, index, getCloudHeight(index))
            this.layers += layer
        }
    }

    override fun prepareDrawAsync() {
        if (!sky.effects.clouds) {
            return
        }
        if (layers.size != nextLayers) {
            updateLayers(nextLayers)
        }

        val time = millis()
        val delta = time - this.time
        this.delta = delta / 1000.0f
        this.time = time

        for (layer in layers) {
            layer.prepareAsync()
        }
    }

    override fun postPrepareDraw() {
        for (unload in toUnload) {
            unload.unload()
        }
        if (!sky.effects.clouds) {
            return
        }
        for (layer in layers) {
            layer.prepare()
        }
    }

    override fun setupOpaque() {
        super.setupOpaque()
        renderSystem.disable(RenderingCapabilities.FACE_CULLING)
    }

    private fun calculateDay(progress: Float): Vec3 {
        return interpolateLinear((abs(progress - 0.5f) * 2).pow(2), DAY_COLOR, DAY_COLOR * 0.8f)
    }

    private fun calculateNight(progress: Float, moon: MoonPhases): Vec3 {
        return interpolateLinear((abs(progress - 0.5f) * 2).pow(2), NIGHT_COLOR, DAY_COLOR * 0.2f) * moon.light
    }

    private fun calculateSunrise(progress: Float, moon: MoonPhases): Vec3 {
        val night = calculateNight(1.0f, moon)
        val day = calculateDay(0.0f)

        val base = interpolateLinear(progress, night, day)
        var color = Vec3(base)
        val sine = maxOf(sin((abs(progress - 0.5f) * 2.0f).pow(2) * PI.toFloat() / 2.0f), 0.4f)

        color = interpolateLinear(sine, SUNRISE_COLOR, color)
        color = interpolateLinear(sky.box.intensity, base, color)

        return color
    }

    fun calculateSunset(progress: Float, moon: MoonPhases): Vec3 {
        val day = calculateDay(1.0f)
        val night = calculateNight(0.0f, moon)

        val base = interpolateLinear(progress, day, night)
        var color = Vec3(base)


        val sine = maxOf(sin((abs(progress - 0.5f) * 2.0f).pow(3) * PI.toFloat() / 2.0f), 0.1f)

        color = interpolateLinear(sine, SUNSET_COLOR, color)
        color = interpolateLinear(sky.box.intensity, base, color)

        return color
    }

    private fun calculateNormal(time: WorldTime): Vec3 {
        return when (time.phase) {
            DayPhases.DAY -> calculateDay(time.progress)
            DayPhases.NIGHT -> calculateNight(time.progress, time.moonPhase)
            DayPhases.SUNRISE -> calculateSunrise(time.progress, time.moonPhase)
            DayPhases.SUNSET -> calculateSunset(time.progress, time.moonPhase)
        }
    }

    private fun calculateRainColor(time: WorldTime, rain: Float, thunder: Float): Vec3 {
        val normal = calculateNormal(time)
        val brightness = normal.length()
        var color = RAIN_COLOR
        color = color * maxOf(1.0f - thunder, 0.4f)

        return interpolateLinear(maxOf(rain, thunder), normal, color) * brightness * 0.8f
    }

    private fun calculateCloudsColor(): Vec3 {
        var weather = connection.world.weather
        if (!sky.effects.weather) {
            weather = WorldWeather.SUNNY
        }
        val time = sky.time
        if (weather.rain > 0.0f || weather.thunder > 0.0f) {
            return calculateRainColor(time, weather.rain, weather.thunder)
        }
        return calculateNormal(time)
    }


    private fun setYOffset() {
        val y = context.connection.camera.entity.renderInfo.eyePosition.y
        var yOffset = 0.0f
        if (baseHeight - y > maxDistance) {
            yOffset = y - baseHeight + maxDistance
        }
        shader.yOffset = yOffset
    }

    override fun drawOpaque() {
        shader.use()
        val color = calculateCloudsColor()
        if (color != this.color) {
            shader.cloudsColor = Vec4(color, 1.0f)
            this.color = color
        }
        setYOffset()


        for (array in layers) {
            array.draw()
        }
    }

    companion object : RendererBuilder<CloudRenderer> {
        override val identifier = minosoft("cloud")
        private val RAIN_COLOR = Vec3(0.31f, 0.35f, 0.40f)
        private val SUNRISE_COLOR = Vec3(0.85f, 0.68f, 0.36f)
        private val DAY_COLOR = Vec3(0.95f, 0.97f, 0.97f)
        private val SUNSET_COLOR = Vec3(1.0f, 0.75f, 0.55f)
        private val NIGHT_COLOR = Vec3(0.08f, 0.13f, 0.18f)


        override fun build(connection: PlayConnection, context: RenderContext): CloudRenderer? {
            val sky = context.renderer[SkyRenderer] ?: return null
            return CloudRenderer(sky, connection, context)
        }
    }
}
