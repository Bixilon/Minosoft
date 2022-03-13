/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.world

import de.bixilon.minosoft.data.registries.effects.DefaultStatusEffects
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.system.base.shader.Shader
import de.bixilon.minosoft.gui.rendering.util.VecUtil.clamp
import de.bixilon.minosoft.gui.rendering.util.VecUtil.modify
import de.bixilon.minosoft.gui.rendering.util.VecUtil.toVec3
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.ONE
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.interpolateLinear
import glm_.glm
import glm_.vec3.Vec3
import org.lwjgl.system.MemoryUtil.memAllocFloat
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin

@Deprecated("Needs refactoring")
class LightMap(renderWindow: RenderWindow) {
    private val connection = renderWindow.connection
    private val profile = connection.profiles.rendering.light
    private val nightVisionStatusEffect = connection.registries.statusEffectRegistry[DefaultStatusEffects.NIGHT_VISION]
    private val conduitPowerStatusEffect = connection.registries.statusEffectRegistry[DefaultStatusEffects.CONDUIT_POWER]
    private val uniformBuffer = renderWindow.renderSystem.createFloatUniformBuffer(memAllocFloat(UNIFORM_BUFFER_SIZE))


    fun init() {
        // Set Alpha for all of them
        for (i in 0 until UNIFORM_BUFFER_SIZE / 4) {
            uniformBuffer.buffer.put(i * 4 + 3, 1.0f)
        }
        uniformBuffer.init()
        update()
    }

    fun use(shader: Shader, bufferName: String = "uLightMapBuffer") {
        uniformBuffer.use(shader, bufferName)
    }

    fun update() {
        val skyGradient = connection.world.time.lightBase.toFloat()

        // ToDo: Lightning

        val underwaterVisibility = 0.0f // ToDo

        val nightVisionEffect = connection.player.modifier.activeStatusEffects[nightVisionStatusEffect]

        val nightVisionVisibility = if (nightVisionEffect != null) {
            if (nightVisionEffect.duration > 200) {
                1.0f
            } else {
                0.7f + sin((nightVisionEffect.duration.toFloat()) * glm.PIf * 0.2f) * 0.3f
            }
        } else if (underwaterVisibility > 0.0f && connection.player.modifier.activeStatusEffects[conduitPowerStatusEffect] != null) {
            underwaterVisibility
        } else {
            0.0f
        }


        var skyGradientColor = Vec3(skyGradient, skyGradient, 1.0f)
        skyGradientColor = interpolateLinear(0.35f, skyGradientColor, Vec3.ONE)

        for (skyLight in 0 until 16) {
            for (blockLight in 0 until 16) {
                val index = ((skyLight shl 4) or blockLight) * 4


                val skyLightBrightness = (connection.world.dimension?.lightLevels?.get(skyLight) ?: 1.0f) * (skyGradient * 0.95f + 0.05f)
                val blockLightBrightness = (connection.world.dimension?.lightLevels?.get(blockLight) ?: 1.0f) * 1.5// ToDo: multiply with time somewhat thing?


                var color = Vec3(blockLightBrightness, blockLightBrightness * ((blockLightBrightness * 0.6f + 0.4f) * 0.6f + 0.4f), blockLightBrightness * (blockLightBrightness * blockLightBrightness * 0.6f + 0.4f))

                // ToDo: Lightning

                let {
                    color = color + (skyGradientColor * skyLightBrightness)

                    color = interpolateLinear(0.04f, color, Vec3(0.75f))

                    // ToDo: Sky darkness
                }

                color = color.clamp(0.0f, 1.0f)

                if (nightVisionVisibility > 0.0f) {
                    val gamma = max(color.x, max(color.y, color.z))
                    if (gamma < 1.0f) {
                        val copy = color.toVec3 * (1.0f / gamma)
                        color = interpolateLinear(nightVisionVisibility, color, copy)
                    }
                }

                color = interpolateLinear(profile.gamma, color, color.toVec3 modify { 1.0f - (1.0f - it).pow(4) })
                color = interpolateLinear(0.04f, color, Vec3(0.75f))
                color = color.clamp(0.0f, 1.0f)


                uniformBuffer.buffer.put(index + 0, color.x)
                uniformBuffer.buffer.put(index + 1, color.y)
                uniformBuffer.buffer.put(index + 2, color.z)
            }
        }
        uniformBuffer.upload()
    }

    private companion object {
        private const val UNIFORM_BUFFER_SIZE = 16 * 16 * 4 // skyLight * blockLight * RGBA
    }
}
