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
import de.bixilon.kutil.collections.CollectionUtil.synchronizedSetOf
import de.bixilon.kutil.collections.CollectionUtil.toSynchronizedSet
import de.bixilon.kutil.collections.map.LockMap
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.minosoft.config.key.KeyActions
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.config.profile.delegate.watcher.SimpleProfileDelegateWatcher.Companion.profileWatch
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.player.LocalPlayerEntity
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.modding.events.VisibilityGraphChangeEvent
import de.bixilon.minosoft.gui.rendering.renderer.Renderer
import de.bixilon.minosoft.gui.rendering.renderer.RendererBuilder
import de.bixilon.minosoft.gui.rendering.system.base.DepthFunctions
import de.bixilon.minosoft.gui.rendering.system.base.RenderSystem
import de.bixilon.minosoft.gui.rendering.system.base.phases.OpaqueDrawable
import de.bixilon.minosoft.gui.rendering.system.base.phases.SkipAll
import de.bixilon.minosoft.modding.event.events.EntityDestroyEvent
import de.bixilon.minosoft.modding.event.events.EntitySpawnEvent
import de.bixilon.minosoft.modding.event.invoker.CallbackEventInvoker
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.format
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class EntityHitboxRenderer(
    val connection: PlayConnection,
    override val renderWindow: RenderWindow,
) : Renderer, OpaqueDrawable, SkipAll {
    override val renderSystem: RenderSystem = renderWindow.renderSystem
    val profile = connection.profiles.entity.hitbox
    private val visibilityGraph = renderWindow.camera.visibilityGraph
    private val meshes: LockMap<Entity, EntityHitbox> = lockMapOf()
    private val toUnload: MutableSet<EntityHitbox> = synchronizedSetOf()

    private lateinit var localHitbox: EntityHitbox

    private var enabled = profile.enabled
    private var setAvailable = enabled

    override val skipAll: Boolean
        get() = !enabled

    override fun init(latch: CountUpAndDownLatch) {
        connection.registerEvent(CallbackEventInvoker.of<EntitySpawnEvent> {
            if (!enabled) {
                return@of
            }
            meshes.synchronizedGetOrPut(it.entity) { EntityHitbox(this, it.entity, visibilityGraph) }
        })
        connection.registerEvent(CallbackEventInvoker.of<EntityDestroyEvent> {
            if (!enabled) {
                return@of
            }
            toUnload += meshes.remove(it.entity) ?: return@of
        })
        connection.registerEvent(CallbackEventInvoker.of<VisibilityGraphChangeEvent> {
            if (!enabled) {
                return@of
            }
            meshes.lock.acquire()
            for (mesh in meshes.values) {
                mesh.updateVisibility()
            }
            meshes.lock.release()
        })

        profile::enabled.profileWatch(this, profile = connection.profiles.entity) { this.setAvailable = it }

        this.localHitbox = EntityHitbox(this, connection.player, visibilityGraph)
        profile::showLocal.profileWatch(this, true, connection.profiles.entity) {
            if (it) {
                meshes[connection.player] = localHitbox
            } else {
                meshes -= connection.player
            }
        }

        renderWindow.inputHandler.registerKeyCallback(HITBOX_TOGGLE_KEY_COMBINATION,
            KeyBinding(
                mapOf(
                    KeyActions.MODIFIER to setOf(KeyCodes.KEY_F3),
                    KeyActions.STICKY to setOf(KeyCodes.KEY_B),
                ),
            ), defaultPressed = profile.enabled) {
            profile.enabled = it
            connection.util.sendDebugMessage("Entity hit boxes: ${it.format()}")
        }
    }

    private fun updateEnabled() {
        if (setAvailable) {
            connection.world.entities.lock.acquire()
            for (entity in connection.world.entities) {
                if (entity is LocalPlayerEntity && (renderWindow.camera.firstPerson && !profile.showLocal) && !renderWindow.camera.debugView) {
                    continue
                }
                meshes[entity] = EntityHitbox(this, entity, visibilityGraph)
            }

            connection.world.entities.lock.release()
        } else {
            for (mesh in meshes.values) {
                mesh.unload()
            }
            this.meshes.clear()
        }
        this.enabled = setAvailable
    }

    override fun prepareDraw() {
        if (setAvailable != enabled) {
            updateEnabled()
        }
        for (hitBox in toUnload.toSynchronizedSet()) {
            hitBox.unload()
            toUnload -= hitBox
        }
    }

    override fun setupOpaque() {
        renderWindow.renderSystem.reset(faceCulling = false)
        if (profile.showThroughWalls) {
            renderWindow.renderSystem.depth = DepthFunctions.ALWAYS
        }
        renderWindow.shaderManager.genericColorShader.use()
    }

    override fun drawOpaque() {
        meshes.lock.acquire()
        for (hitBox in meshes.values) {
            hitBox.draw()
        }
        meshes.lock.release()
    }


    companion object : RendererBuilder<EntityHitboxRenderer> {
        override val RESOURCE_LOCATION = ResourceLocation("minosoft:entity_hitbox")
        private val HITBOX_TOGGLE_KEY_COMBINATION = "minosoft:toggle_hitboxes".toResourceLocation()

        override fun build(connection: PlayConnection, renderWindow: RenderWindow): EntityHitboxRenderer {
            return EntityHitboxRenderer(connection, renderWindow)
        }
    }
}
