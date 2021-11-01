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
import de.bixilon.minosoft.data.registries.AABB
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.Renderer
import de.bixilon.minosoft.gui.rendering.RendererBuilder
import de.bixilon.minosoft.gui.rendering.modding.events.FrustumChangeEvent
import de.bixilon.minosoft.gui.rendering.system.base.DepthFunctions
import de.bixilon.minosoft.gui.rendering.system.base.RenderSystem
import de.bixilon.minosoft.gui.rendering.system.base.phases.OpaqueDrawable
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh
import de.bixilon.minosoft.modding.event.events.EntityDestroyEvent
import de.bixilon.minosoft.modding.event.events.EntitySpawnEvent
import de.bixilon.minosoft.modding.event.invoker.CallbackEventInvoker
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.synchronizedMapOf
import de.bixilon.minosoft.util.KUtil.toSynchronizedMap
import de.bixilon.minosoft.util.collections.SynchronizedMap

class EntityHitBoxRenderer(
    val connection: PlayConnection,
    override val renderWindow: RenderWindow,
) : Renderer, OpaqueDrawable {
    override val renderSystem: RenderSystem = renderWindow.renderSystem
    private val meshes: SynchronizedMap<Entity, EntityHitBoxMesh> = synchronizedMapOf()


    private fun updateMesh(entity: Entity, mesh: EntityHitBoxMesh? = meshes[entity]): EntityHitBoxMesh? {
        entity.tick() // ToDo: Remove
        val aabb = entity.cameraAABB

        val visible = renderWindow.inputHandler.camera.frustum.containsAABB(aabb)
        if (!visible) {
            return mesh
        }

        var nextMesh = mesh

        if (aabb != mesh?.aabb) {
            this.meshes.remove(entity)
            if (mesh?.state == Mesh.MeshStates.LOADED) {
                mesh.unload()
            }
            nextMesh = createMesh(entity, aabb, visible)
        }
        return nextMesh
    }

    private fun createMesh(entity: Entity, aabb: AABB = entity.cameraAABB, visible: Boolean = renderWindow.inputHandler.camera.frustum.containsAABB(aabb)): EntityHitBoxMesh? {
        if (entity.isInvisible && !Minosoft.config.config.game.entities.hitBox.renderInvisibleEntities) {
            return null
        }
        val mesh = EntityHitBoxMesh(renderWindow, entity, aabb)

        if (visible) {
            mesh.load()
        }
        mesh.needsUpdate = !visible

        mesh.visible = visible

        this.meshes[entity] = mesh

        return mesh
    }

    override fun init() {
        connection.registerEvent(CallbackEventInvoker.of<EntitySpawnEvent> {
            renderWindow.queue += {
                createMesh(it.entity)
            }
        })
        connection.registerEvent(CallbackEventInvoker.of<EntityDestroyEvent> {
            val mesh = this.meshes.getAndRemove(it.entity) ?: return@of

            renderWindow.queue += {
                mesh.unload(true)
            }
        })

        connection.registerEvent(CallbackEventInvoker.of<FrustumChangeEvent> {
            for ((_, mesh) in this.meshes.toSynchronizedMap()) {
                mesh.visible = renderWindow.inputHandler.camera.frustum.containsAABB(mesh.aabb)
            }
        })

        if (Minosoft.config.config.game.entities.hitBox.ownHitBox) {
            createMesh(connection.player)
        }
    }

    override fun setupOpaque() {
        renderWindow.renderSystem.reset(faceCulling = false) // ToDo?
        if (Minosoft.config.config.game.entities.hitBox.disableZBuffer) {
            renderWindow.renderSystem.depth = DepthFunctions.ALWAYS
        }
        renderWindow.shaderManager.genericColorShader.use()
    }

    override fun drawOpaque() {
        fun draw(mesh: EntityHitBoxMesh?) {
            mesh ?: return

            if (!mesh.visible) {
                return
            }
            if (mesh.needsUpdate) {
                mesh.load()
                mesh.needsUpdate = false
            }
            mesh.draw()
        }

        for ((entity, mesh) in meshes.toSynchronizedMap()) {
            draw(updateMesh(entity, mesh))
        }
    }


    companion object : RendererBuilder<EntityHitBoxRenderer> {
        override val RESOURCE_LOCATION = ResourceLocation("minosoft:entity_hit_box")

        override fun build(connection: PlayConnection, renderWindow: RenderWindow): EntityHitBoxRenderer {
            return EntityHitBoxRenderer(connection, renderWindow)
        }
    }
}
