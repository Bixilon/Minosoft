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

package de.bixilon.minosoft.gui.rendering.entities.hitbox

import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.gui.rendering.entities.feature.EntityRenderFeature
import de.bixilon.minosoft.gui.rendering.entities.renderer.EntityRenderer
import de.bixilon.minosoft.gui.rendering.util.mesh.LineMesh
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh

class HitboxFeature(renderer: EntityRenderer<*>) : EntityRenderFeature(renderer) {
    private val shader = renderer.renderer.context.shaders.genericColorShader
    private var mesh: LineMesh? = null

    override fun reset() {
        unload()
    }

    override fun update(millis: Long) {
        unload()
        val mesh = LineMesh(renderer.renderer.context)
        mesh.drawLazyAABB(renderer.entity.renderInfo.cameraAABB, renderer.entity.hitboxColor ?: ChatColors.WHITE)
    }


    override fun draw() {
        val mesh = this.mesh ?: return
        if (mesh.state != Mesh.MeshStates.LOADED) mesh.load()
        shader.use()
        mesh.draw()
    }

    override fun unload() {
        val mesh = this.mesh ?: return
        this.mesh = null
        renderer.renderer.queue += { mesh.unload() }
    }
}
