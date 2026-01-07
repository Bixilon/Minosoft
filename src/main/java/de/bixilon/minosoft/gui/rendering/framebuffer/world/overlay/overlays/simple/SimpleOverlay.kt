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

package de.bixilon.minosoft.gui.rendering.framebuffer.world.overlay.overlays.simple

import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.framebuffer.world.overlay.Overlay
import de.bixilon.minosoft.gui.rendering.framebuffer.world.overlay.OverlayManager.Companion.OVERLAY_Z
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh
import de.bixilon.minosoft.gui.rendering.util.mesh.integrated.SimpleTextureMeshBuilder

abstract class SimpleOverlay(
    protected val context: RenderContext,
) : Overlay {
    protected abstract val texture: Texture
    protected open val shader = context.shaders.genericTexture2dShader
    private var mesh: Mesh? = null
    protected open val color: RGBAColor get() = ChatColors.WHITE
    protected open val uvStart get() = Vec2f.EMPTY
    protected open val uvEnd get() = Vec2f.ONE
    protected var invalid = true


    protected open fun createMesh(): Mesh {
        val mesh = SimpleTextureMeshBuilder(context)

        val color = color

        mesh.addVertex(-1.0f, -1.0f, OVERLAY_Z, texture, Vec2f(uvStart.x, uvEnd.y), color)
        mesh.addVertex(-1.0f, +1.0f, OVERLAY_Z, texture, Vec2f(uvStart.x, uvStart.y), color)
        mesh.addVertex(+1.0f, +1.0f, OVERLAY_Z, texture, Vec2f(uvEnd.x, uvStart.y), color)
        mesh.addVertex(+1.0f, -1.0f, OVERLAY_Z, texture, Vec2f(uvEnd.x, uvEnd.y), color)
        mesh.addIndexQuad()

        return mesh.bake()
    }

    private fun updateMesh() {
        if (this.mesh == null || invalid) {
            mesh?.unload()
        }
        this.mesh = createMesh()
        this.invalid = false
    }

    override fun draw() {
        updateMesh()
        val mesh = this.mesh ?: return

        shader.use()
        mesh.draw()
    }
}
