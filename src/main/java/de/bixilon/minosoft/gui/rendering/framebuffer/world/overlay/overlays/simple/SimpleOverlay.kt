/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
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
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.framebuffer.world.overlay.Overlay
import de.bixilon.minosoft.gui.rendering.framebuffer.world.overlay.OverlayManager
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh
import de.bixilon.minosoft.gui.rendering.util.mesh.integrated.SimpleTextureMeshBuilder

abstract class SimpleOverlay(
    protected val context: RenderContext,
) : Overlay {
    protected abstract val texture: Texture
    protected open val shader = context.shaders.genericTexture2dShader
    private var mesh: Mesh? = null
    protected var tintColor: RGBAColor? = null
    protected open var uvStart = Vec2f(0.0f, 0.0f)
    protected open var uvEnd = Vec2f(1.0f, 1.0f)


    protected fun updateMesh(): Mesh {
        val mesh = SimpleTextureMeshBuilder(context)

        mesh.addZQuad(Vec2f(-1.0f, -1.0f), OverlayManager.OVERLAY_Z, Vec2f(+1.0f, +1.0f), uvStart, uvEnd) { position, uv -> mesh.addVertex(position, texture, uv, tintColor) }

        return mesh.bake().apply { load() }
    }

    override fun draw() {
        mesh?.unload()
        val mesh = updateMesh() // ToDo: Don't update every time
        this.mesh = mesh

        shader.use()
        mesh.draw()
    }
}
