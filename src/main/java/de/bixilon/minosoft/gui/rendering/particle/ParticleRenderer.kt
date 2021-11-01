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
import de.bixilon.minosoft.gui.rendering.system.base.RenderSystem
import de.bixilon.minosoft.gui.rendering.system.base.phases.TranslucentDrawable
import de.bixilon.minosoft.gui.rendering.system.base.phases.TransparentDrawable
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
    override val renderWindow: RenderWindow,
) : Renderer, TransparentDrawable, TranslucentDrawable {
    override val renderSystem: RenderSystem = renderWindow.renderSystem
    private val transparentShader: Shader = renderSystem.createShader(ResourceLocation(ProtocolDefinition.MINOSOFT_NAMESPACE, "particle"))
    private val translucentShader: Shader = renderSystem.createShader(ResourceLocation(ProtocolDefinition.MINOSOFT_NAMESPACE, "particle"))

    // There is no opaque mesh because it is simply not needed (every particle has transparency)
    private var transparentMesh = ParticleMesh(renderWindow)
    private var translucentMesh = ParticleMesh(renderWindow)

    private var particles: MutableSet<Particle> = synchronizedSetOf()

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
    }

    fun add(particle: Particle) {
        check(particles.size < RenderConstants.MAXIMUM_PARTICLE_AMOUNT) { "Can not add particle: Limit reached (${particles.size} > ${RenderConstants.MAXIMUM_PARTICLE_AMOUNT}" }
        particles += particle
    }

    operator fun plusAssign(particle: Particle) {
        add(particle)
    }

    override fun prepareDraw() {
        transparentMesh.unload()
        translucentMesh.unload()
        transparentMesh = ParticleMesh(renderWindow)
        translucentMesh = ParticleMesh(renderWindow)


        for (particle in particles.toSynchronizedSet()) {
            particle.tryTick()
            if (particle.dead) {
                this.particles -= particle
                continue
            }
            particle.addVertex(transparentMesh, translucentMesh)
        }

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
