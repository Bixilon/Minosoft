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

package de.bixilon.minosoft.gui.rendering.particle

import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.kutil.array.ArrayUtil.cast
import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.data.world.chunk.ChunkUtil.isInViewDistance
import de.bixilon.minosoft.data.world.particle.AbstractParticleRenderer
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.events.CameraMatrixChangeEvent
import de.bixilon.minosoft.gui.rendering.particle.types.Particle
import de.bixilon.minosoft.gui.rendering.renderer.renderer.AsyncRenderer
import de.bixilon.minosoft.gui.rendering.renderer.renderer.RendererBuilder
import de.bixilon.minosoft.gui.rendering.renderer.renderer.world.LayerSettings
import de.bixilon.minosoft.gui.rendering.renderer.renderer.world.WorldRenderer
import de.bixilon.minosoft.gui.rendering.system.base.RenderSystem
import de.bixilon.minosoft.gui.rendering.system.base.layer.OpaqueLayer
import de.bixilon.minosoft.gui.rendering.system.base.layer.TranslucentLayer
import de.bixilon.minosoft.gui.rendering.system.base.phases.SkipAll
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.util.collections.floats.BufferedArrayFloatList
import java.util.*


class ParticleRenderer(
    private val session: PlaySession,
    override val context: RenderContext,
) : WorldRenderer, AsyncRenderer, SkipAll, AbstractParticleRenderer {
    override val random = Random()
    override val layers = LayerSettings()
    override val renderSystem: RenderSystem = context.system
    private val profile = session.profiles.particle
    private val shader = renderSystem.createShader(minosoft("particle")) { ParticleShader(it) }

    // There is no opaque mesh because it is simply not needed (every particle has transparency)
    var mesh = ParticleMesh(context, BufferedArrayFloatList(profile.maxAmount * ParticleMesh.ParticleMeshStruct.FLOATS_PER_VERTEX))
    var translucentMesh = ParticleMesh(context, BufferedArrayFloatList(profile.maxAmount * ParticleMesh.ParticleMeshStruct.FLOATS_PER_VERTEX))

    val particles = ParticleList(profile.maxAmount)
    val queue = ParticleQueue(this)
    val ticker = ParticleTicker(this)
    private var matrixUpdate = true


    override val skipAll: Boolean
        get() = !enabled


    var enabled = true
        set(value) {
            if (!value) {
                particles.clear()
                queue.clear()
            }
            field = value
        }
    var maxAmount = MAXIMUM_AMOUNT
        set(value) {
            if (value < 0) throw IllegalStateException("Can not set negative amount of particles!")
            if (value < field) {
                removeAllParticles()
            }
            field = value
        }

    val size: Int
        get() = particles.size

    override fun registerLayers() {
        layers.register(OpaqueLayer, shader, renderer = { mesh.draw() })
        layers.register(TranslucentLayer, shader, renderer = { translucentMesh.draw() })
    }

    private fun loadTextures() {
        for (particle in session.registries.particleType) {
            val loaded: Array<Texture> = arrayOfNulls<Texture?>(particle.textures.size).cast()
            for ((index, texture) in particle.textures.withIndex()) {
                loaded[index] = context.textures.static.create(texture)
            }
            particle.loadedTextures = loaded
        }
    }

    override fun init(latch: AbstractLatch) {
        profile::maxAmount.observe(this, true) { maxAmount = minOf(it, MAXIMUM_AMOUNT) }
        profile::enabled.observe(this, true) { enabled = it }

        // TODO: unload particles when renderer is paused

        session.events.listen<CameraMatrixChangeEvent> { matrixUpdate = true }

        mesh.load()
        translucentMesh.load()

        loadTextures()
        DefaultParticleBehavior.register(session, this)
    }

    override fun postInit(latch: AbstractLatch) {
        shader.load()
        ticker.init()

        session.world.particle = this
    }

    override fun addParticle(particle: Particle) {
        if (!context.state.running || !enabled) {
            return
        }
        if (!particle.chunkPosition.isInViewDistance(session.world.view.particleViewDistance, session.player.physics.positionInfo.chunkPosition)) {
            particle.dead = true
            return
        }

        queue += particle
    }

    private fun updateShader() {
        val matrix = context.camera.matrix.viewProjectionMatrix
        shader.cameraRight = Vec3f(matrix[0, 0], matrix[0, 1], matrix[0, 2])
        shader.cameraUp = Vec3f(matrix[1, 0], matrix[1, 1], matrix[1, 2])
    }

    override fun prePrepareDraw() {
        if (matrixUpdate) {
            updateShader()
            matrixUpdate = false
        }
        mesh.unload()
        translucentMesh.unload()
    }

    private fun prepareMesh() {
        mesh.data.clear()
        translucentMesh.data.clear()
        mesh = ParticleMesh(context, mesh.data)
        translucentMesh = ParticleMesh(context, translucentMesh.data)
    }

    override fun prepareDrawAsync() {
        prepareMesh()
        ticker.tick(true)
    }

    override fun postPrepareDraw() {
        mesh.load()
        translucentMesh.load()
    }

    override fun removeAllParticles() {
        particles.clear()
        queue.clear()
    }

    companion object : RendererBuilder<ParticleRenderer> {
        const val MAXIMUM_AMOUNT = 50000

        override fun build(session: PlaySession, context: RenderContext): ParticleRenderer? {
            if (session.profiles.particle.skipLoading) {
                return null
            }
            return ParticleRenderer(session, context)
        }
    }
}
