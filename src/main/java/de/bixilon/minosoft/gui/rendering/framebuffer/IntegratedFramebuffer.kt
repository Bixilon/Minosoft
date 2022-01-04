package de.bixilon.minosoft.gui.rendering.framebuffer

import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.renderer.Drawable
import de.bixilon.minosoft.gui.rendering.system.base.BlendingFunctions
import de.bixilon.minosoft.gui.rendering.system.base.IntegratedBufferTypes
import de.bixilon.minosoft.gui.rendering.system.base.PolygonModes
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.Framebuffer
import de.bixilon.minosoft.gui.rendering.system.base.shader.Shader
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh

interface IntegratedFramebuffer : Drawable {
    val renderWindow: RenderWindow
    val shader: Shader
    val framebuffer: Framebuffer
    val mesh: Mesh
    val polygonMode: PolygonModes


    fun init() {
        framebuffer.init()
        shader.load()
        shader.use()
        shader.setInt("uColor", 0)
        // shader.setInt("uDepth", 1)
        mesh.load()
    }

    fun clear() {
        renderWindow.renderSystem.framebuffer = framebuffer
        renderWindow.renderSystem.clear(IntegratedBufferTypes.COLOR_BUFFER, IntegratedBufferTypes.DEPTH_BUFFER)
    }

    override fun draw() {
        renderWindow.renderSystem.framebuffer = null
        renderWindow.renderSystem.reset(blending = true, sourceRGB = BlendingFunctions.SOURCE_ALPHA, destinationRGB = BlendingFunctions.ONE_MINUS_SOURCE_ALPHA)
        framebuffer.bindTexture()
        shader.use()
        mesh.draw()
    }
}
