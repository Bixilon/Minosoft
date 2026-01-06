/*
 * Minosoft
 * Copyright (C) 2020-2026 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.chunk.breaking

import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.kutil.concurrent.lock.Lock
import de.bixilon.kutil.concurrent.lock.LockUtil.locked
import de.bixilon.kutil.exception.ExceptionUtil.ignoreAll
import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.chunk.breaking.mesh.BlockBreakShader
import de.bixilon.minosoft.gui.rendering.chunk.breaking.mesh.BreakingMeshBuilder
import de.bixilon.minosoft.gui.rendering.renderer.renderer.RendererBuilder
import de.bixilon.minosoft.gui.rendering.renderer.renderer.world.LayerSettings
import de.bixilon.minosoft.gui.rendering.renderer.renderer.world.WorldRenderer
import de.bixilon.minosoft.gui.rendering.system.base.layer.RenderLayer
import de.bixilon.minosoft.gui.rendering.system.base.layer.TranslucentLayer
import de.bixilon.minosoft.gui.rendering.util.mesh.MeshStates
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.blockPosition
import de.bixilon.minosoft.modding.event.events.BlockBreakAnimationEvent
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import de.bixilon.minosoft.protocol.network.session.play.PlaySession

class BlockBreakRenderer(
    override val context: RenderContext,
    val animation: BreakAnimation,
) : WorldRenderer {
    private val shader = context.system.shader.create(minosoft("chunk/breaking"), ::BlockBreakShader)
    override val layers = LayerSettings()
    private val lock = Lock.lock()
    private val instances: HashMap<Int, BreakInstance> = HashMap()

    override fun postInit(latch: AbstractLatch) {
        shader.load()
    }

    private fun unload(id: Int) {
        val existing = lock.locked { instances.remove(id) } ?: return
        if (existing.mesh.state == MeshStates.PREPARING) {
            return existing.mesh.drop()
        }
        context.queue += { existing.mesh.unload() }
    }

    private fun update(id: Int, position: BlockPosition, progress: Float) = lock.locked {
        val state = context.session.world[position] ?: return unload(id)

        val existing = instances[id]

        if (existing != null) {
            if (existing.position == position && existing.state == state) {
                existing.progress = progress
                return
            }
            unload(id)
        }
        while (instances.size >= MAX_INSTANCES) {
            unload(instances.keys.first())
        }
        val model = state.model ?: state.block.model ?: return
        val builder = BreakingMeshBuilder(context)

        model.render(builder, state, null, Vec3f(position - context.camera.offset.offset), null) // TODO: listen for offset changes

        instances[id] = BreakInstance(position, state, progress, builder.bake())
    }

    override fun init(latch: AbstractLatch) {
        context.session.events.listen<BlockBreakAnimationEvent> {
            if (it.progress == null) {
                unload(it.id)
                return@listen
            }
            update(it.id, it.position, it.progress)
        }
        context.session.camera.interactions.breaking.digging::status.observe(this) {
            if (it == null) {
                unload(SELF_ID)
                return@observe
            }
            it::progress.observe(this, true) { progress -> update(SELF_ID, it.position, progress) }
        }
    }

    override fun registerLayers() {
        layers.register(BlockDestroyLayer, shader, this::draw) { this.instances.isEmpty() }
    }

    override fun prePrepareDraw() = lock.locked {
        for (instance in instances.values) {
            if (instance.mesh.state == MeshStates.PREPARING) {
                instance.mesh.load()
            }
        }
    }

    private fun draw() = lock.locked {
        for (instance in instances.values) {
            val distance = (instance.position - context.session.camera.entity.renderInfo.position.blockPosition).length2()
            if (distance > MAX_DISTANCE) continue

            if (instance.mesh.state == MeshStates.PREPARING) continue // created after preparing state
            val texture = animation[instance.progress] ?: continue
            shader.texture = texture.shaderId

            instance.mesh.draw()
        }
    }

    private object BlockDestroyLayer : RenderLayer {
        override val settings = TranslucentLayer.settings.copy(
            polygonOffset = true,
            polygonOffsetFactor = -3.0f,
            polygonOffsetUnit = -3.0f,
        )
        override val priority get() = TranslucentLayer.priority + 100
    }

    companion object : RendererBuilder<BlockBreakRenderer> {
        const val MAX_DISTANCE = 64 * 64
        const val MAX_INSTANCES = 64
        const val SELF_ID = -1

        override fun build(session: PlaySession, context: RenderContext): BlockBreakRenderer? {
            val animation = ignoreAll { BreakAnimation.load(context.textures, context.session.assets) } ?: return null

            return BlockBreakRenderer(context, animation)
        }
    }
}
