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

import de.bixilon.minosoft.data.physics.PhysicsEntity
import de.bixilon.minosoft.data.registries.AABB
import de.bixilon.minosoft.data.registries.particle.data.ParticleData
import de.bixilon.minosoft.gui.rendering.particle.ParticleFactory
import de.bixilon.minosoft.gui.rendering.particle.ParticleMesh
import de.bixilon.minosoft.gui.rendering.util.VecUtil
import de.bixilon.minosoft.gui.rendering.util.VecUtil.assign
import de.bixilon.minosoft.gui.rendering.util.VecUtil.plusAssign
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil
import glm_.vec3.Vec3
import glm_.vec3.Vec3d
import kotlin.math.abs
import kotlin.random.Random
import kotlin.reflect.full.companionObjectInstance

abstract class Particle(
    protected val connection: PlayConnection,
    final override val position: Vec3d,
    velocity: Vec3d = Vec3d.EMPTY,
    data: ParticleData? = null,
) : PhysicsEntity {
    protected val data: ParticleData = data ?: let {
        val resourceLocation = this::class.companionObjectInstance as ParticleFactory<*>
        connection.registries.particleTypeRegistry[resourceLocation]!!.default()
    }
    protected val random = Random
    private var lastTickTime = -1L

    // ageing
    var dead = false
    var age: Int = 0
        protected set
    val floatAge: Float
        get() = age.toFloat()
    var maxAge: Int = (4.0f / (random.nextFloat() * 0.9f + 0.1f)).toInt()

    // moving
    val cameraPosition: Vec3d
        get() = getCameraPosition(KUtil.time)

    final override val velocity: Vec3d = Vec3d(velocity)
    var previousPosition = position
    var movement: Boolean = true
    var physics: Boolean = true
    var friction = 0.98f
    var gravityStrength = 0.0f

    // collisions
    override var onGround: Boolean = true
    var alreadyCollided = false
    var accelerateIfYBlocked = false
    override var aabb: AABB = AABB.EMPTY
    var spacing: Vec3 = Vec3.EMPTY
        set(value) {
            if (field == value) {
                return
            }
            field = value

            val x = ((aabb.min.x + aabb.max.x) - spacing.x) / 2.0
            val z = ((aabb.min.z + aabb.max.z) - spacing.z) / 2.0

            aabb = AABB(Vec3(x, aabb.min.y, z), Vec3(x + spacing.x, aabb.min.y + spacing.y, z + spacing.z))
        }


    init {
        this.velocity += { (random.nextDouble() * 2.0 - 1.0) * MAGIC_VELOCITY_CONSTANT }
        val modifier = (random.nextFloat() + random.nextFloat() + 1.0f) * 0.15
        val divider = this.velocity.length()

        this.velocity assign this.velocity / divider * modifier * MAGIC_VELOCITY_CONSTANTf
        this.velocity.y += 0.1

        spacing = Vec3(0.2)
    }

    fun getCameraPosition(time: Long): Vec3d {
        return VecUtil.lerp((time - lastTickTime) / ProtocolDefinition.TICK_TIMEd, previousPosition, position)
    }

    fun forceMove(delta: Vec3d) {
        this.previousPosition = Vec3d(position)
        position += delta
    }

    fun forceMove(move: () -> Double) {
        this.previousPosition = Vec3d(position)
        position += move
    }

    open fun move(velocity: Vec3d) {
        if (alreadyCollided) {
            this.previousPosition = Vec3d(position)
            return
        }
        var newVelocity = Vec3d(velocity)
        if (this.physics && newVelocity != Vec3d.EMPTY) {
            val aabb = aabb + position
            newVelocity = connection.collisionDetector.collide(this, newVelocity, aabb)
        }

        forceMove(newVelocity)

        if (abs(newVelocity.y) >= Y_VELOCITY_TO_CHECK && abs(velocity.y) < Y_VELOCITY_TO_CHECK) {
            this.alreadyCollided = true
        }
    }

    private fun move() {
        if (!movement) {
            return
        }

        forceMove(velocity)
    }

    fun tryTick(time: Long) {
        if (dead) {
            return
        }

        if (lastTickTime == -1L) {
            lastTickTime = time
            return
        }
        if (time - lastTickTime < ProtocolDefinition.TICK_TIME) {
            return
        }
        tick()

        if (dead) {
            return
        }

        move()
        postTick()
        lastTickTime = time
    }

    protected fun age() {
        if (age++ >= maxAge) {
            dead = true
        }
    }

    open fun postTick() = Unit

    open fun tick() {
        age()
        if (dead) {
            return
        }
        if (!movement) {
            return
        }

        velocity.y -= 0.04f * gravityStrength

        if (accelerateIfYBlocked && position.y == previousPosition.y) {
            velocity.x *= 1.1f
            velocity.z *= 1.1f
        }

        velocity *= friction

        if (onGround) {
            velocity.x *= 0.699999988079071f
            velocity.z *= 0.699999988079071f
        }
    }

    abstract fun addVertex(transparentMesh: ParticleMesh, particleMesh: ParticleMesh, time:Long)

    companion object {
        private const val MAGIC_VELOCITY_CONSTANT = 0.4
        private const val MAGIC_VELOCITY_CONSTANTf = MAGIC_VELOCITY_CONSTANT.toFloat()
        private const val Y_VELOCITY_TO_CHECK = 9.999999747378752E-6f
    }
}
