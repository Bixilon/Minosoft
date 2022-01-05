package de.bixilon.minosoft.gui.rendering.framebuffer.world.overlay.overlays.simple

import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.framebuffer.world.overlay.Overlay
import de.bixilon.minosoft.gui.rendering.system.base.shader.Shader
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh
import de.bixilon.minosoft.gui.rendering.util.mesh.SimpleTextureMesh
import glm_.vec2.Vec2

abstract class SimpleOverlay(
    protected val renderWindow: RenderWindow,
    protected var z: Float,
) : Overlay {
    protected abstract val texture: AbstractTexture
    protected open val shader: Shader = renderWindow.shaderManager.genericTexture2dShader
    private var mesh = SimpleTextureMesh(renderWindow)
    protected var tintColor: RGBColor? = null

    override fun postInit() {
        updateMesh()
    }


    protected fun updateMesh() {
        if (mesh.state == Mesh.MeshStates.LOADED) {
            mesh.unload()
        }
        mesh = SimpleTextureMesh(renderWindow)

        mesh.addZQuad(Vec2(-1.0f, -1.0f), z, Vec2(+1.0f, +1.0f)) { position, uv -> mesh.addVertex(position, texture, uv, tintColor) }
        mesh.load()
    }

    override fun draw() {
        renderWindow.renderSystem.reset(blending = true)
        updateMesh()
        shader.use()
        mesh.draw()
    }
}
