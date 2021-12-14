package de.bixilon.minosoft.gui.rendering.camera

import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.camera.target.TargetHandler

class Camera(
    renderWindow: RenderWindow,
) {
    val matrixHandler = MatrixHandler(renderWindow)
    val targetHandler = TargetHandler(renderWindow, this)
    val fogManager = FogManager(renderWindow)

    fun init() {
        matrixHandler.init()
    }

    fun draw() {
        matrixHandler.entity.tick()
        matrixHandler.draw()
        targetHandler.raycast()
        fogManager.draw()
    }
}
