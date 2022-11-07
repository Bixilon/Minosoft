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
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.world.time.WorldTime
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.renderer.renderer.Renderer
import de.bixilon.minosoft.gui.rendering.renderer.renderer.RendererBuilder
import de.bixilon.minosoft.gui.rendering.sky.SkyRenderer
import de.bixilon.minosoft.gui.rendering.system.base.RenderSystem
import de.bixilon.minosoft.gui.rendering.system.base.RenderingCapabilities
import de.bixilon.minosoft.gui.rendering.system.base.phases.OpaqueDrawable
import de.bixilon.minosoft.gui.rendering.util.VecUtil.plus
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnectionStates
import de.bixilon.minosoft.util.KUtil.minosoft
import kotlin.math.abs

class CloudsRenderer(
    private val sky: SkyRenderer,
    private val connection: PlayConnection,
    override val renderWindow: RenderWindow,
) : Renderer, OpaqueDrawable {
    override val renderSystem: RenderSystem = renderWindow.renderSystem
    private val shader = renderSystem.createShader(minosoft("sky/clouds"))
    val matrix = CloudMatrix()
    private var position = Vec2i(Int.MIN_VALUE)
    private val arrays: Array<CloudArray> = arrayOfNulls<CloudArray?>(4).unsafeCast()
    private var color: Vec3 = Vec3.EMPTY
    private var offset = 0.0f
    private var movement = true
    var height: IntRange = sky.properties.getCloudHeight(connection)
        private set

    override val skipOpaque: Boolean
        get() = !sky.properties.clouds || !sky.profile.clouds.enabled || connection.profiles.block.viewDistance < 3


    override fun asyncInit(latch: CountUpAndDownLatch) {
        matrix.load(connection.assetsManager)
    }

    override fun init(latch: CountUpAndDownLatch) {
        shader.load()
    }

    override fun postInit(latch: CountUpAndDownLatch) {
        sky.profile.clouds::movement.profileWatch(this, profile = connection.profiles.rendering) { this.movement = it }
        connection::state.observe(this) {
            if (it == PlayConnectionStates.SPAWNING) {
                // reset clouds
                position = Vec2i(Int.MIN_VALUE)
                height = sky.properties.getCloudHeight(connection)
            }
        }
    }

    private fun push(from: Int, to: Int, direction: Directions) {
        arrays[to].unload()
        arrays[to] = arrays[from]
        arrays[from] = CloudArray(this, arrays[from].offset + direction)
    }

    private fun reset(cloudPosition: Vec2i) {
        for (array in arrays.unsafeCast<Array<CloudArray?>>()) {
            array?.unload()
        }
        arrays[0] = CloudArray(this, cloudPosition + Vec2i(-1, -1))
        arrays[1] = CloudArray(this, cloudPosition + Vec2i(+0, -1))
        arrays[2] = CloudArray(this, cloudPosition + Vec2i(-1, +0))
        arrays[3] = CloudArray(this, cloudPosition + Vec2i(+0, +0))
    }

    private fun Vec2i.cloudPosition(): Vec2i {
        val position = this shr 4
        if (this.x and 0x0F >= 8) {
            position.x++
        }
        if (this.y and 0x0F >= 8) {
            position.y++
        }
        return position
    }

    private fun updatePosition() {
        val offset = this.offset.toInt()
        val position = connection.player.positionInfo.chunkPosition + Vec2i(offset / CloudArray.CLOUD_SIZE, 0)
        if (position == this.position) {
            return
        }

        val arrayDelta = position.cloudPosition() - this.position.cloudPosition()

        if (abs(arrayDelta.x) > 1 || abs(arrayDelta.y) > 1) {
            // major position change (e.g. teleport)
            reset(position shr 4)
        } else {
            // push array in our direction
            if (arrayDelta.x == -1) {
                push(from = 0, to = 1, Directions.WEST)
                push(from = 2, to = 3, Directions.WEST)
            } else if (arrayDelta.x == 1) {
                push(from = 1, to = 0, Directions.EAST)
                push(from = 3, to = 2, Directions.EAST)
            }
            if (arrayDelta.y == -1) {
                push(from = 0, to = 2, Directions.NORTH)
                push(from = 1, to = 3, Directions.NORTH)
            } else if (arrayDelta.y == 1) {
                push(from = 2, to = 0, Directions.SOUTH)
                push(from = 3, to = 1, Directions.SOUTH)
            }
        }

        this.position = position
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
        return 0.5f
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
