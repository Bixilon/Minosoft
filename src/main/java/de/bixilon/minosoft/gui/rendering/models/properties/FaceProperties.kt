package de.bixilon.minosoft.gui.rendering.models.properties

import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureTransparencies
import glm_.vec2.Vec2

data class FaceProperties(
    override val sizeStart: Vec2,
    override val sizeEnd: Vec2,
    override val transparency: TextureTransparencies,
) : AbstractFaceProperties
