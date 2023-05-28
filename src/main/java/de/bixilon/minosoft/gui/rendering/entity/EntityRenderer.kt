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

package de.bixilon.minosoft.gui.rendering.entity

import de.bixilon.kutil.collections.CollectionUtil.lockMapOf
import de.bixilon.kutil.collections.CollectionUtil.synchronizedListOf
import de.bixilon.kutil.collections.map.LockMap
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.concurrent.pool.ThreadPool
import de.bixilon.kutil.concurrent.worker.unconditional.UnconditionalTask
import de.bixilon.kutil.concurrent.worker.unconditional.UnconditionalWorker
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.time.TimeUtil.millis
import de.bixilon.minosoft.config.key.KeyActions
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.entity.models.EntityModel
import de.bixilon.minosoft.gui.rendering.entity.models.minecraft.player.LocalPlayerModel
import de.bixilon.minosoft.gui.rendering.events.VisibilityGraphChangeEvent
import de.bixilon.minosoft.gui.rendering.renderer.renderer.Renderer
import de.bixilon.minosoft.gui.rendering.renderer.renderer.RendererBuilder
import de.bixilon.minosoft.gui.rendering.system.base.RenderSystem
import de.bixilon.minosoft.gui.rendering.system.base.phases.OpaqueDrawable
import de.bixilon.minosoft.modding.event.events.EntityDestroyEvent
import de.bixilon.minosoft.modding.event.events.EntitySpawnEvent
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.format
import java.util.concurrent.atomic.AtomicInteger

class EntityRenderer(
    val connection: PlayConnection,
    override val context: RenderContext,
) : Renderer, OpaqueDrawable {
    override val renderSystem: RenderSystem = context.renderSystem
    val profile = connection.profiles.entity
    val visibilityGraph = context.camera.visibilityGraph
    private val models: LockMap<Entity, EntityModel<*>> = lockMapOf()
    private lateinit var localModel: LocalPlayerModel
    private var toUnload: MutableList<EntityModel<*>> = synchronizedListOf()

    var hitboxes = profile.hitbox.enabled

    val modelCount: Int get() = models.size
    var visibleCount: Int = 0
        private set

    private var reset = false

    override fun init(latch: CountUpAndDownLatch) {
        connection.events.listen<EntitySpawnEvent> { event ->
            if (event.entity is LocalPlayerEntity) return@listen
            DefaultThreadPool += { event.entity.createModel(this)?.let { models[event.entity] = it } }
        }
        connection.events.listen<EntityDestroyEvent> {
            if (it.entity is LocalPlayerEntity) return@listen
            DefaultThreadPool += add@{ toUnload += models.remove(it.entity) ?: return@add }
        }
        connection.events.listen<VisibilityGraphChangeEvent> {
            runAsync { it.updateVisibility(visibilityGraph) }
        }

        profile.hitbox::enabled.observe(this) { this.hitboxes = it }
        context.camera.offset::offset.observe(this) { reset = true }

        context.inputHandler.registerKeyCallback(
            HITBOX_TOGGLE_KEY_COMBINATION,
            KeyBinding(
                KeyActions.MODIFIER to setOf(KeyCodes.KEY_F3),
                KeyActions.STICKY to setOf(KeyCodes.KEY_B),
            ), defaultPressed = profile.hitbox.enabled
        ) {
            profile.hitbox.enabled = it
            connection.util.sendDebugMessage("Entity hit boxes: ${it.format()}")
            hitboxes = it
        }
    }

    override fun postAsyncInit(latch: CountUpAndDownLatch) {
        localModel = context.connection.player.createModel(this)

        models[connection.player] = localModel
    }

    private fun unloadUnused() {
        while (toUnload.isNotEmpty()) { // ToDo: Thread safety
            val model = toUnload.removeAt(0)
            model.unload()
        }
    }

    override fun prePrepareDraw() {
        val count = AtomicInteger()
        val reset = reset
        runAsync {
            it.entity.draw(millis())
            if (reset) {
                it.reset()
            }
            it.update = it.checkUpdate()
            it.prepareAsync()
            if (it.visible) {
                count.incrementAndGet()
            }
        }
        this.visibleCount = count.get()
        if (reset) {
            this.reset = false
        }
    }

    override fun postPrepareDraw() {
        unloadUnused()
        models.lock.acquire()
        for (model in models.unsafe.values) {
            model.prepare()
        }
        models.lock.release()
    }

    override fun setupOpaque() {
        context.renderSystem.reset(faceCulling = false)
    }

    override fun drawOpaque() {
        // ToDo: Probably more transparent
        models.lock.acquire()
        for (model in models.unsafe.values) {
            if (model.skipDraw) {
                continue
            }
            model.draw()
        }
        models.lock.release()
    }

    private fun runAsync(executor: ((EntityModel<*>) -> Unit)) {
        val worker = UnconditionalWorker()
        models.lock.acquire()
        for (model in models.unsafe.values) {
            worker += UnconditionalTask(ThreadPool.Priorities.HIGH) {
                executor(model)
            }
        }
        models.lock.release()
        worker.work()
    }


    companion object : RendererBuilder<EntityRenderer> {
        override val identifier = minosoft("entity")
        private val HITBOX_TOGGLE_KEY_COMBINATION = minosoft("toggle_hitboxes")


        override fun build(connection: PlayConnection, context: RenderContext): EntityRenderer {
            return EntityRenderer(connection, context)
        }
    }
}
