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

package de.bixilon.minosoft.gui.rendering.particle

import de.bixilon.kotlinglm.mat4x4.Mat4
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kutil.concurrent.lock.simple.SimpleLock
import de.bixilon.kutil.concurrent.time.TimeWorker
import de.bixilon.kutil.concurrent.time.TimeWorkerTask
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.kutil.time.TimeUtil.millis
import de.bixilon.kutil.watcher.DataWatcher.Companion.observe
import de.bixilon.minosoft.config.profile.delegate.watcher.SimpleProfileDelegateWatcher.Companion.profileWatch
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.world.particle.AbstractParticleRenderer
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.modding.events.CameraMatrixChangeEvent
import de.bixilon.minosoft.gui.rendering.particle.types.Particle
import de.bixilon.minosoft.gui.rendering.renderer.renderer.AsyncRenderer
import de.bixilon.minosoft.gui.rendering.renderer.renderer.RendererBuilder
import de.bixilon.minosoft.gui.rendering.system.base.RenderSystem
import de.bixilon.minosoft.gui.rendering.system.base.phases.SkipAll
import de.bixilon.minosoft.gui.rendering.system.base.phases.TranslucentDrawable
import de.bixilon.minosoft.gui.rendering.system.base.phases.TransparentDrawable
import de.bixilon.minosoft.gui.rendering.system.base.shader.Shader
import de.bixilon.minosoft.gui.rendering.system.base.shader.ShaderUniforms
import de.bixilon.minosoft.modding.event.invoker.CallbackEventInvoker
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnectionStates
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnectionStates.Companion.disconnected
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.minosoft
import de.bixilon.minosoft.util.chunk.ChunkUtil.isInViewDistance
import de.bixilon.minosoft.util.collections.floats.DirectArrayFloatList


