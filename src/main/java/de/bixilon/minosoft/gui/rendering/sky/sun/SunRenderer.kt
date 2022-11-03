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

package de.bixilon.minosoft.gui.rendering.sky.sun

import de.bixilon.kotlinglm.func.rad
import de.bixilon.kotlinglm.mat4x4.Mat4
import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec4.Vec4
import de.bixilon.minosoft.data.world.time.DayPhases
import de.bixilon.minosoft.data.world.time.WorldTime
import de.bixilon.minosoft.gui.rendering.events.CameraMatrixChangeEvent
import de.bixilon.minosoft.gui.rendering.sky.SkyChildRenderer
import de.bixilon.minosoft.gui.rendering.sky.SkyRenderer
import de.bixilon.minosoft.gui.rendering.system.base.BlendingFunctions
import de.bixilon.minosoft.gui.rendering.system.base.RenderingCapabilities
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.minecraft
import de.bixilon.minosoft.util.KUtil.minosoft
import de.bixilon.minosoft.util.KUtil.murmur64
import java.util.*
import kotlin.math.pow

class SunRenderer(
    private val sky: SkyRenderer,
) : SkyChildRenderer {
    private val texture = sky.renderWindow.textureManager.staticTextures.createTexture(SUN)
    private val shader = sky.renderWindow.renderSystem.createShader(minosoft("weather/sun"))
    private var mesh = SunMesh(sky.renderWindow)
    private var day = -1L
    private var matrix = Mat4()
    private var matrixUpdate = true
    private var sunModifier = 0.0f

    override fun init() {
        shader.load()
    }

    private fun prepareMesh() {
        mesh.addYQuad(
            start = Vec2(-0.15f, -0.15f),
            y = 1f,
            end = Vec2(+0.15f, +0.15f),
            vertexConsumer = { position, uv ->
                mesh.addVertex(
                    position = position,
                    texture = texture,
                    uv = uv,
                )
            }
        )

        mesh.load()
    }

    override fun postInit() {
        prepareMesh()
        sky.renderWindow.textureManager.staticTextures.use(shader)
        sky.renderWindow.connection.events.listen<CameraMatrixChangeEvent> { calculateMatrix(it.projectionMatrix, it.viewMatrix) }
    }

    private fun getSunAngle(): Float {
        val time = sky.renderWindow.connection.world.time

        // 270: sunrise (23k-0k)
        // 0: day (0-12k)
        // 90: sunset (12k-13k)
        // 180: night (13k-23k)


        return ((time.time / ProtocolDefinition.TICKS_PER_DAYf) - 0.25f) * 360.0f
    }

    private fun calculateSunIntensity(): Float {
        val time = sky.renderWindow.connection.world.time
        return when (time.phase) {
            DayPhases.NIGHT -> 0.0f
            DayPhases.DAY -> 1.0f
            DayPhases.SUNSET -> (1.0f - time.progress).pow(2)
            DayPhases.SUNRISE -> time.progress.pow(2)
        }
    }

    private fun calculateMatrix(projection: Mat4 = sky.renderWindow.camera.matrixHandler.projectionMatrix, view: Mat4 = sky.renderWindow.camera.matrixHandler.viewMatrix) {
        val matrix = projection * view.toMat3().toMat4()

        matrix.rotateAssign(getSunAngle().rad, Vec3(0, 0, -1))
        matrix.translateAssign(Vec3(0.0f, -0.01f, 0.0f)) // prevents face fighting

        matrix.translateAssign(Vec3(0.0f, -sunModifier, 0.0f)) // moves the sun closer to the player based on the day (sun appears bigger)


        this.matrix = matrix
        this.matrixUpdate = true
    }

    override fun onTimeUpdate(time: WorldTime) {
        if (this.day != time.day) {
            this.day = time.day
            sunModifier = Random(day.murmur64()).nextFloat(0.0f, 0.2f)
        }
    }

    override fun draw() {
        shader.use()
        calculateMatrix()
        if (matrixUpdate) {
            shader.setMat4("uSunMatrix", matrix)
            shader.setVec4("uTintColor", Vec4(1.0f, 1.0f, 1.0f, calculateSunIntensity()))
            this.matrixUpdate = false
        }

        sky.renderSystem.enable(RenderingCapabilities.BLENDING)
        sky.renderSystem.setBlendFunction(BlendingFunctions.SOURCE_ALPHA, BlendingFunctions.ONE, BlendingFunctions.ONE, BlendingFunctions.ZERO)

        mesh.unload()
        mesh = SunMesh(sky.renderWindow)
        prepareMesh()
        mesh.draw()
        sky.renderSystem.resetBlending()
    }

    companion object {
        private val SUN = minecraft("environment/sun").texture()
    }
}
