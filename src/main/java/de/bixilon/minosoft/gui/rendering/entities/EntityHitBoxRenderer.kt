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

package de.bixilon.minosoft.gui.rendering.entities

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.Renderer
import de.bixilon.minosoft.gui.rendering.RendererBuilder
import de.bixilon.minosoft.modding.event.CallbackEventInvoker
import de.bixilon.minosoft.modding.event.events.EntityDestroyEvent
import de.bixilon.minosoft.modding.event.events.EntitySpawnEvent
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.util.KUtil.synchronizedMapOf
import de.bixilon.minosoft.util.KUtil.toSynchronizedMap
import de.bixilon.minosoft.util.collections.SynchronizedMap
import org.lwjgl.opengl.GL11.*

class EntityHitBoxRenderer(
    val connection: PlayConnection,
    val renderWindow: RenderWindow,
) : Renderer {
    private val meshes: SynchronizedMap<Entity, EntityHitBoxMesh> = synchronizedMapOf()


    private fun createMesh(entity: Entity): EntityHitBoxMesh? {
        if (entity.isInvisible && !Minosoft.config.config.game.entities.hitBox.renderInvisibleEntities) {
            return null
        }
        val mesh = EntityHitBoxMesh(entity)
        mesh.load()
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
            val meshes: MutableSet<EntityHitBoxMesh> = mutableSetOf()
            for (entity in it.entities) {
                val mesh = this.meshes.getAndRemove(entity) ?: continue
                meshes += mesh
            }

            renderWindow.queue += {
                for (mesh in meshes) {
                    mesh.unload(false)
                }
            }
        })
    }

    override fun draw() {
        glDisable(GL_CULL_FACE)
        if (Minosoft.config.config.game.entities.hitBox.disableZBuffer) {
            glDepthFunc(GL_ALWAYS)
        }
        renderWindow.shaderManager.genericColorShader.use()

        for ((entity, mesh) in meshes.toSynchronizedMap()) {
            val aabb = entity.aabb
            if (aabb != mesh.aabb) {
                this.meshes.remove(entity)
                mesh.unload()

                createMesh(entity)?.draw()
                continue
            }

            mesh.draw()
        }


        glEnable(GL_CULL_FACE)
        if (Minosoft.config.config.game.entities.hitBox.disableZBuffer) {
            glDepthFunc(GL_LESS)
        }
    }


    companion object : RendererBuilder<EntityHitBoxRenderer> {
        override val RESOURCE_LOCATION = ResourceLocation("minosoft:entity_hitbox")

        override fun build(connection: PlayConnection, renderWindow: RenderWindow): EntityHitBoxRenderer {
            return EntityHitBoxRenderer(connection, renderWindow)
        }
    }
}
