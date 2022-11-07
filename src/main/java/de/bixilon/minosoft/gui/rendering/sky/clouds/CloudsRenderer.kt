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
import de.bixilon.kotlinglm.vec4.Vec4
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.renderer.renderer.Renderer
import de.bixilon.minosoft.gui.rendering.renderer.renderer.RendererBuilder
import de.bixilon.minosoft.gui.rendering.sky.SkyRenderer
import de.bixilon.minosoft.gui.rendering.system.base.RenderSystem
import de.bixilon.minosoft.gui.rendering.system.base.RenderingCapabilities
import de.bixilon.minosoft.gui.rendering.system.base.phases.TranslucentDrawable
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.readTexture
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import de.bixilon.minosoft.gui.rendering.util.VecUtil.plus
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.minecraft
import de.bixilon.minosoft.util.KUtil.minosoft
import java.util.*
import kotlin.math.abs

class CloudsRenderer(
    private val sky: SkyRenderer,
    private val connection: PlayConnection,
    override val renderWindow: RenderWindow,
) : Renderer, TranslucentDrawable {
    override val renderSystem: RenderSystem = renderWindow.renderSystem
    private val shader = renderSystem.createShader(minosoft("sky/clouds"))
    val cloudMatrix = BitSet(CLOUD_MATRIX_SIZE * CLOUD_MATRIX_SIZE)
    private var position = Vec2i(Int.MIN_VALUE)
    private val arrays: Array<CloudArray> = arrayOfNulls<CloudArray?>(4).unsafeCast()


    override val skipTranslucent: Boolean
        get() = !sky.properties.clouds || !sky.profile.clouds || connection.profiles.block.viewDistance < 3

    private fun loadCloudMatrix() {
        val data = sky.renderWindow.connection.assetsManager[CLOUD_MATRIX].readTexture()

        if (data.size.x != CLOUD_MATRIX_SIZE || data.size.y != CLOUD_MATRIX_SIZE) {
            throw IllegalStateException("Cloud matrix has invalid size: ${data.size}")
        }

        for (i in 0 until CLOUD_MATRIX_SIZE * CLOUD_MATRIX_SIZE) {
            cloudMatrix[i] = data.buffer.getInt(i * 4) ushr 24 == 0xFF
        }
    }

    override fun asyncInit(latch: CountUpAndDownLatch) {
        loadCloudMatrix()
    }

    override fun init(latch: CountUpAndDownLatch) {
        shader.load()
    }

    private fun push(from: Int, to: Int, direction: Directions) {
        arrays[to].unload()
        arrays[to] = arrays[from]
        arrays[from] = CloudArray(this, arrays[from].offset + direction)
    }

    private fun reset() {
        // TODO: unload previous
        val position = connection.player.positionInfo.chunkPosition
        val cloudPosition = position shr 4
        arrays[0] = CloudArray(this, cloudPosition)
        arrays[1] = CloudArray(this, cloudPosition + Vec2i(1, 0))
        arrays[2] = CloudArray(this, cloudPosition + Vec2i(0, 1))
        arrays[3] = CloudArray(this, cloudPosition + Vec2i(1, 1))
    }


    override fun postPrepareDraw() {
        val position = connection.player.positionInfo.chunkPosition
        if (position == this.position) {
            return
        }
        val cloudPosition = position shr 4
        val arrayDelta = cloudPosition - (this.position shr 4)

        if (abs(arrayDelta.x) > 1 || abs(arrayDelta.y) > 1) {
            // major position change (e.g. teleport)
            reset()
        } else {
            // push array in our direction
            if (arrayDelta.x == -1) {
                push(from = 1, to = 0, Directions.WEST)
                push(from = 3, to = 2, Directions.WEST)
            } else if (arrayDelta.x == 1) {
                push(from = 0, to = 1, Directions.EAST)
                push(from = 2, to = 3, Directions.EAST)
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

    override fun setupTranslucent() {
        super.setupTranslucent()
        renderSystem.disable(RenderingCapabilities.FACE_CULLING)
    }

    override fun drawTranslucent() {
        shader.use()
        shader.setVec4("uCloudsColor", Vec4(1.0f, 0.5, 1.0f, 1.0f))

        for (array in arrays) {
            array.draw()
        }
    }

    companion object : RendererBuilder<CloudsRenderer> {
        override val RESOURCE_LOCATION = ResourceLocation("minosoft:clouds")
        private val CLOUD_MATRIX = minecraft("environment/clouds").texture()
        const val CLOUD_MATRIX_SIZE = 256
        const val CLOUD_MATRIX_MASK = 0xFF


        override fun build(connection: PlayConnection, renderWindow: RenderWindow): CloudsRenderer? {
            val sky = renderWindow.renderer[SkyRenderer] ?: return null
            return CloudsRenderer(sky, connection, renderWindow)
        }
    }
}
