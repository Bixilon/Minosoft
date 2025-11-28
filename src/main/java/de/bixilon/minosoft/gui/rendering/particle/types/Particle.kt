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

package de.bixilon.minosoft.gui.rendering.particle.types

import de.bixilon.kmath.vec.vec3.d.MVec3d
import de.bixilon.kmath.vec.vec3.d.Vec3d
import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.kutil.time.TimeUtil
import de.bixilon.kutil.time.TimeUtil.now
import de.bixilon.minosoft.data.physics.PhysicsEntity
import de.bixilon.minosoft.data.registries.blocks.shapes.collision.context.ParticleCollisionContext
import de.bixilon.minosoft.data.registries.particle.data.ParticleData
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.data.registries.shapes.collision.CollisionShape
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.gui.rendering.particle.ParticleFactory
import de.bixilon.minosoft.gui.rendering.particle.mesh.ParticleMeshBuilder
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.blockPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.interpolateLinear
import de.bixilon.minosoft.physics.parts.CollisionMovementPhysics.collide
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.network.session.play.tick.TickUtil
import java.util.*
import kotlin.math.abs
import kotlin.reflect.full.companionObjectInstance
import kotlin.time.TimeSource.Monotonic.ValueTimeMark

abstract class Particle(
    protected val session: PlaySession,
    override var position: Vec3d,
    override val velocity: MVec3d = MVec3d(),
    data: ParticleData? = null,
) : PhysicsEntity {
    protected val data: ParticleData by lazy {
        data ?: let {
            val identifier = this::class.companionObjectInstance as ParticleFactory<*>
            session.registries.particleType[identifier]!!.default()
        }
    }
    var chunkPosition = position.blockPosition.chunkPosition
        private set
    protected val random = Random()
    private var lastTickTime = TimeUtil.NULL

    private var chunk: Chunk? = null
    private var chunkRevision = -1

    // ageing
    var dead = false
    var age: Int = 0
        protected set
    val floatAge: Float
        get() = age.toFloat()
    var maxAge: Int = (4.0f / (random.nextFloat() * 0.9f + 0.1f)).toInt()

    // moving
    val cameraPosition: Vec3d
        get() = getCameraPosition(now())

    var previousPosition = position
    var movement: Boolean = true
    var physics: Boolean = true
    var friction = 0.98f
    var gravityStrength = 0.0f

    // collisions
    override var onGround: Boolean = true
    var alreadyCollided = false
    var accelerateIfYBlocked = false
    var spacing: Vec3f = Vec3f.EMPTY

    val aabb: AABB
        get() {
            val spacing = spacing
            val position = position


            return AABB(Vec3d(position.x - spacing.x, position.y - spacing.y, position.z - spacing.z), Vec3d(position.x + spacing.x, position.y + spacing.y, position.z + spacing.z))
        }

    protected fun getChunk(): Chunk? {
        val revision = session.world.chunks.revision
        var chunk = this.chunk

        if (chunk != null) {
            if (chunk.position == this.chunkPosition) return chunk
            chunk = chunk.neighbours.traceChunk(chunkPosition - chunk.position)
        }
        if (chunk == null && revision != this.chunkRevision) {
            chunk = session.world.chunks[chunkPosition]
            this.chunk = chunk
            this.chunkRevision = revision
        }

        return chunk
    }


    init {
        this.velocity.x += (random.nextDouble() * 2.0 - 1.0) * MAGIC_VELOCITY_CONSTANT
        this.velocity.y += (random.nextDouble() * 2.0 - 1.0) * MAGIC_VELOCITY_CONSTANT
        this.velocity.z += (random.nextDouble() * 2.0 - 1.0) * MAGIC_VELOCITY_CONSTANT
        val modifier = (random.nextFloat() + random.nextFloat() + 1.0f) * 0.15
        val divider = this.velocity.length()

        this.velocity(this.velocity / divider * modifier * MAGIC_VELOCITY_CONSTANTf)
        this.velocity.y += 0.1

        spacing = DEFAULT_SPACING
    }

    fun getCameraPosition(time: ValueTimeMark): Vec3d {
        return interpolateLinear((time - lastTickTime) / TickUtil.TIME_PER_TICK, previousPosition, position)
    }

    open fun forceMove(delta: Vec3d = this.velocity.unsafe) {
        this.previousPosition = position
        position += delta
    }

    fun forceMove(move: () -> Double) {
        forceMove(Vec3d(move()))
    }

    private fun collide(movement: Vec3d): Vec3d {
        val aabb = aabb + movement

        val context = ParticleCollisionContext(this)
        val collisions = CollisionShape(session.world, context, aabb, movement, getChunk())
        val adjusted = collide(movement, aabb, collisions)
        if (adjusted.y != movement.y) {
            onGround = true
            adjusted.y = 0.0
        }
        if (adjusted.x != movement.x) {
            adjusted.x = 0.0
        }
        if (adjusted.z != movement.z) {
            adjusted.z = 0.0
        }


        return adjusted.unsafe
    }

    open fun move(velocity: Vec3d = this.velocity.unsafe) {
        if (alreadyCollided) {
            this.previousPosition = position
            return
        }
        val previousY = velocity.y
        if (this.physics && velocity != Vec3d.EMPTY) {
            velocity(collide(velocity))
        }

        forceMove(velocity)

        if (abs(velocity.y) >= Y_VELOCITY_TO_CHECK && abs(previousY) < Y_VELOCITY_TO_CHECK) {
            this.alreadyCollided = true
        }
    }

    private fun tryMove() {
        if (!movement) {
            return
        }

        forceMove()
    }

    open fun tryTick(time: ValueTimeMark) {
        if (dead) {
            return
        }

        if (lastTickTime == TimeUtil.NULL) {
            lastTickTime = time
            return
        }
        if (time - lastTickTime < TickUtil.INTERVAL) return
        tick()
        chunkPosition = position.blockPosition.chunkPosition

        if (dead) {
            return
        }

        tryMove()
        postTick()
        lastTickTime = time
    }

    protected fun age() {
        if (++age >= maxAge) {
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

    abstract fun addVertex(mesh: ParticleMeshBuilder, translucentMesh: ParticleMeshBuilder, time: ValueTimeMark)

    companion object {
        private const val MAGIC_VELOCITY_CONSTANT = 0.4
        private const val MAGIC_VELOCITY_CONSTANTf = MAGIC_VELOCITY_CONSTANT.toFloat()
        private const val Y_VELOCITY_TO_CHECK = 9.999999747378752E-6f
        private val DEFAULT_SPACING = Vec3f(0.2f)
    }
}
