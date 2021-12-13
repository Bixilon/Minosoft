package de.bixilon.minosoft.gui.rendering.camera

import de.bixilon.minosoft.gui.rendering.RenderWindow

class Camera(
    private val renderWindow: RenderWindow,
) {
    private val connection = renderWindow.connection
    val matrixHandler = MatrixHandler(renderWindow)
    val raycastHandler = RaycastHandler(renderWindow, this)
    val fogManager = FogManager(renderWindow)

    fun init() {
        matrixHandler.init()
    }

    fun draw() {
        matrixHandler.entity.tick()
        matrixHandler.draw()
        raycastHandler.raycast()
        fogManager.draw()
    }
}
