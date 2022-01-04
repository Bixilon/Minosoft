package de.bixilon.minosoft.gui.rendering.camera

import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.minosoft.data.registries.effects.DefaultStatusEffects
import de.bixilon.minosoft.data.registries.fluid.water.WaterFluid
import de.bixilon.minosoft.data.text.ChatColors
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition

class FogManager(
    private val renderWindow: RenderWindow,
) {
    private val blindness = renderWindow.connection.registries.statusEffectRegistry[DefaultStatusEffects.BLINDNESS]
    private val player = renderWindow.connection.player

    var fogColor: RGBColor? = null
        set(value) {
            if (field == value) {
                return
            }
            field = value
            updateShaders = true
        }
    private var fogStart = 0.0f
        set(value) {
            if (field == value) {
                return
            }
            field = value
            updateShaders = true
        }
    private var fogEnd = 0.0f
        set(value) {
            if (field == value) {
                return
            }
            field = value
            updateShaders = true
        }

    private var updateShaders = true

    fun draw() {
        calculateFog()
        if (!updateShaders) {
            return
        }
        updateShaders()
    }

    private fun calculateFog() {
        var fogStart = if (!renderWindow.connection.profiles.rendering.fog.enabled) {
            Float.MAX_VALUE
        } else {
            renderWindow.connection.world.view.viewDistance * ProtocolDefinition.SECTION_WIDTH_X - (ProtocolDefinition.SECTION_WIDTH_X / 2.0f) // could be improved? basically view distance in blocks and then the center of that chunk
        }
        var fogEnd = fogStart + 15.0f
        var color: RGBColor? = null

        player.submergedFluid?.nullCast<WaterFluid>()?.let {
            color = player.positionInfo.biome?.waterFogColor
            fogStart = 5.0f
            fogEnd = 10.0f
        } ?: player.activeStatusEffects[blindness]?.let {
            color = ChatColors.BLACK
            fogStart = 3.0f
            fogEnd = 5.0f
        }

        this.fogStart = fogStart
        this.fogEnd = fogEnd
        this.fogColor = color
    }


    private fun updateShaders() {
        val start = fogStart
        val end = fogEnd
        val color = fogColor
        for (shader in renderWindow.renderSystem.shaders) {
            if (!shader.uniforms.contains("uFogColor")) {
                continue
            }

            shader.use()

            shader["uFogStart"] = start
            shader["uFogEnd"] = end
            if (color == null) {
                shader["uUseFogColor"] = false
            } else {
                shader["uFogColor"] = color
                shader["uUseFogColor"] = true
            }
        }
        updateShaders = false
    }
}
