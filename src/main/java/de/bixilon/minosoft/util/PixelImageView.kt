package de.bixilon.minosoft.util

import javafx.scene.canvas.Canvas
import javafx.scene.image.Image

class PixelImageView : Canvas() {
    var image: Image? = null
        set(value) {
            if (image == value) {
                return
            }
            graphicsContext2D.clearRect(0.0, 0.0, width, height)
            value?.let {
                graphicsContext2D.drawImage(it, 0.0, 0.0, width, height)
            }
            field = value
        }

    init {
        graphicsContext2D.isImageSmoothing = false
    }
}
