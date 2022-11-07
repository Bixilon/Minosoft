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

package de.bixilon.minosoft.gui.rendering.sky.clouds

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec4.Vec4
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.kutil.watcher.DataWatcher.Companion.observe
import de.bixilon.minosoft.config.profile.delegate.watcher.SimpleProfileDelegateWatcher.Companion.profileWatch
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.world.time.WorldTime
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.renderer.renderer.AsyncRenderer
import de.bixilon.minosoft.gui.rendering.renderer.renderer.Renderer
import de.bixilon.minosoft.gui.rendering.renderer.renderer.RendererBuilder
import de.bixilon.minosoft.gui.rendering.sky.SkyRenderer
import de.bixilon.minosoft.gui.rendering.system.base.RenderSystem
import de.bixilon.minosoft.gui.rendering.system.base.RenderingCapabilities
import de.bixilon.minosoft.gui.rendering.system.base.phases.OpaqueDrawable
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnectionStates
import de.bixilon.minosoft.util.KUtil.minosoft
import de.bixilon.minosoft.util.KUtil.murmur64
import java.util.*
import kotlin.math.abs

class CloudsRenderer(
    private val sky: SkyRenderer,
    private val connection: PlayConnection,
    override val renderWindow: RenderWindow,
) : Renderer, OpaqueDrawable, AsyncRenderer {
    override val renderSystem: RenderSystem = renderWindow.renderSystem
    private val shader = renderSystem.createShader(minosoft("sky/clouds"))
    val matrix = CloudMatrix()
    private var position = Vec2i(Int.MIN_VALUE)
    private val arrays: Array<CloudArray> = arrayOfNulls<CloudArray?>(3 * 3).unsafeCast()
    private var color: Vec3 = Vec3.EMPTY
    private var offset = 0.0f
    private var movement = true
    var height: IntRange = sky.properties.getCloudHeight(connection)
        private set
    private var maxDistance = 0.0f
    private var yOffset = 0.0f
    private var day = -1L
    private var randomSpeed = 0.0f

    override val skipOpaque: Boolean
        get() = !sky.properties.clouds || !sky.profile.clouds.enabled || connection.profiles.block.viewDistance < 3


    override fun asyncInit(latch: CountUpAndDownLatch) {
        matrix.load(connection.assetsManager)
    }

    override fun init(latch: CountUpAndDownLatch) {
        shader.load()
    }

    override fun postInit(latch: CountUpAndDownLatch) {
        sky.profile.clouds::movement.profileWatch(this, instant = true, profile = connection.profiles.rendering) { this.movement = it }
        sky.profile.clouds::maxDistance.profileWatch(this, instant = true, profile = connection.profiles.rendering) { this.maxDistance = it }
        connection::state.observe(this) {
            if (it == PlayConnectionStates.SPAWNING) {
                // reset clouds
                position = Vec2i(Int.MIN_VALUE)
                height = sky.properties.getCloudHeight(connection)
            }
        }
    }

    private fun push(from: Int, to: Int) {
        arrays[to].unload()
        arrays[to] = arrays[from]
    }

    private fun fill(index: Int) {
        val offset = Vec2i((index % 3) - 1, (index / 3) - 1)
        arrays[index] = CloudArray(this, position.cloudPosition() + offset)
    }

    fun pushX(negative: Boolean) {
        if (negative) {
            push(1, 0); push(2, 1)
            push(4, 3); push(5, 4)
            push(7, 6); push(8, 7)
            fill(2); fill(5); fill(8)
        } else {
            push(1, 2); push(0, 1)
            push(4, 5); push(3, 4)
            push(7, 8); push(6, 7)
            fill(0); fill(3); fill(6)
        }
    }

    fun pushZ(negative: Boolean) {
        if (negative) {
            push(3, 0); push(6, 3)
            push(4, 1); push(7, 4)
            push(5, 2); push(8, 5)
            fill(6); fill(7); fill(8)
        } else {
            push(3, 6); push(0, 3)
            push(4, 7); push(1, 4)
            push(5, 8); push(2, 5)
            fill(0); fill(1); fill(2)
        }
    }

    fun push(offset: Vec2i) {
        if (offset.x != 0) pushX(offset.x == 1)
        if (offset.y != 0) pushZ(offset.y == 1)
    }

    private fun reset(cloudPosition: Vec2i) {
        for (array in arrays.unsafeCast<Array<CloudArray?>>()) {
            array?.unload()
        }
        for (x in -1..1) {
            for (z in -1..1) {
                arrays[(x + 1) + 3 * (z + 1)] = CloudArray(this, cloudPosition + Vec2i(x, z))
            }
        }
    }

    private fun Vec2i.cloudPosition(): Vec2i {
        return this shr 4
    }

    private fun updatePosition() {
        val offset = this.offset.toInt()
        val position = connection.player.positionInfo.chunkPosition + Vec2i(offset / CloudArray.CLOUD_SIZE, 0)
        if (position == this.position) {
            return
        }

        val cloudPosition = position.cloudPosition()
        val arrayDelta = cloudPosition - this.position.cloudPosition()


        this.position = position
        if (abs(arrayDelta.x) > 1 || abs(arrayDelta.y) > 1) {
            // major position change (e.g. teleport)
            reset(cloudPosition)
        } else {
            push(arrayDelta)
        }
    }

    override fun prepareDrawAsync() {
        val day = sky.time.day
        if (day != this.day) {
            this.day = day
            randomSpeed = Random(sky.time.age.murmur64()).nextFloat(0.0f, 0.1f)
        }
    }

    override fun postPrepareDraw() {
        updateOffset()
        updatePosition()
    }

    override fun setupOpaque() {
        super.setupOpaque()
        renderSystem.disable(RenderingCapabilities.FACE_CULLING)
    }

    private fun calculateNormal(time: WorldTime): Vec3 {
        return Vec3(1.0f)
    }

    private fun calculateRainColor(time: WorldTime, rain: Float): Vec3 {
        return Vec3(0.5f)
    }

    private fun calculateCloudsColor(): Vec3 {
        val weather = connection.world.weather
        val time = sky.time
        if (weather.rain > 0.0f || weather.thunder > 0.0f) {
            return calculateRainColor(time, maxOf(weather.rain, weather.thunder))
        }
        return calculateNormal(time)
    }

    private fun getCloudSpeed(): Float {
        return randomSpeed + 0.1f
    }

    private fun updateOffset() {
        if (!movement) {
            return
        }
        var offset = this.offset
        offset += getCloudSpeed()
        if (offset > MAX_OFFSET) {
            offset -= MAX_OFFSET
        }
        this.offset = offset
    }

    private fun setYOffset() {
        val y = renderWindow.camera.matrixHandler.eyePosition.y
        var yOffset = 0.0f
        if (height.first - y > maxDistance) {
            yOffset = y - height.first + maxDistance
        }
        if (yOffset != this.yOffset) {
            shader.setFloat("uYOffset", yOffset)
            this.yOffset = yOffset
        }
    }

    override fun drawOpaque() {
        shader.use()
        val color = calculateCloudsColor()
        if (color != this.color) {
            shader.setVec4("uCloudsColor", Vec4(color, 1.0f))
            this.color = color
        }
        if (movement) {
            shader.setFloat("uOffset", offset)
        }
        setYOffset()


        for (array in arrays) {
            array.draw()
        }
    }

    companion object : RendererBuilder<CloudsRenderer> {
        override val RESOURCE_LOCATION = ResourceLocation("minosoft:clouds")
        private const val MAX_OFFSET = CloudMatrix.CLOUD_MATRIX_MASK * CloudArray.CLOUD_SIZE


        override fun build(connection: PlayConnection, renderWindow: RenderWindow): CloudsRenderer? {
            val sky = renderWindow.renderer[SkyRenderer] ?: return null
            return CloudsRenderer(sky, connection, renderWindow)
        }
    }
}
