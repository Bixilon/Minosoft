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

import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.Renderer
import de.bixilon.minosoft.gui.rendering.RendererBuilder
import de.bixilon.minosoft.gui.rendering.modding.events.CameraMatrixChangeEvent
import de.bixilon.minosoft.gui.rendering.particle.types.Particle
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.CampfireSmokeParticle
import de.bixilon.minosoft.gui.rendering.shader.Shader
import de.bixilon.minosoft.gui.rendering.textures.Texture
import de.bixilon.minosoft.gui.rendering.textures.TextureArray
import de.bixilon.minosoft.modding.event.CallbackEventInvoker
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.synchronizedSetOf
import de.bixilon.minosoft.util.KUtil.toSynchronizedSet
import de.bixilon.minosoft.util.MMath
import glm_.vec3.Vec3
import org.lwjgl.opengl.GL11.glDepthMask
import kotlin.random.Random


class ParticleRenderer(
    private val connection: PlayConnection,
    val renderWindow: RenderWindow,
) : Renderer {
    private lateinit var particleShader: Shader
    private var particleMesh = ParticleMesh()

    private var particles: MutableSet<Particle> = synchronizedSetOf()

    override fun init() {
        connection.registerEvent(CallbackEventInvoker.of<CameraMatrixChangeEvent> {
            renderWindow.queue += {
                particleShader.use().setMat4("uViewProjectionMatrix", it.viewProjectionMatrix)
                particleShader.use().setVec3("uCameraRight", Vec3(it.viewMatrix[0][0], it.viewMatrix[1][0], it.viewMatrix[2][0]))
                particleShader.use().setVec3("uCameraUp", Vec3(it.viewMatrix[0][1], it.viewMatrix[1][1], it.viewMatrix[2][1]))
            }
        })
        particleMesh.load()
        connection.registries.particleTypeRegistry.forEachItem {
            for (resourceLocation in it.textures) {
                renderWindow.textures.allTextures[resourceLocation] = Texture(resourceLocation)
            }
        }

        DefaultParticleBehavior.register(connection, this)
    }

    override fun postInit() {
        particleShader = Shader(
            resourceLocation = ResourceLocation(ProtocolDefinition.MINOSOFT_NAMESPACE, "particle"),
            defines = mapOf("ANIMATED_TEXTURE_COUNT" to MMath.clamp(renderWindow.textures.animator.animatedTextures.size, 1, TextureArray.MAX_ANIMATED_TEXTURE)),
        )
        particleShader.load()
        renderWindow.textures.use(particleShader)
        renderWindow.textures.animator.use(particleShader)
    }


    fun add(particle: Particle) {
        check(particles.size < RenderConstants.MAXIMUM_PARTICLE_AMOUNT) { "Can not add particle: Limit reached (${particles.size} > ${RenderConstants.MAXIMUM_PARTICLE_AMOUNT}" }
        particles += particle
    }

    var lastTickTime = System.currentTimeMillis()

    override fun draw() {
        particleShader.use()

        particleMesh.unload()
        particleMesh = ParticleMesh()

        // ToDo: Remove, this ist just for testing purposes
        if (System.currentTimeMillis() - lastTickTime > ProtocolDefinition.TICK_TIME) {
            val blockPosition = Vec3(0, 5, 0)
            if (Random.nextFloat() < 0.11f) {
                for (i in 0 until Random.nextInt(2) + 2) {
                    val horizontal = { 0.5f + Random.nextFloat() / 3.0f * if (Random.nextBoolean()) 1.0f else -1.0f }
                    val position = Vec3(
                        blockPosition.x + horizontal(),
                        blockPosition.y + Random.nextFloat() + Random.nextFloat(),
                        blockPosition.z + horizontal()
                    )

                    val data = connection.registries.particleTypeRegistry[CampfireSmokeParticle.CosySmokeParticleFactory]!!
                    val particle = CampfireSmokeParticle(connection, this, position, Vec3(0.0f, 0.07f, 0.0f), data.simple(), true)
                    add(particle)
                }
            }
            lastTickTime = System.currentTimeMillis()
        }

        for (particle in particles.toSynchronizedSet()) {
            particle.tick()
            if (particle.dead) {
                this.particles -= particle
                continue
            }
            particle.addVertex(particleMesh)
        }

        particleMesh.load()

        glDepthMask(false)
        particleMesh.draw()
        glDepthMask(true)
    }

    companion object : RendererBuilder<ParticleRenderer> {
        override val RESOURCE_LOCATION = ResourceLocation("minosoft:particle")


        override fun build(connection: PlayConnection, renderWindow: RenderWindow): ParticleRenderer {
            return ParticleRenderer(connection, renderWindow)
        }
    }
}
