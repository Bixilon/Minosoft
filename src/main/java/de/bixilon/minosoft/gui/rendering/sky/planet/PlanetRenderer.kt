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

package de.bixilon.minosoft.gui.rendering.sky.planet

import de.bixilon.kmath.mat.mat4.f.MMat4f
import de.bixilon.kmath.mat.mat4.f.Mat4f
import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kmath.vec.vec4.f.Vec4f
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.primitive.FloatUtil.rad
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.data.world.time.WorldTime
import de.bixilon.minosoft.gui.rendering.sky.SkyChildRenderer
import de.bixilon.minosoft.gui.rendering.sky.SkyRenderer
import de.bixilon.minosoft.gui.rendering.system.base.BlendingFunctions
import de.bixilon.minosoft.gui.rendering.system.base.RenderingCapabilities
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture

abstract class PlanetRenderer(
    protected val sky: SkyRenderer,
) : SkyChildRenderer {
    protected abstract val texture: Texture
    protected val shader = sky.context.system.shader.create(minosoft("sky/planet")) { PlanetShader(it) }
    private var mesh = PlanetMesh(sky.context)
    protected var day = -1L
    protected var matrix = Mat4f()
    private var matrixUpdate = true
    protected var modifier = 0.0f
    protected var intensity = -1.0f
    protected var meshInvalid = false

    open var uvStart = Vec2f(0.0f, 0.0f)
    open var uvEnd = Vec2f(1.0f, 1.0f)

    private fun prepareMesh() {
        mesh.addYQuad(
            start = Vec2f(-0.2f, -0.2f),
            y = 1f,
            end = Vec2f(+0.2f, +0.2f),
            uvStart = uvStart,
            uvEnd = uvEnd,
            vertexConsumer = { position, uv ->
                mesh.addVertex(
                    position = position,
                    texture = texture,
                    uv = uv,
                )
            }
        )

        mesh.load()
        this.meshInvalid = false
    }

    override fun postInit() {
        shader.load()
        prepareMesh()
        sky::matrix.observe(this) { calculateMatrix(it) }
    }

    protected abstract fun calculateAngle(): Float
    protected abstract fun calculateIntensity(): Float


    private fun calculateMatrix(base: Mat4f) {
        val matrix = MMat4f(base)

        matrix.apply {
            rotateZAssign(calculateAngle().rad)
            translateYAssign(-modifier) // moves the planet closer to the player (appears bigger)
        }


        this.matrix = matrix.unsafe
        this.matrixUpdate = true
    }

    protected abstract fun calculateModifier(day: Long): Float

    override fun onTimeUpdate(time: WorldTime) {
        if (this.day != time.day) {
            this.day = time.day
            modifier = calculateModifier(time.day)
        }
        calculateMatrix(sky.matrix)
    }

    override fun draw() {
        val weather = sky.session.world.weather
        if (weather.rain > 0.8f || weather.thunder > 0.8f) {
            // sky not clear
            return
        }
        shader.use()
        if (matrixUpdate) {
            shader.matrix = matrix

            val intensity = calculateIntensity()
            if (this.intensity != intensity) {
                shader.tintColor = Vec4f(1.0f, 1.0f, 1.0f, intensity)
                this.intensity = intensity
            }
            this.matrixUpdate = false
        }
        if (meshInvalid) {
            this.mesh.unload()
            this.mesh = PlanetMesh(sky.context)
            prepareMesh()
        }

        val system = sky.context.system
        system.enable(RenderingCapabilities.BLENDING)
        system.setBlendFunction(BlendingFunctions.SOURCE_ALPHA, BlendingFunctions.ONE, BlendingFunctions.ONE, BlendingFunctions.ZERO)

        mesh.draw()
        system.resetBlending()
    }
}
