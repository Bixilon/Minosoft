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

package de.bixilon.minosoft.gui.rendering.entity

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.Renderer
import de.bixilon.minosoft.gui.rendering.RendererBuilder
import de.bixilon.minosoft.gui.rendering.modding.events.FrustumChangeEvent
import de.bixilon.minosoft.gui.rendering.system.base.DepthFunctions
import de.bixilon.minosoft.gui.rendering.system.base.RenderSystem
import de.bixilon.minosoft.gui.rendering.system.base.phases.OpaqueDrawable
import de.bixilon.minosoft.modding.event.events.EntityDestroyEvent
import de.bixilon.minosoft.modding.event.events.EntitySpawnEvent
import de.bixilon.minosoft.modding.event.invoker.CallbackEventInvoker
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.lockMapOf
import de.bixilon.minosoft.util.KUtil.synchronizedSetOf
import de.bixilon.minosoft.util.KUtil.toSynchronizedSet
import de.bixilon.minosoft.util.collections.LockMap

class EntityHitBoxRenderer(
    val connection: PlayConnection,
    override val renderWindow: RenderWindow,
) : Renderer, OpaqueDrawable {
    override val renderSystem: RenderSystem = renderWindow.renderSystem
    private val frustum = renderWindow.inputHandler.camera.frustum
    private val meshes: LockMap<Entity, EntityHitBox> = lockMapOf()
    private val toUnload: MutableSet<EntityHitBox> = synchronizedSetOf()


    override fun init() {
        connection.registerEvent(CallbackEventInvoker.of<EntitySpawnEvent> {
            meshes.getOrPut(it.entity) { EntityHitBox(renderWindow, it.entity, frustum) }
        })
        connection.registerEvent(CallbackEventInvoker.of<EntityDestroyEvent> { toUnload += meshes.remove(it.entity) ?: return@of })
        connection.registerEvent(CallbackEventInvoker.of<FrustumChangeEvent> {
            meshes.lock.acquire()
            for (mesh in meshes.values) {
                mesh.updateVisibility()
            }
            meshes.lock.release()
        })

        if (Minosoft.config.config.game.entities.hitBox.ownHitBox) {
            meshes[connection.player] = EntityHitBox(renderWindow, connection.player, frustum)
        }
    }

    override fun prepareDraw() {
        for (hitBox in toUnload.toSynchronizedSet()) {
            hitBox.unload()
            toUnload -= hitBox
        }
    }

    override fun setupOpaque() {
        renderWindow.renderSystem.reset(faceCulling = false)
        if (Minosoft.config.config.game.entities.hitBox.disableZBuffer) {
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


    companion object : RendererBuilder<EntityHitBoxRenderer> {
        override val RESOURCE_LOCATION = ResourceLocation("minosoft:entity_hit_box")

        override fun build(connection: PlayConnection, renderWindow: RenderWindow): EntityHitBoxRenderer {
            return EntityHitBoxRenderer(connection, renderWindow)
        }
    }
}
