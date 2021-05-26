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
import de.bixilon.minosoft.gui.rendering.particle.ParticleMesh
import de.bixilon.minosoft.gui.rendering.particle.ParticleRenderer
import de.bixilon.minosoft.gui.rendering.util.VecUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.VecUtil.ONE
import de.bixilon.minosoft.gui.rendering.util.VecUtil.clear
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import glm_.vec3.Vec3
import kotlin.random.Random

abstract class Particle(protected val connection: PlayConnection, protected val particleRenderer: ParticleRenderer, protected val position: Vec3, protected val data: ParticleData) {
    protected val random = Random
    private var lastTickTime = -1L


    // ageing
    var dead = false
    var age: Int = 0
        protected set
    var tickAge: Int
        get() = age / ProtocolDefinition.TICK_TIME
        set(value) {
            age = value * ProtocolDefinition.TICK_TIME
        }
    var maxAge: Int = Integer.MAX_VALUE
    var maxTickAge: Int
        get() = maxAge / ProtocolDefinition.TICK_TIME
        set(value) {
            maxAge = value * ProtocolDefinition.TICK_TIME
        }

    // moving
    val friction = Vec3.EMPTY
    val velocity = Vec3.EMPTY

    // hover
    protected var hovering = false
    protected var hoverMinY = 0.0f
    protected var hoverMaxY = 0.0f


    protected var lastRealTickTime = 0L


    private fun move(deltaTime: Int) {
        val perSecond = deltaTime / 1000.0f
        position += velocity * perSecond
        velocity *= Vec3.ONE - friction * perSecond

        if (velocity.length() < MINIMUM_VELOCITY) {
            velocity.clear()
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
        val yFriction = 1 / (totalDistance / distanceToMiddle)


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
                friction.y = yFriction
            }
        }
    }


    fun tick() {
        val currentTime = System.currentTimeMillis()
        if (lastTickTime == -1L) {
            // never ticked before, skip
            lastTickTime = currentTime
            return
        }
        val deltaTime = (currentTime - lastTickTime).toInt()

        tick(deltaTime)

        if (lastRealTickTime == -1L) {
            lastRealTickTime = System.currentTimeMillis()
        } else if (currentTime - lastRealTickTime >= ProtocolDefinition.TICK_TIME) {
            realTick()
            lastRealTickTime = currentTime
        }

        lastTickTime = currentTime
    }

    open fun tick(deltaTime: Int) {
        check(!dead) { "Cannot tick dead particle!" }
        check(deltaTime >= 0)
        age += deltaTime

        if (age >= maxAge) {
            dead = true
            return
        }

        move(deltaTime)
        hover(deltaTime)
    }

    open fun realTick() {

    }

    abstract fun addVertex(particleMesh: ParticleMesh)

    companion object {
        const val MINIMUM_VELOCITY = 0.01f
    }
}
