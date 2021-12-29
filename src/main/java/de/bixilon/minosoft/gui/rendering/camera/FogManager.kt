package de.bixilon.minosoft.gui.rendering.camera

import de.bixilon.minosoft.data.text.ChatColors
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.sky.SkyRenderer
import de.bixilon.minosoft.util.delegate.watcher.SimpleDelegateWatcher.Companion.watchRendering

@Deprecated("Needs some refactoring and improvements")
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

    fun init() {
        renderWindow.connection.world.view::viewDistance.watchRendering(this, true) { calculateFog() }
    }

    fun draw() {
        if (upToDate) {
            return
        }
        calculateFog()
        updateShaders()
    }

    private fun calculateFog() {
        if (!renderWindow.connection.profiles.rendering.fog.enabled) {
            // ToDo: This is not improving performance
            fogStart = Float.MAX_VALUE
            fogEnd = Float.MAX_VALUE
        } else {
            fogStart = renderWindow.connection.world.view.viewDistance * 16.0f - 8.0f // ToDo
            fogEnd = fogStart + 10.0f
        }
        renderWindow.renderer[SkyRenderer]?.let { fogColor = it.baseColor }
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
