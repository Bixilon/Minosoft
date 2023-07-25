/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.particle

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kutil.concurrent.lock.simple.SimpleLock
import de.bixilon.kutil.concurrent.schedule.RepeatedTask
import de.bixilon.kutil.concurrent.schedule.TaskScheduler
import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.time.TimeUtil.millis
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.data.world.particle.AbstractParticleRenderer
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.events.CameraMatrixChangeEvent
import de.bixilon.minosoft.gui.rendering.particle.types.Particle
import de.bixilon.minosoft.gui.rendering.renderer.renderer.AsyncRenderer
import de.bixilon.minosoft.gui.rendering.renderer.renderer.RendererBuilder
import de.bixilon.minosoft.gui.rendering.system.base.RenderSystem
import de.bixilon.minosoft.gui.rendering.system.base.phases.SkipAll
import de.bixilon.minosoft.gui.rendering.system.base.phases.TranslucentDrawable
import de.bixilon.minosoft.gui.rendering.system.base.phases.TransparentDrawable
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnectionStates
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnectionStates.Companion.disconnected
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.chunk.ChunkUtil.isInViewDistance
import de.bixilon.minosoft.util.collections.floats.BufferedArrayFloatList


class ParticleRenderer(
    private val connection: PlayConnection,
    override val context: RenderContext,
) : AsyncRenderer, TransparentDrawable, TranslucentDrawable, SkipAll, AbstractParticleRenderer {
    override val renderSystem: RenderSystem = context.system
    private val profile = connection.profiles.particle
    private val transparentShader = renderSystem.createShader(minosoft("particle")) { ParticleShader(it, true) }
    private val translucentShader = renderSystem.createShader(minosoft("particle")) { ParticleShader(it, false) }

    // There is no opaque mesh because it is simply not needed (every particle has transparency)
    private var transparentMesh = ParticleMesh(context, BufferedArrayFloatList(MAXIMUM_AMOUNT * ParticleMesh.ParticleMeshStruct.FLOATS_PER_VERTEX))
    private var translucentMesh = ParticleMesh(context, BufferedArrayFloatList(MAXIMUM_AMOUNT * ParticleMesh.ParticleMeshStruct.FLOATS_PER_VERTEX))

    private val particlesLock = SimpleLock()
    private var particles: MutableList<Particle> = mutableListOf()
    private var particleQueueLock = SimpleLock()
    private var particleQueue: MutableList<Particle> = mutableListOf()
    private var matrixUpdate = true


    private lateinit var particleTask: RepeatedTask

    override val skipAll: Boolean
        get() = !enabled


    private var enabled = true
        set(value) {
            if (!value) {
                particlesLock.lock()
                particles.clear()
                particlesLock.unlock()

                particleQueueLock.lock()
                particleQueue.clear()
                particleQueueLock.unlock()
            }
            field = value
        }
    private var maxAmount = MAXIMUM_AMOUNT
        set(value) {
            check(value > 1) { "Can not have negative particle max amount" }
            particlesLock.lock()
            while (particles.size > value) {
                particles.removeAt(0)
            }
            val particlesSize = particles.size
            particlesLock.unlock()
            particleQueueLock.lock()
            while (particlesSize + particleQueue.size > value) {
                particleQueue.removeAt(0)
            }
            particleQueueLock.unlock()
            field = value
        }

    val size: Int
        get() = particles.size

    override fun init(latch: AbstractLatch) {
        profile::maxAmount.observe(this, true) { maxAmount = minOf(it, MAXIMUM_AMOUNT) }
        profile::enabled.observe(this, true) { enabled = it }

        connection.events.listen<CameraMatrixChangeEvent> {
            matrixUpdate = true
        }

        transparentMesh.load()
        translucentMesh.load()
        for (particle in connection.registries.particleType) {
            for (resourceLocation in particle.textures) {
                context.textures.staticTextures.createTexture(resourceLocation)
            }
        }

        DefaultParticleBehavior.register(connection, this)
    }

    override fun postInit(latch: AbstractLatch) {
        transparentShader.load()
        translucentShader.load()

        connection.world.particleRenderer = this

        particleTask = RepeatedTask(ProtocolDefinition.TICK_TIME, maxDelay = ProtocolDefinition.TICK_TIME / 2) {
            if (!context.state.running || !enabled || connection.state != PlayConnectionStates.PLAYING) {
                return@RepeatedTask
            }
            val cameraPosition = connection.player.physics.positionInfo.chunkPosition
            val particleViewDistance = connection.world.view.particleViewDistance


            particlesLock.lock()
            try {
                val time = millis()
                val iterator = particles.iterator()
                for (particle in iterator) {
                    if (!particle.chunkPosition.isInViewDistance(particleViewDistance, cameraPosition)) { // ToDo: Check fog distance
                        particle.dead = true
                        iterator.remove()
                    } else if (particle.dead) {
                        iterator.remove()
                    } else {
                        particle.tryTick(time)
                    }
                }

                particleQueueLock.lock()
                particles += particleQueue
                particleQueue.clear()
                particleQueueLock.unlock()
            } finally {
                particlesLock.unlock()
            }
        }
        TaskScheduler += particleTask

        connection::state.observe(this) {
            if (!it.disconnected) {
                return@observe
            }
            TaskScheduler -= particleTask
        }
    }

    override fun addParticle(particle: Particle) {
        if (!context.state.running || !enabled) {
            return
        }
        val particleCount = particles.size + particleQueue.size
        if (particleCount >= maxAmount) {
            return
        }

        if (!particle.chunkPosition.isInViewDistance(connection.world.view.particleViewDistance, connection.player.physics.positionInfo.chunkPosition)) {
            particle.dead = true
            return
        }
        particle.tryTick(millis())

        particleQueueLock.lock()
        particleQueue += particle
        particleQueueLock.unlock()
    }

    private fun updateShaders() {
        val matrix = context.camera.matrixHandler.viewProjectionMatrix
        val cameraRight = Vec3(matrix[0][0], matrix[1][0], matrix[2][0])
        val cameraUp = Vec3(matrix[0][1], matrix[1][1], matrix[2][1])

        transparentShader.cameraRight = cameraRight
        transparentShader.cameraUp = cameraUp

        translucentShader.cameraRight = cameraRight
        translucentShader.cameraUp = cameraUp
    }

    override fun prePrepareDraw() {
        if (matrixUpdate) {
            updateShaders()
            matrixUpdate = false
        }
        transparentMesh.unload()
        translucentMesh.unload()
    }

    override fun prepareDrawAsync() {
        transparentMesh.data.clear()
        translucentMesh.data.clear()
        transparentMesh = ParticleMesh(context, transparentMesh.data)
        translucentMesh = ParticleMesh(context, translucentMesh.data)

        particlesLock.acquire()

        val start = millis()
        var time = start
        for ((index, particle) in particles.withIndex()) {
            particle.tryTick(time)
            if (particle.dead) {
                continue
            }
            particle.addVertex(transparentMesh, translucentMesh, time)

            if (index % 1000 == 0) {
                time = millis()
                if (time - start > MAX_FRAME_TIME) {
                    // particles are heavily lagging out the game, reducing it.
                    // TODO: introduce slow particle mode and optimize out slow particles
                    break
                }
            }
        }

        particlesLock.release()
    }

    override fun postPrepareDraw() {
        transparentMesh.load()
        translucentMesh.load()
    }

    override fun setupTransparent() {
        super.setupTransparent()
        transparentShader.use()
    }

    override fun drawTransparent() {
        transparentMesh.draw()
    }

    override fun setupTranslucent() {
        super.setupTranslucent()
        translucentShader.use()
    }

    override fun drawTranslucent() {
        translucentMesh.draw()
    }

    override fun removeAllParticles() {
        particlesLock.lock()
        particles.clear()
        particlesLock.unlock()
        particleQueueLock.lock()
        particleQueue.clear()
        particleQueueLock.unlock()
    }


    companion object : RendererBuilder<ParticleRenderer> {
        const val MAXIMUM_AMOUNT = 50000
        const val MAX_FRAME_TIME = 5

        override fun build(connection: PlayConnection, context: RenderContext): ParticleRenderer? {
            if (connection.profiles.particle.skipLoading) {
                return null
            }
            return ParticleRenderer(connection, context)
        }
    }
}
