package de.bixilon.minosoft.gui.rendering.models

import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureTransparencies

object CullUtil {

    fun Array<FaceProperties>.canCull(properties: FaceProperties, sameBlock: Boolean): Boolean {
        for (property in this) {
            if (
                property.sizeStart.x <= properties.sizeStart.x
                && property.sizeStart.y <= properties.sizeStart.y
                && property.sizeEnd.x >= properties.sizeEnd.x
                && property.sizeEnd.y >= properties.sizeEnd.y
                && !((properties.transparency == TextureTransparencies.OPAQUE && property.transparency != TextureTransparencies.OPAQUE)
                        || (properties.transparency != TextureTransparencies.OPAQUE && property.transparency == properties.transparency && !sameBlock)
                        || (properties.transparency == TextureTransparencies.TRANSPARENT && property.transparency == TextureTransparencies.TRANSLUCENT))
            ) {
                return true
            }
        }
        return false
    }
}
