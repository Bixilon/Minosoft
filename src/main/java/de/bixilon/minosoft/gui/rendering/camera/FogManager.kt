package de.bixilon.minosoft.gui.rendering.camera

import de.bixilon.minosoft.data.text.ChatColors
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderWindow

class FogManager(
    private val renderWindow: RenderWindow,
) {
    private var upToDate = false

    var fogColor: RGBColor = ChatColors.GREEN
        set(value) {
            field = value
            upToDate = false
        }
    private var fogStart = 0.0f
    private var fogEnd = 0.0f

    fun draw() {
        if (upToDate) {
            return
        }
        calculateFog()
        updateShaders()
    }

    private fun calculateFog() {
        fogStart = renderWindow.connection.world.view.viewDistance * 16.0f
        fogEnd = fogStart + 10.0f
    }


    private fun updateShaders() {
        for (shader in renderWindow.renderSystem.shaders) {
            if (!shader.uniforms.contains("uFogColor")) {
                continue
            }

            shader.use()

            shader.setFloat("uFogStart", fogStart)
            shader.setFloat("uFogEnd", fogEnd)
            shader["uFogColor"] = fogColor
        }
    }
}
