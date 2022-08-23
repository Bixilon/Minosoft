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

package de.bixilon.minosoft.gui.rendering.entity

import de.bixilon.kutil.collections.CollectionUtil.lockMapOf
import de.bixilon.kutil.collections.CollectionUtil.synchronizedListOf
import de.bixilon.kutil.collections.map.LockMap
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.kutil.time.TimeUtil
import de.bixilon.minosoft.config.key.KeyActions
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.config.profile.delegate.watcher.SimpleProfileDelegateWatcher.Companion.profileWatch
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.entity.models.EntityModel
import de.bixilon.minosoft.gui.rendering.entity.models.minecraft.player.LocalPlayerModel
import de.bixilon.minosoft.gui.rendering.modding.events.VisibilityGraphChangeEvent
import de.bixilon.minosoft.gui.rendering.renderer.Renderer
import de.bixilon.minosoft.gui.rendering.renderer.RendererBuilder
import de.bixilon.minosoft.gui.rendering.system.base.RenderSystem
import de.bixilon.minosoft.gui.rendering.system.base.phases.OpaqueDrawable
import de.bixilon.minosoft.modding.event.events.EntityDestroyEvent
import de.bixilon.minosoft.modding.event.events.EntitySpawnEvent
import de.bixilon.minosoft.modding.event.invoker.CallbackEventInvoker
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.format
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class EntityRenderer(
    val connection: PlayConnection,
    override val renderWindow: RenderWindow,
) : Renderer, OpaqueDrawable {
    override val renderSystem: RenderSystem = renderWindow.renderSystem
    val profile = connection.profiles.entity
    val visibilityGraph = renderWindow.camera.visibilityGraph
    private val models: LockMap<Entity, EntityModel<*>> = lockMapOf()
    private lateinit var localModel: LocalPlayerModel
    private var toUnload: MutableList<EntityModel<*>> = synchronizedListOf()

    var hitboxes = profile.hitbox.enabled

    override fun init(latch: CountUpAndDownLatch) {
        connection.registerEvent(CallbackEventInvoker.of<EntitySpawnEvent> { event ->
            DefaultThreadPool += { event.entity.createModel(this)?.let { models[event.entity] = it } }
        })
        connection.registerEvent(CallbackEventInvoker.of<EntityDestroyEvent> {
            DefaultThreadPool += add@{ toUnload += models.remove(it.entity) ?: return@add }
        })
        connection.registerEvent(CallbackEventInvoker.of<VisibilityGraphChangeEvent> {
            runAsync { it.updateVisibility(visibilityGraph) }
        })

        profile.hitbox::enabled.profileWatch(this, profile = profile) { this.hitboxes = it }

        renderWindow.inputHandler.registerKeyCallback(
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
        localModel = renderWindow.connection.player.createModel(this)

        models[connection.player] = localModel
    }

    private fun unloadUnused() {
        while (toUnload.isNotEmpty()) { // ToDo: Thread safety
            val model = toUnload.removeAt(0)
            model.unload()
        }
    }

    override fun prePrepareDraw() {
        runAsync {
            it.entity.draw(TimeUtil.millis)
            it.update = it.checkUpdate()
            it.prepareAsync()
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
        renderWindow.renderSystem.reset(faceCulling = false)
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
        val latch = CountUpAndDownLatch(0)
        models.lock.acquire()
        for (model in models.unsafe.values) {
            latch.inc()
            DefaultThreadPool += {
                executor(model)
                latch.dec()
            }
        }
        models.lock.release()
        latch.await()
    }


    companion object : RendererBuilder<EntityRenderer> {
        override val RESOURCE_LOCATION = ResourceLocation("minosoft:entity")
        private val HITBOX_TOGGLE_KEY_COMBINATION = "minosoft:toggle_hitboxes".toResourceLocation()


        override fun build(connection: PlayConnection, renderWindow: RenderWindow): EntityRenderer {
            return EntityRenderer(connection, renderWindow)
        }
    }
}
