/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.particle.types

import de.bixilon.minosoft.data.mappings.particle.data.ParticleData
import de.bixilon.minosoft.data.text.ChatColors
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.particle.ParticleMesh
import de.bixilon.minosoft.gui.rendering.util.VecUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.VecUtil.ONE
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import glm_.vec3.Vec3
import kotlin.math.abs
import kotlin.random.Random

abstract class Particle(protected val connection: PlayConnection, protected val position: Vec3, protected val data: ParticleData, protected val random: Random) {
    protected val texture = connection.rendering!!.renderWindow.textures.allTextures[data.type.textures.random()]!!
    protected var scale: Float = 0.1f
    protected var color: RGBColor = ChatColors.WHITE

    private var lastTickTime = -1L

    // growing
    protected var nextScale: Float = scale
    protected var scalePerMillisecond = -1.0f

    // ageing
    var dead = false
    var age: Int = 0
        protected set
    var maxAge: Int = 100000 + random.nextInt(0, 10000)

    // moving
    var friction = Vec3.EMPTY
    var velocity = Vec3.EMPTY

    // hover
    protected var hovering = false
    protected var hoverMinY = 0.0f
    protected var hoverMaxY = 0.0f


    fun grow(scale: Float, time: Long) {
        nextScale = scale
        scalePerMillisecond = (scale - this.scale) / time
    }

    private fun grow(deltaTime: Int) {
        val deltaScale = nextScale - scale
        if (abs(deltaScale) > GROW_LOWER_LIMIT) {
            // we need to grow
            val scaleAdd = scalePerMillisecond * deltaTime

            // checke if the delta gets bigger (aka. we'd grew to much)
            val nextScale = scale + scaleAdd
            if (abs(this.nextScale - nextScale) > deltaScale) {
                // abort scaling and avoid getting called another time
                scale = nextScale
                return
            }
            // we can grow
            scale = nextScale
        }
    }

    private fun move(deltaTime: Int) {
        val perSecond = deltaTime / 1000.0f
        position += velocity * perSecond
        velocity = velocity * (Vec3.ONE - friction * perSecond)

        if (velocity.length() < MINIMUM_VELOCITY) {
            velocity = Vec3.EMPTY
        }
    }


    fun hover(minY: Float, maxY: Float) {
        check(maxY >= minY) { "Maximum y can not be smaller than minimum!" }
        hoverMinY = minY
        hoverMaxY = maxY
        hovering = true
    }

    fun relativeHover(minY: Float, maxY: Float) {
        hover(position.y + minY, position.y + maxY)
    }

    private fun hover(deltaTime: Int) {
        // ToDo: Maybe implement this sometimes later, not time for it now
        if (!hovering) {
            return
        }
        val distanceToMiddle = if (velocity.y <= 0) {
            position.y - hoverMinY
        } else {
            hoverMaxY - position.y
        }
        val totalDistance = hoverMaxY - hoverMinY
        val yVelocity = 1 / (totalDistance / distanceToMiddle)


        when {
            position.y <= hoverMinY -> {
                // change direction: up
                velocity.y = 1.0f
            }
            position.y >= hoverMaxY || velocity.y == 0.0f -> {
                // change direction: down
                velocity.y = -1.0f
            }
            else -> {
                friction.y = yVelocity
            }
        }
    }

    open fun tick() {
        check(!dead) { "Cannot tick dead particle!" }
        val currentTime = System.currentTimeMillis()
        if (lastTickTime == -1L) {
            // never ticked before, skip
            lastTickTime = currentTime
            return
        }
        val deltaTime = (currentTime - lastTickTime).toInt()
        check(deltaTime >= 0)

        age += deltaTime

        if (age >= maxAge) {
            dead = true
            return
        }

        grow(deltaTime)
        move(deltaTime)
        hover(deltaTime)

        lastTickTime = currentTime
    }

    open fun addVertex(particleMesh: ParticleMesh) {
        particleMesh.addVertex(position, scale, texture, color)
    }

    companion object {
        const val GROW_LOWER_LIMIT = 0.001f
        const val MINIMUM_VELOCITY = 0.01f
    }

}
