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
import de.bixilon.minosoft.gui.rendering.chunk.models.AABB
import de.bixilon.minosoft.gui.rendering.particle.ParticleMesh
import de.bixilon.minosoft.gui.rendering.particle.ParticleRenderer
import de.bixilon.minosoft.gui.rendering.util.VecUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.VecUtil.assign
import de.bixilon.minosoft.gui.rendering.util.VecUtil.millis
import de.bixilon.minosoft.gui.rendering.util.VecUtil.plusAssign
import de.bixilon.minosoft.gui.rendering.util.VecUtil.sqr
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.millis
import glm_.vec3.Vec3
import kotlin.math.sqrt
import kotlin.random.Random

abstract class Particle(
    protected val connection: PlayConnection,
    protected val particleRenderer: ParticleRenderer,
    protected val position: Vec3,
    protected val velocity: Vec3 = Vec3.EMPTY,
    protected val data: ParticleData,
) {
    protected val random = Random
    var lastTickTime = -1L

    // ageing
    var dead = false
    var age: Int = 0
        protected set
    var maxAge: Int = (4.0f / (random.nextFloat() * 0.9f + 0.1f)).millis

    // moving
    var previousPosition = position
    var movement: Boolean = true
    var physics: Boolean = true
    var friction = 0.98f
    var gravityStrength = 0.0f

    // collisions
    var onGround: Boolean = true
    var accelerateIfYBlocked = false
    var aabb: AABB = AABB.EMPTY
    var spacing: Vec3 = Vec3.EMPTY
        set(value) {
            if (field == value) {
                return
            }
            field = value


            val x = ((aabb.min.x + aabb.max.x) - spacing.x) / 2.0f
            val z = ((aabb.min.z + aabb.max.z) - spacing.z) / 2.0f

            aabb = AABB(Vec3(x, aabb.min.y, z), Vec3(x + spacing.x, aabb.min.y + spacing.y, z + spacing.z))
        }


    private var lastRealTickTime = -1L


    init {
        velocity += { (random.nextFloat() * 2.0f - 1.0f) * MAGIC_VELOCITY_CONSTANTf }
        val modifier = (random.nextFloat() + random.nextFloat() + 1.0f) * 0.15000000596046448f
        val divider = sqrt((velocity.x.sqr + velocity.y.sqr + velocity.z.sqr).toDouble()).toFloat()

        velocity assign velocity / divider * modifier * MAGIC_VELOCITY_CONSTANTf
        velocity.y += 0.10000000149011612f

        spacing = Vec3(0.2f)
    }

    fun move(velocity: Vec3) {
        var newVelocity = Vec3(velocity)
        if (this.physics && newVelocity != Vec3.EMPTY) {
            val aabb = aabb + position
            val collisions = connection.collisionDetector.getCollisionsToCheck(newVelocity, aabb)
            newVelocity = connection.collisionDetector.collide(null, newVelocity, collisions, aabb)
        }

        if (newVelocity != Vec3.EMPTY) {
            position += newVelocity
        }

        onGround = (newVelocity.y != velocity.y) && velocity.y < 0.0f

        if (newVelocity.x != velocity.x) {
            this.velocity.x = 0.0f
        }
        if (newVelocity.z != velocity.z) {
            this.velocity.z = 0.0f
        }
    }

    private fun move(deltaTime: Int) {
        if (!movement) {
            return
        }
        previousPosition = Vec3(position)

        velocity.y -= (0.04f * gravityStrength).millis

        move(velocity.millis * (deltaTime / 1000.0f))
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

        if (dead) {
            return
        }

        if (lastRealTickTime == -1L) {
            lastRealTickTime = System.currentTimeMillis()
        } else if (currentTime - lastRealTickTime >= ProtocolDefinition.TICK_TIME) {
            realTick()
            lastRealTickTime = currentTime
        }

        postTick(deltaTime)
        lastTickTime = currentTime
    }

    protected fun age(deltaTime: Int) {
        age += deltaTime

        if (age >= maxAge) {
            dead = true
        }
    }

    open fun tick(deltaTime: Int) {
        check(!dead) { "Cannot tick dead particle!" }
        check(deltaTime >= 0)

        age(deltaTime)
        if (dead) {
            return
        }
    }

    open fun postTick(deltaTime: Int) {
        move(deltaTime)
    }

    open fun realTick() {
        if (!movement) {
            return
        }

        if (accelerateIfYBlocked && position.y == previousPosition.y) {
            velocity.x *= 1.1f
            velocity.z *= 1.1f
        }

        velocity *= friction

        if (onGround) {
            velocity.x *= 0.699999988079071f
            velocity.y *= 0.699999988079071f
        }
    }

    abstract fun addVertex(particleMesh: ParticleMesh)

    companion object {
        private const val MINIMUM_VELOCITY = 0.01f
        private const val MAGIC_VELOCITY_CONSTANT = 0.4000000059604645
        private const val MAGIC_VELOCITY_CONSTANTf = MAGIC_VELOCITY_CONSTANT.toFloat()
    }
}
