package de.bixilon.minosoft.gui.rendering.framebuffer.world.overlay.overlays.simple

import de.bixilon.minosoft.data.registries.fluid.water.WaterFluid
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.framebuffer.world.overlay.OverlayFactory
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class WaterOverlay(renderWindow: RenderWindow, z: Float) : SimpleOverlay(renderWindow, z) {
    override val texture: AbstractTexture = renderWindow.textureManager.staticTextures.createTexture("minecraft:misc/underwater".toResourceLocation().texture())
    override val render: Boolean
        get() = renderWindow.connection.player.submergedFluid is WaterFluid

    override fun draw() {
        val brightness = renderWindow.connection.world.getBrightness(renderWindow.connection.player.positionInfo.blockPosition)
        tintColor = RGBColor(brightness, brightness, brightness, 0.1f)

        // ToDo: Minecraft sets the uv coordinates according to the yaw and pitch (see InGameOverlayRenderer::renderUnderwaterOverlay)

        super.draw()
    }


    companion object : OverlayFactory<WaterOverlay> {

        override fun build(renderWindow: RenderWindow, z: Float): WaterOverlay {
            return WaterOverlay(renderWindow, z)
        }
    }
}