class ParticleRenderer(
    private val connection: PlayConnection,
    override val renderWindow: RenderWindow,
) : AsyncRenderer, TransparentDrawable, TranslucentDrawable, SkipAll, AbstractParticleRenderer {
    override val renderSystem: RenderSystem = renderWindow.renderSystem
    private val profile = connection.profiles.particle
    private val transparentShader: Shader = renderSystem.createShader(minosoft("particle"))
    private val translucentShader: Shader = renderSystem.createShader(minosoft("particle"))

    // There is no opaque mesh because it is simply not needed (every particle has transparency)
    private var transparentMesh = ParticleMesh(renderWindow, DirectArrayFloatList(RenderConstants.MAXIMUM_PARTICLE_AMOUNT * ParticleMesh.ParticleMeshStruct.FLOATS_PER_VERTEX))
    private var translucentMesh = ParticleMesh(renderWindow, DirectArrayFloatList(RenderConstants.MAXIMUM_PARTICLE_AMOUNT * ParticleMesh.ParticleMeshStruct.FLOATS_PER_VERTEX))

    private val particlesLock = SimpleLock()
    private var particles: MutableList<Particle> = mutableListOf()
    private var particleQueueLock = SimpleLock()
    private var particleQueue: MutableList<Particle> = mutableListOf()


    private lateinit var particleTask: TimeWorkerTask

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
    private var maxAmount = RenderConstants.MAXIMUM_PARTICLE_AMOUNT
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

    override fun init(latch: CountUpAndDownLatch) {
        profile::maxAmount.profileWatch(this, true, profile) { maxAmount = minOf(it, RenderConstants.MAXIMUM_PARTICLE_AMOUNT) }
        profile::enabled.profileWatch(this, true, profile) { enabled = it }

        connection.registerEvent(CallbackEventInvoker.of<CameraMatrixChangeEvent> {
            renderWindow.queue += {
                fun applyToShader(shader: Shader) {
                    shader.apply {
                        use()
                        setMat4(ShaderUniforms.VIEW_PROJECTION_MATRIX, Mat4(it.viewProjectionMatrix))
                        setVec3(ShaderUniforms.CAMERA_RIGHT, Vec3(it.viewMatrix[0][0], it.viewMatrix[1][0], it.viewMatrix[2][0]))
                        setVec3(ShaderUniforms.CAMERA_UP, Vec3(it.viewMatrix[0][1], it.viewMatrix[1][1], it.viewMatrix[2][1]))
                    }
                }

                applyToShader(transparentShader)
                applyToShader(translucentShader)
            }
        })

        transparentMesh.load()
        translucentMesh.load()
        for (particle in connection.registries.particleTypeRegistry) {
            for (resourceLocation in particle.textures) {
                renderWindow.textureManager.staticTextures.createTexture(resourceLocation)
            }
        }

        DefaultParticleBehavior.register(connection, this)
    }

    override fun postInit(latch: CountUpAndDownLatch) {
        transparentShader.defines[Shader.TRANSPARENT_DEFINE] = ""
        transparentShader.load()
        renderWindow.textureManager.staticTextures.use(transparentShader)
        renderWindow.textureManager.staticTextures.animator.use(transparentShader)

        translucentShader.load()
        renderWindow.textureManager.staticTextures.use(translucentShader)
        renderWindow.textureManager.staticTextures.animator.use(translucentShader)


        connection.world.particleRenderer = this

        particleTask = TimeWorkerTask(ProtocolDefinition.TICK_TIME, maxDelayTime = ProtocolDefinition.TICK_TIME / 2) {
            if (!renderWindow.renderingState.running || !enabled || connection.state != PlayConnectionStates.PLAYING) {
                return@TimeWorkerTask
            }
            val cameraPosition = connection.player.positionInfo.chunkPosition
            val particleViewDistance = connection.world.view.particleViewDistance

            val toRemove: MutableSet<Particle> = mutableSetOf()

            particlesLock.acquire()
            try {
                val time = millis()
                for (particle in particles) {
                    if (!particle.chunkPosition.isInViewDistance(particleViewDistance, cameraPosition)) { // ToDo: Check fog distance
                        particle.dead = true
                        toRemove += particle
                    } else if (particle.dead) {
                        toRemove += particle
                    } else {
                        particle.tryTick(time)
                    }
                }
            } finally {
                particlesLock.release()
            }

            particlesLock.lock()
            particles -= toRemove

            particleQueueLock.lock()
            particles += particleQueue
            particleQueue.clear()
            particleQueueLock.unlock()

            particlesLock.unlock()
        }
        TimeWorker += particleTask

        connection::state.observe(this) {
            if (!it.disconnected) {
                return@observe
            }
            TimeWorker.removeTask(particleTask)
        }
    }

    override fun addParticle(particle: Particle) {
        if (!renderWindow.renderingState.running || !enabled) {
            return
        }
        val particleCount = particles.size + particleQueue.size
        if (particleCount >= maxAmount) {
            return
        }

        if (!particle.chunkPosition.isInViewDistance(connection.world.view.particleViewDistance, connection.player.positionInfo.chunkPosition)) {
            particle.dead = true
            return
        }
        particle.tryTick(millis())

        particleQueueLock.lock()
        particleQueue += particle
        particleQueueLock.unlock()
    }

    override fun prePrepareDraw() {
        transparentMesh.unload()
        translucentMesh.unload()
    }

    override fun prepareDrawAsync() {
        transparentMesh.data.clear()
        translucentMesh.data.clear()
        transparentMesh = ParticleMesh(renderWindow, transparentMesh.data)
        translucentMesh = ParticleMesh(renderWindow, translucentMesh.data)

        particlesLock.acquire()

        val time = millis()
        for (particle in particles) {
            particle.tryTick(time)
            if (particle.dead) {
                continue
            }
            particle.addVertex(transparentMesh, translucentMesh, time)
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
        override val RESOURCE_LOCATION = ResourceLocation("minosoft:particle")

        override fun build(connection: PlayConnection, renderWindow: RenderWindow): ParticleRenderer {
            return ParticleRenderer(connection, renderWindow)
        }
    }
}
