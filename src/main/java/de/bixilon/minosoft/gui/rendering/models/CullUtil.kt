package de.bixilon.minosoft.gui.rendering.models

import de.bixilon.minosoft.gui.rendering.models.properties.AbstractFaceProperties
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureTransparencies

object CullUtil {

    fun Array<AbstractFaceProperties>.canCull(properties: AbstractFaceProperties, blockCull: Boolean): Boolean {
        // ToDo: Sometimes faces get drawn between stairs (we need to swap xy with yx in special cases)
        val sizeStartX = properties.sizeStart.x
        val sizeStartY = properties.sizeStart.y
        val sizeEndX = properties.sizeEnd.x
        val sizeEndY = properties.sizeEnd.y
        for (property in this) {
            if (
                property.sizeStart.x <= sizeStartX
                && property.sizeStart.y <= sizeStartY
                && property.sizeEnd.x >= sizeEndX
                && property.sizeEnd.y >= sizeEndY
                && !((properties.transparency == TextureTransparencies.OPAQUE && property.transparency != TextureTransparencies.OPAQUE)
                        || (properties.transparency != TextureTransparencies.OPAQUE && property.transparency == properties.transparency && !blockCull)
                        || (properties.transparency == TextureTransparencies.TRANSPARENT && property.transparency == TextureTransparencies.TRANSLUCENT))
            ) {
                return true
            }
        }
        return false
    }
}
