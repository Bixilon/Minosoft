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

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.Renderer
import de.bixilon.minosoft.gui.rendering.RendererBuilder
import de.bixilon.minosoft.gui.rendering.modding.events.CameraMatrixChangeEvent
import de.bixilon.minosoft.gui.rendering.particle.types.Particle
import de.bixilon.minosoft.gui.rendering.system.base.shader.Shader
import de.bixilon.minosoft.modding.event.invoker.CallbackEventInvoker
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.synchronizedSetOf
import de.bixilon.minosoft.util.KUtil.toSynchronizedSet
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3


class ParticleRenderer(
    private val connection: PlayConnection,
    val renderWindow: RenderWindow,
) : Renderer {
    private val particleShader: Shader = renderWindow.renderSystem.createShader(ResourceLocation(ProtocolDefinition.MINOSOFT_NAMESPACE, "particle"))
    private var particleMesh = ParticleMesh(renderWindow)
    private var transparentParticleMesh = ParticleMesh(renderWindow)

    private var particles: MutableSet<Particle> = synchronizedSetOf()

    override fun init() {
        connection.registerEvent(CallbackEventInvoker.of<CameraMatrixChangeEvent> {
            renderWindow.queue += {
                particleShader.use().setMat4("uViewProjectionMatrix", Mat4(it.viewProjectionMatrix))
                particleShader.use().setVec3("uCameraRight", Vec3(it.viewMatrix[0][0], it.viewMatrix[1][0], it.viewMatrix[2][0]))
                particleShader.use().setVec3("uCameraUp", Vec3(it.viewMatrix[0][1], it.viewMatrix[1][1], it.viewMatrix[2][1]))
            }
        })
        particleMesh.load()
        transparentParticleMesh.load()
        connection.registries.particleTypeRegistry.forEachItem {
            for (resourceLocation in it.textures) {
                renderWindow.textureManager.staticTextures.createTexture(resourceLocation)
            }
        }

        DefaultParticleBehavior.register(connection, this)
    }

    override fun postInit() {
        particleShader.load()
        renderWindow.textureManager.staticTextures.use(particleShader)
        renderWindow.textureManager.staticTextures.animator.use(particleShader)

        connection.world.particleRenderer = this
    }

    fun add(particle: Particle) {
        check(particles.size < RenderConstants.MAXIMUM_PARTICLE_AMOUNT) { "Can not add particle: Limit reached (${particles.size} > ${RenderConstants.MAXIMUM_PARTICLE_AMOUNT}" }
        particles += particle
    }

    operator fun plusAssign(particle: Particle) {
        add(particle)
    }

    override fun update() {
        particleMesh.unload()
        transparentParticleMesh.unload()
        particleMesh = ParticleMesh(renderWindow)
        transparentParticleMesh = ParticleMesh(renderWindow)


        for (particle in particles.toSynchronizedSet()) {
            particle.tryTick()
            if (particle.dead) {
                this.particles -= particle
                continue
            }
            particle.addVertex(transparentParticleMesh, particleMesh)
        }

        particleMesh.load()
        transparentParticleMesh.load()
    }

    override fun draw() {
        renderWindow.renderSystem.reset()
        particleShader.use()
        particleMesh.draw()
    }

    override fun postDraw() {
        renderWindow.renderSystem.reset(depthMask = false)
        particleShader.use()
        transparentParticleMesh.draw()
    }

    companion object : RendererBuilder<ParticleRenderer> {
        override val RESOURCE_LOCATION = ResourceLocation("minosoft:particle")


        override fun build(connection: PlayConnection, renderWindow: RenderWindow): ParticleRenderer {
            return ParticleRenderer(connection, renderWindow)
        }
    }
}
