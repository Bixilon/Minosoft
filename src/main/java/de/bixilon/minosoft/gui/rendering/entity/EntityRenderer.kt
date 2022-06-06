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

import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.renderer.Renderer
import de.bixilon.minosoft.gui.rendering.renderer.RendererBuilder
import de.bixilon.minosoft.gui.rendering.system.base.RenderSystem
import de.bixilon.minosoft.gui.rendering.system.base.phases.OpaqueDrawable
import de.bixilon.minosoft.gui.rendering.system.base.phases.SkipAll
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

class EntityRenderer(
    val connection: PlayConnection,
    override val renderWindow: RenderWindow,
) : Renderer, OpaqueDrawable, SkipAll {
    override val renderSystem: RenderSystem = renderWindow.renderSystem
    val profile = connection.profiles.entity.hitbox
    private val visibilityGraph = renderWindow.camera.visibilityGraph

    override val skipAll: Boolean
        get() = false

    override fun init(latch: CountUpAndDownLatch) {
    }

    override fun setupOpaque() {
        renderWindow.renderSystem.reset(faceCulling = false)
    }

    override fun drawOpaque() {
        connection.world.entities.lock.acquire()
        for (entity in connection.world.entities) {
            if (entity.model == null) {
                entity.model = entity.createModel(renderWindow)
            }
            entity.model?.draw()
        }
        connection.world.entities.lock.release()
    }


    companion object : RendererBuilder<EntityRenderer> {
        override val RESOURCE_LOCATION = ResourceLocation("minosoft:entity")

        override fun build(connection: PlayConnection, renderWindow: RenderWindow): EntityRenderer {
            return EntityRenderer(connection, renderWindow)
        }
    }
}
