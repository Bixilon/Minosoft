package de.bixilon.minosoft.gui.rendering.models

object CullUtil {

    fun Array<FaceSize>.canCull(size: FaceSize): Boolean {
        for (faceSize in this) {
            if (
                faceSize.start.x <= size.start.x
                && faceSize.start.y <= size.start.y
                && faceSize.end.x >= size.end.x
                && faceSize.end.y >= size.end.y
            ) {
                return true
            }
        }
        return false
    }
}
