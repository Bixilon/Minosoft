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

package de.bixilon.minosoft.gui.rendering.particle

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.*
import de.bixilon.minosoft.gui.rendering.modding.events.CameraMatrixChangeEvent
import de.bixilon.minosoft.gui.rendering.particle.types.Particle
import de.bixilon.minosoft.gui.rendering.system.base.RenderSystem
import de.bixilon.minosoft.gui.rendering.system.base.phases.TranslucentDrawable
import de.bixilon.minosoft.gui.rendering.system.base.phases.TransparentDrawable
import de.bixilon.minosoft.gui.rendering.system.base.shader.Shader
import de.bixilon.minosoft.modding.event.events.connection.play.PlayConnectionStateChangeEvent
import de.bixilon.minosoft.modding.event.invoker.CallbackEventInvoker
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnectionStates.Companion.disconnected
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.ReadWriteLock
import de.bixilon.minosoft.util.collections.floats.DirectArrayFloatList
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import de.bixilon.minosoft.util.task.time.TimeWorker
import de.bixilon.minosoft.util.task.time.TimeWorkerTask
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3


class ParticleRenderer(
    private val connection: PlayConnection,
    override val renderWindow: RenderWindow,
) : Renderer, TransparentDrawable, TranslucentDrawable {
    override val renderSystem: RenderSystem = renderWindow.renderSystem
    private val transparentShader: Shader = renderSystem.createShader(ResourceLocation(ProtocolDefinition.MINOSOFT_NAMESPACE, "particle"))
    private val translucentShader: Shader = renderSystem.createShader(ResourceLocation(ProtocolDefinition.MINOSOFT_NAMESPACE, "particle"))

    // There is no opaque mesh because it is simply not needed (every particle has transparency)
    private var transparentMesh = ParticleMesh(renderWindow, DirectArrayFloatList(RenderConstants.MAXIMUM_PARTICLE_AMOUNT * ParticleMesh.ParticleMeshStruct.FLOATS_PER_VERTEX))
    private var translucentMesh = ParticleMesh(renderWindow, DirectArrayFloatList(RenderConstants.MAXIMUM_PARTICLE_AMOUNT * ParticleMesh.ParticleMeshStruct.FLOATS_PER_VERTEX))

    private val particlesLock = ReadWriteLock()
    private var particles: MutableList<Particle> = mutableListOf()
    private var particleQueueLock = ReadWriteLock()
    private var particleQueue: MutableList<Particle> = mutableListOf()


    private lateinit var particleTask: TimeWorkerTask

    val size: Int
        get() = particles.size

    override fun init() {
        connection.registerEvent(CallbackEventInvoker.of<CameraMatrixChangeEvent> {
            renderWindow.queue += {
                fun applyToShader(shader: Shader) {
                    shader.apply {
                        use()
                        setMat4("uViewProjectionMatrix", Mat4(it.viewProjectionMatrix))
                        setVec3("uCameraRight", Vec3(it.viewMatrix[0][0], it.viewMatrix[1][0], it.viewMatrix[2][0]))
                        setVec3("uCameraUp", Vec3(it.viewMatrix[0][1], it.viewMatrix[1][1], it.viewMatrix[2][1]))
                    }
                }

                applyToShader(transparentShader)
                applyToShader(translucentShader)
            }
        })

        transparentMesh.load()
        translucentMesh.load()
        connection.registries.particleTypeRegistry.forEachItem {
            for (resourceLocation in it.textures) {
                renderWindow.textureManager.staticTextures.createTexture(resourceLocation)
            }
        }

        DefaultParticleBehavior.register(connection, this)
    }

    override fun postInit() {
        transparentShader.defines[Shader.TRANSPARENT_DEFINE] = ""
        transparentShader.load()
        renderWindow.textureManager.staticTextures.use(transparentShader)
        renderWindow.textureManager.staticTextures.animator.use(transparentShader)

        translucentShader.load()
        renderWindow.textureManager.staticTextures.use(translucentShader)
        renderWindow.textureManager.staticTextures.animator.use(translucentShader)


        connection.world.particleRenderer = this

        particleTask = TimeWorker.addTask(TimeWorkerTask(ProtocolDefinition.TICK_TIME, maxDelayTime = ProtocolDefinition.TICK_TIME / 2) {
            if (renderWindow.renderingState == RenderingStates.PAUSED || renderWindow.renderingState == RenderingStates.STOPPED) {
                return@TimeWorkerTask
            }

            val cameraLength = connection.player.position.length()
            val toRemove: MutableSet<Particle> = mutableSetOf()

            particlesLock.acquire()
            try {
                val time = KUtil.time
                for (particle in particles) {
                    if (particle.position.length() - cameraLength >= Minosoft.config.config.game.camera.viewDistance * ProtocolDefinition.SECTION_WIDTH_X) {
                        particle.dead = true
                        toRemove += particle
                    } else if (particle.dead) {
                        toRemove += particle
                    }
                    particle.tryTick(time)
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
        })

        connection.registerEvent(CallbackEventInvoker.of<PlayConnectionStateChangeEvent> {
            if (!it.state.disconnected) {
                return@of
            }
            TimeWorker.removeTask(particleTask)
        })
    }

    fun add(particle: Particle) {
        if (renderWindow.renderingState == RenderingStates.PAUSED || renderWindow.renderingState == RenderingStates.STOPPED) {
            return
        }
        val particleCount = particles.size
        if (particleCount >= RenderConstants.MAXIMUM_PARTICLE_AMOUNT) {
            Log.log(LogMessageType.RENDERING_GENERAL, LogLevels.WARN) { "Can not add particle: Limit reached (${particleCount} > ${RenderConstants.MAXIMUM_PARTICLE_AMOUNT}" }
            return
        }
        val cameraLength = connection.player.position.length()

        if (particle.position.length() - cameraLength >= Minosoft.config.config.game.camera.viewDistance * ProtocolDefinition.SECTION_WIDTH_X) {
            particle.dead = true
            return
        }

        particleQueueLock.lock()
        particleQueue += particle
        particleQueueLock.unlock()
    }

    operator fun plusAssign(particle: Particle) = add(particle)

    override fun prepareDraw() {
        transparentMesh.unload()
        translucentMesh.unload()


        transparentMesh.data.clear()
        translucentMesh.data.clear()
        transparentMesh = ParticleMesh(renderWindow, transparentMesh.data)
        translucentMesh = ParticleMesh(renderWindow, translucentMesh.data)

        particlesLock.acquire()

        val time = KUtil.time
        for (particle in particles) {
            if (particle.dead) {
                continue
            }
            particle.addVertex(transparentMesh, translucentMesh, time)
        }

        particlesLock.release()

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


    companion object : RendererBuilder<ParticleRenderer> {
        override val RESOURCE_LOCATION = ResourceLocation("minosoft:particle")


        override fun build(connection: PlayConnection, renderWindow: RenderWindow): ParticleRenderer {
            return ParticleRenderer(connection, renderWindow)
        }
    }
}
